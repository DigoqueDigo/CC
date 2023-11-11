package client.listener;
import java.net.DatagramSocket;
import carrier.UDPCarrier;
import packets.UDPPacket;


public class Listener implements Runnable{

    public static final int DefaultPort = 54321;
    
    private DatagramSocket socket;


    public Listener(DatagramSocket socket){
        this.socket = socket;
    }

    
    public void run(){

        boolean hasNext = true;
        UDPCarrier carrier = UDPCarrier.getInstance();

        while (hasNext) {
            
            try{

                for (UDPPacket packet : carrier.receiveUDPPacket(socket)){

                    new Thread(
                        new ListenerWorker(
                            packet.getIP(),
                            packet.getPort())
                    ).start();
                }
            }
    
            catch (Exception e){
                hasNext = false;
                System.out.println(e.getMessage());
            }
        }
    }
}