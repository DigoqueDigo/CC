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

            packets_send.add(udpPacket);
            carrier.sendUDPPacket(socket,packets_send); // envia para o listener
      //      System.out.println(System.currentTimeMillis());
     
            //      System.out.println(socket.getLocalAddress().getHostAddress());
            //      System.out.println(socket.getLocalPort());
            
            packets_send.clear();

     //       System.out.println(socket.getLocalAddress().getHostAddress());
      //      System.out.println(socket.getLocalPort());

            
            System.out.println("DOWNLOADERWORKER -> Listener");
            System.out.println(udpPacket.toString());

        //    Thread.sleep(2000);

             while (packets_receive.size() == 0){
                packets_receive = carrier.receiveUDPPacket(socket);
             } // recebe do listenerWorker


            udpPacket = packets_receive.get(0);
            packets_receive.clear();

            System.out.println("Listener -> DOWNLOADERWORKER");
            System.out.println(udpPacket.toString());

            System.out.println("---------------------");
            packets_send = this.createUDPPacketsList(this.pieces,udpPacket.getIPsource(),udpPacket.getPortsource());
            packets_send.forEach(x -> System.out.println(x));

            carrier.sendUDPPacket(socket,packets_send); // enviar as pieces que quero para o listenerWorker
            
            packets_send.clear();
            System.out.println("A recebe pacotes");
            
            while (packets_receive.size() == 0){
                packets_receive = carrier.receiveUDPPacket(socket); // receber as pieces enviadas a partir do listenerWorker
            }

            packets_receive.forEach(x -> this.buffer.put(x.getPiece().getPosition(),x.getData()));
        }

        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}