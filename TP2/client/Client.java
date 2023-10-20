package client;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import packets.TCPPacket;


public class Client{

    private Socket socket;
    private String folder;
    private ClientUI clientUI;
    private final InetSocketAddress source;
    private final InetSocketAddress dest;
    private DataInputStream inputstream;
    private DataOutputStream outputstream;
    private BufferedReader scanner;


    public Client(Socket socket, String folder) throws IOException{
        this.socket = socket;
        this.folder = folder;
        this.clientUI = new ClientUI();
        this.source = (InetSocketAddress) socket.getLocalSocketAddress();
        this.dest = (InetSocketAddress) socket.getRemoteSocketAddress();
        this.inputstream = new DataInputStream(socket.getInputStream());
        this.outputstream = new DataOutputStream(socket.getOutputStream());
    }


    public void run() throws IOException{

        TCPPacket tcpPacket = this.clientUI.getHELLOTCPPacket(this.folder,this.source,this.dest);

        System.out.println(tcpPacket.toString());

        byte[] response, request = tcpPacket.serializeTCPPacket();

        System.out.println(TCPPacket.deserializeTCPacket(request).toString());

        outputstream.writeInt(request.length);
        outputstream.write(request);
        outputstream.flush();

        try{

            for (int packet_size; (packet_size = inputstream.readInt()) > 0;){

                response = new byte[packet_size];

                if (inputstream.read(response,0,packet_size) != packet_size){
                    throw new Exception("A leitura do pacote TCP não foi atómica");
                }

                tcpPacket = TCPPacket.deserializeTCPacket(response);
                System.out.println(tcpPacket.toString());

                // trabalhar o pacote acabado de receber

                tcpPacket = this.clientUI.getTCPPacket(this.source,this.dest);
                request = tcpPacket.serializeTCPPacket();

                System.out.println(tcpPacket.toString());

                outputstream.writeInt(request.length);
                outputstream.write(request);
                outputstream.flush();
            }
        }

        catch (EOFException e){
            this.scanner.close();
            this.inputstream.close();
            this.outputstream.close();
            this.socket.close();
        }

        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}