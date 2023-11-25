package client.listener;
import java.net.DatagramSocket;
import carrier.UDPCarrier;
import packets.UDPPacket;
import packets.UDPPacket.UDPProtocol;


public class Listener implements Runnable{

    public static final int DefaultPort = 54321;
    private DatagramSocket socket;

    public Listener(DatagramSocket socket){
        this.socket = socket;
    }

    
    public void run(){
        
        boolean hasNext = true;
        UDPCarrier carrier = UDPCarrier.getInstance();
        
        while (hasNext){
            
            try{

                for (UDPPacket packet : carrier.receiveUDPPacket(socket)){

        //            System.out.println("LISTENER <- DOWNLOADWORKER");
        //            System.out.println(packet.toString());

                    if (packet.getProtocol() == UDPProtocol.HELLO){

                        new Thread(
                            new ListenerWorker(
                                packet.getIPsource(),
                                packet.getPortsource()))
                            .start();
                    }
                }
            }
    
            catch (Exception e){
                hasNext = false;
                e.printStackTrace();
            }
        }
    }
}