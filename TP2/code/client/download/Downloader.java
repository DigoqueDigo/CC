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
import client.ClientUI;
import client.resolver.Resolver;
import client.schedule.DownloadSchedule;
import packets.DNSPacket;
import packets.DNSPacket.DNSProtocol;
import packets.UDPPacket.UDPProtocol;
import packets.info.PieceInfo;


public class Downloader implements Runnable{

    private String filename;
    private DownloadSchedule schedule;
    private ConcurrentMap<Integer,byte[]> buffer;


    public Downloader(String filename, DownloadSchedule schedule) throws IOException{
        this.filename = filename;
        this.schedule = schedule;
        this.buffer = new ConcurrentHashMap<Integer,byte[]>();
    }


    private void writeToFile() throws IOException{

        FileOutputStream outputStream = new FileOutputStream(Client.FOLDER + this.filename);
        Comparator<Map.Entry<Integer,byte[]>> comparator = Comparator.comparingInt(x -> x.getKey());

        buffer.entrySet().stream().sorted(comparator).map(x -> x.getValue()).forEach(x -> {
            try {outputStream.write(x);}
            catch (Exception e) {e.printStackTrace();}
        });

        outputStream.close();
    }


    public void run(){

        try{

            System.out.println(ClientUI.YELLOW_BOLD + "Download iniciado: " + this.filename + ClientUI.RESET);

            long start = System.currentTimeMillis();
            List<Thread> threads = new ArrayList<Thread>();

            for (Map.Entry<String,List<PieceInfo>> element : this.schedule.entrySet()){

                DNSPacket dnsRequest = new DNSPacket(DNSProtocol.REQUEST,element.getKey());
                DNSPacket dnsResponse = Resolver.getInstance().resolve(dnsRequest,Client.DNSAddress,Client.DNSPort);

                if (dnsResponse.getProtocol() == DNSProtocol.RESPONSE){
                    threads.add(new Thread(
                        new DownloaderWorker(
                            dnsResponse.getAddress(),
                            element.getValue(),
                            buffer)
                    ));
                }

                threads.get(threads.size()-1).start();
            }

            for (Thread thread : threads) {thread.join();}

            System.out.println(ClientUI.YELLOW_BOLD + "Download finalizado (" + (System.currentTimeMillis() - start) + " ms): " + this.filename + ClientUI.RESET);

            this.writeToFile();
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }
}