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

    private TCPPacket tcpPacket;
    private DownloadSchedule schedule;
    private ArrayList<byte[]> file_buffer;
    private FileOutputStream outputStream;

    
    public Downloader(TCPPacket tcpPacket) throws IOException{
        this.tcpPacket = tcpPacket;
        this.schedule = new DownloadSchedule();
        this.file_buffer = new ArrayList<byte[]>();
        this.outputStream = new FileOutputStream(
            tcpPacket.getToClient().getKeys().get(0).getFile()
        );
    }

    
    private void initSchedule(TCPPacket tcpPacket){

        Random random = new Random();
    
        for (PieceInfo pieceInfo : tcpPacket.getToClient().getKeys()){

            List<String> IPaddresses = tcpPacket.getToClient().getValue(pieceInfo);
          
            this.schedule.addPieceInfo(IPaddresses.get(
                random.nextInt(IPaddresses.size())),
                pieceInfo
            );
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
            this.file_buffer.ensureCapacity(this.schedule.getNumberOfPieces());;

            Thread[] threads = new Thread[this.schedule.size()];

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
        }
    }
}
