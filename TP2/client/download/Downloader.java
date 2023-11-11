package client.download;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import packets.TCPPacket;
import packets.info.PieceInfo;


public class Downloader implements Runnable{

    private String filename;
    private TCPPacket tcpPacket;
    private DownloadSchedule schedule;
    private FileOutputStream outputStream;
    private ConcurrentMap<Integer,byte[]> buffer;

    
    public Downloader(String filename, TCPPacket tcpPacket) throws IOException{
        this.filename = filename;
        this.tcpPacket = tcpPacket;
        this.schedule = new DownloadSchedule();
        this.outputStream = new FileOutputStream(filename);
        this.buffer = new ConcurrentHashMap<Integer,byte[]>();
    }

    
    private void initSchedule(TCPPacket tcpPacket){

        Random random = new Random();
    
        for (PieceInfo pieceInfo : tcpPacket.getToClient().getKeys()){

            if (pieceInfo.getFile().equals(this.filename)){

                List<String> IPaddresses = tcpPacket.getToClient().getValue(pieceInfo);
            
                this.schedule.addPieceInfo(
                    IPaddresses.get(random.nextInt(IPaddresses.size())),
                    pieceInfo
                );
            }
        }
    }

    
    private void writeToFile(FileOutputStream outputStream, ConcurrentMap<Integer,byte[]> buffer) throws IOException{
        Comparator<Map.Entry<Integer,byte[]>> comparator = Comparator.comparingInt(x -> x.getKey());
        buffer.entrySet().stream().sorted(comparator).map(x -> x.getValue()).forEach(x -> {
            try {outputStream.write(x);}
            catch (Exception e) {System.out.println(e.getMessage());}
        });
        
        outputStream.close();
    }


    public void run(){

        try{

            int index = 0;
            this.initSchedule(this.tcpPacket);
            Thread[] threads = new Thread[this.schedule.size()];

            for (Map.Entry<String,List<PieceInfo>> element : this.schedule.entrySet()){

                threads[index] = new Thread(
                    new DownloaderWorker(
                        element.getKey(),
                        element.getValue(),
                        buffer
                    )
                );
                
                threads[index++].start();
            }

            for (int p = 0; p < index; p++){
                threads[p].join();
            }

            this.writeToFile(outputStream,buffer);
        }

        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}