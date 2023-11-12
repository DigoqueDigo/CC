package client.download;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import client.Client;
import packets.info.PieceInfo;


public class Downloader implements Runnable{

    private DownloadSchedule schedule;
    private FileOutputStream outputStream;
    private ConcurrentMap<Integer,byte[]> buffer;

    
    public Downloader(String filename, DownloadSchedule schedule) throws IOException{
        this.schedule = schedule;
        this.outputStream = new FileOutputStream(Client.FOLDER + filename);
        this.buffer = new ConcurrentHashMap<Integer,byte[]>();
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

            List<Thread> threads = new ArrayList<Thread>();

            for (Map.Entry<String,List<PieceInfo>> element : this.schedule.entrySet()){

                threads.add(new Thread(
                    new DownloaderWorker(
                        element.getKey(),
                        element.getValue(),
                        buffer)
                ));
                
                threads.get(threads.size()-1).start();
            }

            for (Thread thread : threads) {thread.join();}

            this.writeToFile(outputStream,buffer);
        }

        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}