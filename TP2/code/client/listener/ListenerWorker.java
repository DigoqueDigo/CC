package client.listener;
import java.io.FileInputStream;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import carrier.Reader;
import carrier.UDPCarrier;
import client.Client;
import packets.UDPPacket;
import packets.UDPPacket.UDPProtocol;
import packets.info.PieceInfo;


public class ListenerWorker implements Runnable{

    private DatagramSocket socket;
    private String IPdest;
    private int Portdest;


    public ListenerWorker(String IPaddress, int Port) throws Exception{
        this.socket = new DatagramSocket();
        this.IPdest = IPaddress;
        this.Portdest = Port;
    }


    private Map<PieceInfo,byte[]> getDataFromFile(String file, List<PieceInfo> pieces) throws Exception{

        int index;
        byte[] data = new byte[PieceInfo.SIZE];
        Map<PieceInfo,byte[]> result = new HashMap<PieceInfo,byte[]>();
        FileInputStream inputstream = new FileInputStream(Client.FOLDER + file);

        for (int bytes_read, p = 0; (bytes_read = Reader.read(inputstream,data,data.length)) > 0; p++){

            PieceInfo piece = new PieceInfo(Arrays.copyOf(data,bytes_read),p,file);

            if ((index = pieces.indexOf(piece)) != -1){

                piece.setPosition(pieces.get(index).getPosition());
                result.put(piece,Arrays.copyOf(data,bytes_read));
            }
        }

        inputstream.close();
        return result;
    }


    public void run(){

        try{

            UDPPacket packet;
            Map<PieceInfo,byte[]> fileData;
            UDPCarrier carrier = UDPCarrier.getInstance();
            List<UDPPacket> packets_receive = new ArrayList<>();
            List<UDPPacket> packets_send = new ArrayList<>();

            packet = new UDPPacket(
                UDPProtocol.HELLO,
                this.socket.getLocalAddress().getHostAddress(),
                this.IPdest,
                this.socket.getLocalPort(),
                this.Portdest
            );

            packets_send.add(packet);
            carrier.sendUDPPacket(socket,packets_send);
            packets_send.clear();

            while (packets_receive.size() == 0){
                packets_receive = carrier.receiveUDPPacket(socket);
            }

            fileData = getDataFromFile(
                packets_receive.get(0).getPiece().getFile(),
                packets_receive.stream().map(x -> x.getPiece()).collect(Collectors.toList())
            );

            for (UDPPacket element : packets_receive){

                UDPPacket udpPacket = new UDPPacket(
                    UDPProtocol.DATA,
                    this.socket.getLocalAddress().getHostAddress(),
                    this.IPdest,
                    this.socket.getLocalPort(),
                    this.Portdest
                );

                udpPacket.setPiece(element.getPiece());
                udpPacket.setData(fileData.get(udpPacket.getPiece()));
                packets_send.add(udpPacket);
            }

            carrier.sendUDPPacket(socket,packets_send);
            packets_send.clear();
            packets_receive.clear();
            this.socket.close();
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }
}