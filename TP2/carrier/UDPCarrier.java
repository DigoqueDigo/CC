package carrier;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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


    private void setSeqNums(List<UDPPacket> packets){
        int p = 0;
        for (UDPPacket packet : packets){
            packet.setSeqNum(p++);
        }
    }


    public void sendUDPPacket(DatagramSocket socket, List<UDPPacket> list) throws IOException{

        UDPPacket udpPacket_send;
        UDPPacket udpPacket_receive;
        DatagramPacket packets_send = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);
        DatagramPacket packets_receive = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);
        List<UDPPacket> udpPackets = list.stream().map(x -> x.clone()).collect(Collectors.toList());
           
        setSeqNums(udpPackets);
        socket.setSoTimeout(UDPCarrier.SENDER_TIMEOUT);
    
    //    System.out.println("A ENVIAR PARA: " + socket.getInetAddress().getHostAddress() + " " + socket.getPort());
    //    System.out.println("ENTROU UDPCARRIER " + ACKList.size());
    //    System.out.flush();

        while (udpPackets.size() > 0){

            int window_size = Math.min(udpPackets.size(),UDPCarrier.WINDOW_SIZE);

            for (int index = 0; index < (udpPackets.size() + window_size); index++){
                
                if (index < udpPackets.size()){

                    udpPacket_send = udpPackets.get(index);
                    
                    packets_send.setAddress(InetAddress.getByName(udpPacket_send.getIPdest()));
                    packets_send.setPort(udpPacket_send.getPortdest());
                    packets_send.setData(udpPacket_send.serializeUDPPacket());
                    socket.send(packets_send);

            //        System.out.println("UDPCARRIER PACOTE ENVIADO");
            //        System.out.println("INDEX: " + index);
            //        System.out.println(udpPacket_send);
            //        System.out.flush();

                    System.out.println("TRY SEND");
                }

                if (index >= window_size){

                    try{

            //            System.out.println("IINDEX: " + index);
                        
                        socket.receive(packets_receive);
                        udpPacket_receive = UDPPacket.deserializeUDPPacket(packets_receive.getData());

                        if (udpPacket_receive.getProtocol() == UDPProtocol.ACK){
                            udpPackets.remove(udpPacket_receive);
                            index--;
             //               System.out.println("SENDER RECEIVE ACK");
                        }
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
        DatagramPacket packets_receive = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);
        DatagramPacket packets_send = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);

        socket.setSoTimeout(UDPCarrier.RECEIVER_TIMEOUT);

        while (hasNext){

            try{

             //   System.out.println("À ESCUTA");
                socket.receive(packets_receive);
                udpPacket_receive = UDPPacket.deserializeUDPPacket(packets_receive.getData());

                if (udpPacket_receive.checkSHA1()){

                   // System.out.print("Packet received: " + udpPacket_receive.toString());
                    
                    if (!result.contains(udpPacket_receive)){
                        udpPacket_receive.setIPsource(packets_receive.getSocketAddress());
                        result.add(udpPacket_receive.clone());
                    }

                    udpPacket_send = new UDPPacket(
                        UDPProtocol.ACK,
                        udpPacket_receive.getIPdest(),
                        udpPacket_receive.getIPsource(),
                        udpPacket_receive.getPortdest(),
                        udpPacket_receive.getPortsource()
                    );

                    udpPacket_send.setSeqNum(udpPacket_receive.getSeqNum());
                    udpPacket_send.setData(new byte[0]);
                
                    packets_send.setSocketAddress(packets_receive.getSocketAddress());
                    packets_send.setData(udpPacket_send.serializeUDPPacket());
                    socket.send(packets_send);
               }
            }

            catch (SocketTimeoutException e){
                hasNext = false;
            }
        }

        System.out.println("RESULT SIZE: " + result.size());
        return result;
    }
}