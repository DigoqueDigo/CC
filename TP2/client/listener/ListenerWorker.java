package client.listener;
import java.io.FileInputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

    DatagramSocket socket;


    public ListenerWorker(String IPaddress, int Port) throws Exception{
        InetAddress address = InetAddress.getByName(IPaddress);
        this.socket = new DatagramSocket();
        this.socket.connect(address,Port);
    }


    private Map<PieceInfo,byte[]> getDataFromFile(String file) throws Exception{

        byte[] data = new byte[PieceInfo.SIZE];
        Map<PieceInfo,byte[]> result = new HashMap<PieceInfo,byte[]>();
        FileInputStream inputstream = new FileInputStream(Client.FOLDER + file);

        for (int bytes_read, p = 0; (bytes_read = Reader.read(inputstream,data,data.length)) > 0; p++){

            result.put(new PieceInfo(
                Arrays.copyOf(data,bytes_read),p,file),
                Arrays.copyOf(data,bytes_read));
        }

        return result;
    }


    public void run(){

        try{

            UDPCarrier carrier = UDPCarrier.getInstance();
            List<UDPPacket> packets = carrier.receiveUDPPacket(socket); // recebe todas as pieces que o cliente pretende adquirir
            Map<PieceInfo,byte[]> data = getDataFromFile(packets.get(0).getPiece().getFile());

            packets.clear();
            data.entrySet().forEach(x -> {
                UDPPacket udpPacket = new UDPPacket(UDPProtocol.DATA);
                udpPacket.setPiece(x.getKey());
                udpPacket.setData(x.getValue());
                packets.add(udpPacket);
            });

            carrier.sendUDPPacket(socket,packets); // enviar os dados das pieces para o cliente
        }

        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}