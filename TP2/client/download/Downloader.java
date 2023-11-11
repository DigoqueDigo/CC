package client.download;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import packets.TCPPacket;
import packets.info.PieceInfo;


public class Downloader implements Runnable{

    private String filename;
    private TCPPacket tcpPacket;
    private DownloadSchedule schedule;
    private List<byte[]> file_buffer;
    private FileOutputStream outputStream;

    
    public Downloader(String filename, TCPPacket tcpPacket) throws IOException{
        this.filename = filename;
        this.tcpPacket = tcpPacket;
        this.schedule = new DownloadSchedule();
        this.file_buffer = new ArrayList<byte[]>();
        this.outputStream = new FileOutputStream(filename);
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

    
    private void writeToFile(FileOutputStream outputStream, List<byte[]> file_buffer) throws IOException{
        for (byte[] element : file_buffer) {outputStream.write(element);}
        outputStream.close();
    }


    public void run(){

        try{

            int index = 0;
            this.initSchedule(this.tcpPacket);
            Thread[] threads = new Thread[this.schedule.size()];
            this.file_buffer = new ArrayList<>(this.schedule.getNumberOfPieces());

            for (Map.Entry<String,List<PieceInfo>> element : this.schedule.entrySet()){

                threads[index] = new Thread(
                    new DownloaderWorker(
                        element.getKey(),
                        element.getValue(),
                        file_buffer
                    )
                );
                
                threads[index++].start();
            }

            for (int p = 0; p < index; p++){
                threads[p].join();
            }

            this.writeToFile(outputStream,file_buffer);
        }

        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
