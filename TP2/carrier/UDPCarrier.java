package carrier;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.zip.CRC32C;
import packets.UDPPacket;
import packets.UDPPacket.UDPProtocol;


public class UDPCarrier{

    private static final int SENDER_TIMEOUT = 25;
    private static final int RECEIVER_TIMEOUT = 500;
    private static final int WINDOW_SIZE = 10;
    private static final int SEQNUMBOUND = 1024;
    private static UDPCarrier singleton = null;

    private UDPCarrier(){}

    
    public static UDPCarrier getInstance(){
        if (UDPCarrier.singleton == null) UDPCarrier.singleton = new UDPCarrier();
        return UDPCarrier.singleton;
    }


    private void setSeqNums(List<UDPPacket> packets){
        int p = new Random().nextInt(SEQNUMBOUND);
        for (UDPPacket packet : packets){
            packet.setSeqNum(p++);
        }
    }


    private boolean checkUDPPacket(DatagramPacket packet, UDPPacket udpPacket){
        CRC32C checksum = new CRC32C();
        checksum.update(packet.getData(),0,packet.getLength()-UDPPacket.CheckSumSize);
        return checksum.getValue() == udpPacket.getChecksum();
    }


    public void sendUDPPacket(DatagramSocket socket, List<UDPPacket> list) throws IOException{

        UDPPacket udpPacket_send;
        UDPPacket udpPacket_receive;
        DatagramPacket packets_send = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);
        DatagramPacket packets_receive = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);
        List<UDPPacket> udpPackets = list.stream().map(x -> x.clone()).collect(Collectors.toList());
           
        setSeqNums(udpPackets);
        socket.setSoTimeout(UDPCarrier.SENDER_TIMEOUT);

        while (udpPackets.size() > 0){

            int window_size = Math.min(udpPackets.size(),UDPCarrier.WINDOW_SIZE);

            for (int index = 0; index < (udpPackets.size() + window_size); index++){
                
                if (index < udpPackets.size()){

                    udpPacket_send = udpPackets.get(index);
                    packets_send.setAddress(InetAddress.getByName(udpPacket_send.getIPdest()));
                    packets_send.setPort(udpPacket_send.getPortdest());
                    packets_send.setData(udpPacket_send.serialize());
                    socket.send(packets_send);
                }

                if (index >= window_size){

                    try{
                        
                        socket.receive(packets_receive);
                        udpPacket_receive = UDPPacket.deserialize(packets_receive.getData());

                        if (udpPacket_receive.getProtocol() == UDPProtocol.ACK){
                            udpPackets.remove(udpPacket_receive);
                            index--;
                        }
                    }

                    catch (SocketTimeoutException e){}
                }
            }
        }
    }


    public List<UDPPacket> receiveUDPPacket(DatagramSocket socket) throws IOException{

        boolean hasNext = true;
        UDPPacket udpPacket_send;
        UDPPacket udpPacket_receive;
        List<UDPPacket> result = new ArrayList<UDPPacket>(0);
        DatagramPacket datagram_receive = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);
        DatagramPacket datagram_send = new DatagramPacket(new byte[UDPPacket.MaxSize],UDPPacket.MaxSize);

        socket.setSoTimeout(UDPCarrier.RECEIVER_TIMEOUT);

        while (hasNext){

            try{

                socket.receive(datagram_receive);
                udpPacket_receive = UDPPacket.deserialize(datagram_receive.getData());

                if (checkUDPPacket(datagram_receive,udpPacket_receive) && udpPacket_receive.checkSHA1()){
                    
                    if (!result.contains(udpPacket_receive)){
                        udpPacket_receive.setIPsource(datagram_receive.getSocketAddress());
                        result.add(udpPacket_receive);
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
                
                    datagram_send.setSocketAddress(datagram_receive.getSocketAddress());
                    datagram_send.setData(udpPacket_send.serialize());
                    socket.send(datagram_send);
                }
            }

            catch (SocketTimeoutException e){
                hasNext = false;
            }
        }

        return result;
    }
}