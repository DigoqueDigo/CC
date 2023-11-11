package client.listener;
import java.io.FileInputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import carrier.Reader;
import carrier.UDPCarrier;
import client.Client;
import packets.UDPPacket;
import packets.UDPPacket.UDPProtocol;
import packets.info.PieceInfo;


public class ListenerWorker implements Runnable{

    private DatagramSocket socket;
    private InetAddress address;
    private int Port;


    public ListenerWorker(String IPaddress, int Port) throws Exception{
        this.socket = new DatagramSocket();
        this.address = InetAddress.getByName(IPaddress);
        this.Port = Port;
    }


    private Map<PieceInfo,byte[]> getDataFromFile(String file) throws Exception{

        byte[] data = new byte[PieceInfo.SIZE];
        Map<PieceInfo,byte[]> result = new HashMap<PieceInfo,byte[]>();
        FileInputStream inputstream = new FileInputStream(Client.FOLDER + file);

        for (int bytes_read, p = 0; (bytes_read = Reader.read(inputstream,data,data.length)) > 0; p++){

            result.put(
                new PieceInfo(Arrays.copyOf(data,bytes_read),p,file),
                Arrays.copyOf(data,bytes_read)
            );
        }

        return result;
    }


    public void run(){

        try{
            
            Map<PieceInfo,byte[]> data;
            UDPCarrier carrier = UDPCarrier.getInstance();
            List<UDPPacket> packets_receive = new ArrayList<>();
            List<UDPPacket> packets_send = new ArrayList<>();
            UDPPacket packet = new UDPPacket(UDPProtocol.HELLO); 
            
            packets_send.add(packet);

            System.out.println("LISTERNERWORKER -> DOWNLOADWORKER" + this.address + this.Port);
            System.out.println(packet.toString());

            socket.connect(this.address,this.Port);
            carrier.sendUDPPacket(socket,packets_send); // enviar o hello para o downloadworker
            socket.disconnect();
            packets_send.clear();

            packets_receive = carrier.receiveUDPPacket(socket); // recebe todas as pieces que o cliente pretende adquirir

            System.out.println("LISTERNERWORKER <- DOWNLOADWORKER");
            System.out.println(packets_receive.size());

            if (packets_receive.get(0).getProtocol() != UDPProtocol.GET) throw new Exception("LISTENER WORKER NOT RECEIVE GET PROTOCOL");

            data = getDataFromFile(packets_receive.get(0).getPiece().getFile());

            for (UDPPacket element : packets_receive){

                UDPPacket udpPacket = new UDPPacket(UDPProtocol.DATA);
                udpPacket.setPiece(element.getPiece());
                udpPacket.setData(data.get(udpPacket.getPiece()));
                packets_send.add(udpPacket);
            }

            carrier.sendUDPPacket(socket,packets_send); // enviar os dados das pieces para o cliente
            socket.disconnect();
        }

        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}