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

    private static final int SENDER_TIMEOUT = 25;
    private static final int RECEIVER_TIMEOUT = 2000;
    private static final int WINDOW_SIZE = 10;
    private static UDPCarrier singleton = null;

    private UDPCarrier(){}

    
    public static UDPCarrier getInstance(){
        if (UDPCarrier.singleton == null) UDPCarrier.singleton = new UDPCarrier();
        return UDPCarrier.singleton;
    }


    private List<Integer> getACKList(int size){
        List<Integer> result = new ArrayList<>(size);
        for (int p = 0; p < result.size(); p++) {result.set(p,p);}
        return result;
    }


    public void sendUDPPacket(DatagramSocket socket, List<UDPPacket> udpPackets) throws IOException{

        UDPPacket udpPacket;
        List<Integer> ACKList = this.getACKList(udpPackets.size());
        DatagramPacket packet = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);
            
        socket.setSoTimeout(UDPCarrier.SENDER_TIMEOUT);

        while (ACKList.size() > 0){

            int window_size = Math.min(ACKList.size(),UDPCarrier.WINDOW_SIZE);

            for (int index = 0; index < ACKList.size() + window_size; index++){
                
                if (index < ACKList.size()){
                    
                    udpPackets.get(ACKList.get(index)).setProtocol(UDPProtocol.DATA);
                    udpPackets.get(ACKList.get(index)).setIP(socket.getLocalAddress().getHostAddress());
                    udpPackets.get(ACKList.get(index)).setPort(socket.getLocalPort());
                    udpPackets.get(ACKList.get(index)).setSeqNum(ACKList.get(index));
                    
                    packet.setData(udpPackets.get(ACKList.get(index)).serializeUDPPacket());
                    socket.send(packet);
                }

                if (index > window_size){

                    try{
                        socket.receive(packet);
                        udpPacket = UDPPacket.deserializeUDPPacket(packet.getData());
                        if (udpPacket.getProtocol() == UDPProtocol.ACK) ACKList.remove(udpPacket.getSeqNum());
                    }

                    catch (SocketTimeoutException e){
                        System.out.println("SENDER TIMEOUT");
                    }
                }
            }
        }
    }


    public ArrayList<UDPPacket> receiveUDPPacket(DatagramSocket socket) throws IOException{

        boolean hasNext = true;
        UDPPacket udpPacket;
        ArrayList<UDPPacket> result = new ArrayList<UDPPacket>(0);
        DatagramPacket packet = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);

        socket.setSoTimeout(UDPCarrier.RECEIVER_TIMEOUT);

        while (hasNext){

            try{

                socket.receive(packet);
                udpPacket = UDPPacket.deserializeUDPPacket(packet.getData());

                result.ensureCapacity(udpPacket.getSeqNum());
                result.set(udpPacket.getSeqNum(),udpPacket.clone());

                udpPacket.setProtocol(UDPProtocol.ACK);
                udpPacket.setIP(socket.getLocalAddress().getHostAddress());
                udpPacket.setPort(socket.getLocalPort());
                udpPacket.setData(new byte[0]);
                
                packet.setData(udpPacket.serializeUDPPacket());
                socket.send(packet);
            }

            catch (SocketTimeoutException e){
                hasNext = false;
                System.out.println("RECEIVER TIMEOUT");
            }
        }

        return result;
    }
}