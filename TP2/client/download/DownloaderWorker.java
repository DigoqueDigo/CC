package client.download;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import carrier.UDPCarrier;
import client.listener.Listener;
import packets.UDPPacket;
import packets.UDPPacket.UDPProtocol;
import packets.info.PieceInfo;


public class DownloaderWorker implements Runnable{

    DatagramSocket socket;
    private String listenerAddress;
    private List<PieceInfo> pieces;
    private ConcurrentMap<Integer,byte[]> buffer;


    public DownloaderWorker(String listenerAddress, List<PieceInfo> pieces, ConcurrentMap<Integer,byte[]> buffer) throws SocketException{
        this.listenerAddress = listenerAddress;
        this.pieces = pieces;
        this.buffer = buffer;
        this.socket = new DatagramSocket();
    }


    private List<UDPPacket> createUDPPacketsList(List<PieceInfo> pieces, String IPdest, int Portdest){
        
        List<UDPPacket> packets = new ArrayList<UDPPacket>();
        
        pieces.forEach(x -> {
            
            UDPPacket udpPacket = new UDPPacket(
                UDPProtocol.GET,
                this.socket.getLocalAddress().getHostAddress(),
                IPdest,
                this.socket.getLocalPort(),
                Portdest
            );

            udpPacket.setPiece(x);
            packets.add(udpPacket);
        });

        return packets;
    }


    public void run(){

        try{

            UDPPacket udpPacket;
            UDPCarrier carrier = UDPCarrier.getInstance();
            List<UDPPacket> packets_send = new ArrayList<UDPPacket>();
            List<UDPPacket> packets_receive = new ArrayList<UDPPacket>();
            
            udpPacket = new UDPPacket(
                UDPProtocol.HELLO,
                this.socket.getLocalAddress().getHostAddress(),
                this.listenerAddress,
                this.socket.getLocalPort(),
                Listener.DefaultPort
            );

        //    System.out.println("DOWNLOADERWORKER -> LISTENER");

            packets_send.add(udpPacket);
            carrier.sendUDPPacket(socket,packets_send);
            packets_send.clear();
            
        //    System.out.println(udpPacket);

            while (packets_receive.size() == 0){
                packets_receive = carrier.receiveUDPPacket(socket);
            }

            udpPacket = packets_receive.get(0);
            packets_receive.clear();

        //    System.out.println("DOWNLOADERWORKER <- LISTENERWORKER");
        //    System.out.println(udpPacket);

        //    System.out.println("------------------------------------------------");
        //    System.out.println("DOWNLOADERWORKER -> LISTENERWORKER");
            
            packets_send = this.createUDPPacketsList(this.pieces,udpPacket.getIPsource(),udpPacket.getPortsource());
        //    packets_send.forEach(x -> System.out.println(x));

        //    System.out.println("------------------------------------------------");
            
            carrier.sendUDPPacket(socket,packets_send);
            packets_send.clear();

            while (packets_receive.size() == 0){
                packets_receive = carrier.receiveUDPPacket(socket);
            }

        //    System.out.println("------------------------------------------------");
        //    System.out.println("DOWNLOADERWORKER <- LISTENERWORKER");

        //    packets_receive.forEach(x -> System.out.println(x));

        //    System.out.println("------------------------------------------------");

            packets_receive.forEach(x -> this.buffer.put(x.getPiece().getPosition(),x.getData()));
            packets_receive.clear();
            this.socket.close();
        }

        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}