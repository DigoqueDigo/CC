package client.listener;
import java.io.FileInputStream;
import java.net.DatagramSocket;
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
    private String IPdest;
    private int Portdest;


    public ListenerWorker(String IPaddress, int Port) throws Exception{
        this.socket = new DatagramSocket();
        this.IPdest = IPaddress;
        this.Portdest = Port;
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
            
            System.out.println("LISTERNERWORKER -> DOWNLOADWORKER");
            System.out.println(packet);


            carrier.sendUDPPacket(socket,packets_send); // enviar o hello para o downloadworker
            packets_send.clear();

            while (packets_receive.size() == 0){
                packets_receive = carrier.receiveUDPPacket(socket); // recebe todas as pieces que o cliente pretende adquirir
            }

            System.out.println("LISTENERWORKER <- DOWNLOADWORKER");
            System.out.println("------------------------------------------------");

            packets_receive.forEach(x -> System.out.println(x));
            System.out.println("------------------------------------------------");


            System.out.println("------------------------------------------------");
            System.out.println("LISTENERWORKER -> DOWNLOADWORKER");

            fileData = getDataFromFile(packets_receive.get(0).getPiece().getFile());

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
                System.out.println(udpPacket);
            }

            System.out.println("------------------------------------------------");

            carrier.sendUDPPacket(socket,packets_send); // enviar os dados das pieces para o cliente
            packets_send.clear();
            packets_receive.clear();
            this.socket.close();
        }

        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}