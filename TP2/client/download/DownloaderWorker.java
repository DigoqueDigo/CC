package client.download;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import carrier.UDPCarrier;
import client.listener.Listener;
import packets.UDPPacket;
import packets.UDPPacket.UDPProtocol;
import packets.info.PieceInfo;


public class DownloaderWorker implements Runnable{

    DatagramSocket socket;
    private String listenerAddress;
    private List<PieceInfo> pieces;
    private List<byte[]> file_buffer;


    public DownloaderWorker(String listenerAddress, List<PieceInfo> pieces, List<byte[]> file_buffer) throws SocketException{
        this.listenerAddress = listenerAddress;
        this.pieces = pieces;
        this.file_buffer = file_buffer;
        this.socket = new DatagramSocket();
    }


    private void connectSocket(String IPaddress, int Port){
        
        try{
            InetAddress address = InetAddress.getByName(IPaddress);
            this.socket.connect(address,Port);
        }

        catch (Exception e){
            System.out.println("Can not connect UDP socket");
        }
    }


    private List<UDPPacket> createUDPPacketsList(List<PieceInfo> pieces){
        
        List<UDPPacket> packets = new ArrayList<UDPPacket>();
        
        pieces.forEach(x -> {
            UDPPacket udpPacket = new UDPPacket(UDPProtocol.GET);
            udpPacket.setPiece(x);
            packets.add(udpPacket);
        });

        return packets;
    }


    public void run(){

        try{


            UDPCarrier carrier = UDPCarrier.getInstance();
            UDPPacket udpPacket = new UDPPacket(UDPProtocol.HELLO);
            List<UDPPacket> packets_send = new ArrayList<UDPPacket>();
            List<UDPPacket> packets_receive = new ArrayList<UDPPacket>();

            packets_send.add(udpPacket);            

            connectSocket(this.listenerAddress,Listener.DefaultPort);
            carrier.sendUDPPacket(socket,packets_send); // envia para o listener
      //      System.out.println(socket.getLocalAddress().getHostAddress());
      //      System.out.println(socket.getLocalPort());
            socket.disconnect();
            packets_send.clear();

     //       System.out.println(socket.getLocalAddress().getHostAddress());
      //      System.out.println(socket.getLocalPort());
            System.out.println("DOWNLOADERWORKER -> Listener");
            System.out.println(udpPacket.toString());

            packets_receive = carrier.receiveUDPPacket(socket); // recebe do listenerWorker
            udpPacket = packets_receive.get(0);

            System.out.println("Listener -> DOWNLOADERWORKER");
            System.out.println(udpPacket.toString());

            packets_send = this.createUDPPacketsList(this.pieces);

            connectSocket(udpPacket.getIP(),udpPacket.getPort()); // connectar o socket ao listenerWorker
            carrier.sendUDPPacket(socket,packets_send); // enviar as pieces que quero para o listenerWorker
            socket.disconnect();
            
            packets_send.clear();
            packets_receive = carrier.receiveUDPPacket(socket); // receber as pieces enviadas a partir do listenerWorker
            packets_receive.forEach(x -> this.file_buffer.set(x.getPiece().getPosition(),x.getData()));
        }

        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}