package client;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import client.download.DownloadSchedule;
import client.download.Downloader;
import packets.TCPPacket;


public class ClienteControler{

    public ClienteControler(){}

    private List<String> getFilesName(TCPPacket tcpPacket){
        return tcpPacket.getToClient().getKeys().stream().map(x -> x.getFile()).distinct().collect(Collectors.toList());
    }

    public void handler(TCPPacket tcpPacket) throws Exception{

        switch (tcpPacket.getProtocol()){

            case GETAK:

                List<Thread> threads = new ArrayList<Thread>();

                for (String filename : getFilesName(tcpPacket)){
                    
                    DownloadSchedule schedule = new DownloadSchedule();
                    schedule.fillSchedule(tcpPacket,filename);
                    
                    threads.add(new Thread(new Downloader(filename,schedule)));
                    threads.get(threads.size()-1).start();
                }

                for (Thread thread : threads) {thread.join();}
                break;

            case EXITACK:
                throw new EOFException();
        
            default:
                break;
        }
    }
}
