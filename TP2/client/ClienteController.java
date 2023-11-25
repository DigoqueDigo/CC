package client;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import client.download.Downloader;
import client.schedule.Schedule;
import packets.TCPPacket;


public class ClienteController{

    private Schedule schedule;

    public ClienteController(){
        this.schedule = new Schedule();
    }

    public void handler(TCPPacket tcpPacket) throws Exception{

        switch (tcpPacket.getProtocol()){

            case GETAK:

                System.out.println(ClientUI.YELLOW_BOLD + "A escalonar blocos..." + ClientUI.RESET);
                this.schedule.fillSchedule(tcpPacket);
                List<Thread> threads = new ArrayList<Thread>();

                for (String filename : this.schedule.getKeys()){
                    
                    threads.add(new Thread(
                        new Downloader(
                            filename,
                            this.schedule.getValue(filename))
                    ));

                    threads.get(threads.size()-1).start();
                }

                for (Thread thread : threads) {thread.join();}

                this.schedule.clearSchedule();
                break;

            case EXITACK:
                throw new EOFException();
        
            default:
                break;
        }
    }
}