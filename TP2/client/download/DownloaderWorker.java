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
            UDPPacket packet = new UDPPacket(UDPProtocol.GET);
            packet.setPiece(x);
            packets.add(packet);
        });

        return packets;
    }


    public void run(){

        try{

            UDPCarrier carrier = UDPCarrier.getInstance();
            UDPPacket udpPacket = new UDPPacket(UDPProtocol.HELLO);
            ArrayList<UDPPacket> packets = new ArrayList<UDPPacket>();

            this.connectSocket(this.listenerAddress,Listener.DefaultPort);

            packets.add(udpPacket);
            carrier.sendUDPPacket(socket,packets); // envia para o listener

            packets = carrier.receiveUDPPacket(socket); // recebe do listenerWorker
            udpPacket = packets.get(0);

            this.connectSocket(udpPacket.getIP(),udpPacket.getPort()); // connectar o socket ao listenerWorker

            carrier.sendUDPPacket(socket,this.createUDPPacketsList(this.pieces)); // enviar as pieces que quero para o listenerWorker
            packets = carrier.receiveUDPPacket(socket); // receber as pieces enviadas a partir do listenerWorker

            packets.forEach(x -> this.file_buffer.set(x.getPiece().getPosition(),x.getData()));
        }

        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}