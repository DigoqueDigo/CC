package carrier;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import packets.UDPPacket;
import packets.UDPPacket.UDPProtocol;


public class UDPCarrier{

    private static final int SENDER_TIMEOUT = 1000;
    private static final int RECEIVER_TIMEOUT = 2000;
    private static final int WINDOW_SIZE = 10;
    private static UDPCarrier singleton = null;

    private UDPCarrier(){}

    
    public static UDPCarrier getInstance(){
        if (UDPCarrier.singleton == null) UDPCarrier.singleton = new UDPCarrier();
        return UDPCarrier.singleton;
    }


    private List<Integer> getACKList(int size){
        List<Integer> result = new ArrayList<>();
        for (int p = 0; p < size; p++) {result.add(p);}
        return result;
    }


    public void sendUDPPacket(DatagramSocket socket, List<UDPPacket> udpPackets) throws IOException{

        UDPPacket udpPacket_send;
        UDPPacket udpPacket_receive;
        List<Integer> ACKList = this.getACKList(udpPackets.size());
        DatagramPacket packet = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);
            
        socket.setSoTimeout(UDPCarrier.SENDER_TIMEOUT);
    //    System.out.println("A ENVIAR PARA: " + socket.getInetAddress().getHostAddress() + " " + socket.getPort());
    //    System.out.println("ENTROU UDPCARRIER " + ACKList.size());
    //    System.out.flush();

        while (ACKList.size() > 0){

            int window_size = Math.min(ACKList.size(),UDPCarrier.WINDOW_SIZE);

            for (int index = 0; index < (ACKList.size() + window_size); index++){
                
                if (index < ACKList.size()){

                    udpPacket_send = udpPackets.get(ACKList.get(index));
                    udpPacket_send.setIP(socket.getLocalAddress().getHostAddress());
                    udpPacket_send.setPort(socket.getLocalPort());
                    udpPacket_send.setSeqNum(ACKList.get(index));
                    
                    packet.setData(udpPacket_send.serializeUDPPacket());
                    socket.send(packet);

            //        System.out.println("UDPCARRIER PACOTE ENVIADO");
            //        System.out.println("INDEX: " + index);
            //        System.out.println(udpPacket_send);
            //        System.out.flush();
                }

                if (index >= window_size){

                    try{

            //            System.out.println("IINDEX: " + index);
                        
                        socket.receive(packet);
                        udpPacket_receive = UDPPacket.deserializeUDPPacket(packet.getData());
                        
                        if (udpPacket_receive.getProtocol() == UDPProtocol.ACK){
                            ACKList.remove(udpPacket_receive.getSeqNum());
                            index--;
             //               System.out.println("SENDER RECEIVE ACK");
                        }

                        System.out.flush();
                    }

                    catch (SocketTimeoutException e){
         //               System.out.println("SENDER TIMEOUT");
                    }
                }

       //         System.out.println("---------------------------------");
            }
        }

    //    System.out.println("ENVIADO COM SUCESSO");
    }


    public List<UDPPacket> receiveUDPPacket(DatagramSocket socket) throws IOException{

        boolean hasNext = true;
        UDPPacket udpPacket_send;
        UDPPacket udpPacket_receive;
        List<UDPPacket> result = new ArrayList<UDPPacket>(0);
        DatagramPacket packet_receive = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);
        DatagramPacket packet_send = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);

        socket.setSoTimeout(UDPCarrier.RECEIVER_TIMEOUT);

        while (hasNext){

            try{

             //   System.out.println("À ESCUTA");
                socket.receive(packet_receive);
                udpPacket_receive = UDPPacket.deserializeUDPPacket(packet_receive.getData());

                if (udpPacket_receive.checkSHA1()){

                   // System.out.print("Packet received: " + udpPacket_receive.toString());
                    
                    if (!result.contains(udpPacket_receive)) result.add(udpPacket_receive.clone());

                    udpPacket_send = new UDPPacket(UDPProtocol.ACK);
                    udpPacket_send.setIP(socket.getLocalAddress().getHostAddress());
                    udpPacket_send.setPort(socket.getLocalPort());
                    udpPacket_send.setSeqNum(udpPacket_receive.getSeqNum());
                    udpPacket_send.setData(new byte[0]);
                
                    packet_send.setSocketAddress(packet_receive.getSocketAddress());
                    packet_send.setData(udpPacket_send.serializeUDPPacket());
                    socket.send(packet_send);
               }
            }

            catch (SocketTimeoutException e){
                hasNext = false;
            }
        }

        return result;
    }
}