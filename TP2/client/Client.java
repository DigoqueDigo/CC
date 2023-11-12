package client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import carrier.Carrier;
import packets.TCPPacket;
import packets.TCPPacket.Protocol;


public class Client{

    public static String FOLDER;

    private Socket socket;
    private ClientUI clientUI;
    private ClienteControler clienteControler; 
    private DataInputStream inputstream;
    private DataOutputStream outputstream;
    

    public Client(Socket socket, String folder) throws IOException{
        Client.FOLDER = folder;
        this.socket = socket;
        this.clientUI = new ClientUI();
        this.clienteControler = new ClienteControler();
        this.inputstream = new DataInputStream(socket.getInputStream());
        this.outputstream = new DataOutputStream(socket.getOutputStream());
    }


    public void run() throws IOException{

        Carrier carrier = Carrier.getInstance();
        InetSocketAddress source = (InetSocketAddress) socket.getLocalSocketAddress();
        InetSocketAddress dest = (InetSocketAddress) socket.getRemoteSocketAddress();
        TCPPacket tcpPacket = this.clientUI.getHELLOTCPPacket(source,dest);

        System.out.println(tcpPacket.toString());

        carrier.sendTCPPacket(outputstream,tcpPacket);

        try{

            while ((tcpPacket = carrier.receiveTCPPacket(inputstream)) != null){

                System.out.println(tcpPacket.toString());

                this.clienteControler.handler(tcpPacket);

                if (tcpPacket.getProtocol() == Protocol.GETAK){
                    tcpPacket = this.clientUI.getHELLOTCPPacket(source,dest);
                }

                else tcpPacket = this.clientUI.getTCPPacket(source,dest);

                System.out.println(tcpPacket.toString());
                carrier.sendTCPPacket(outputstream,tcpPacket);
            }
        }

        catch (EOFException e){
            this.inputstream.close();
            this.outputstream.close();
            this.socket.close();
        }

        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}