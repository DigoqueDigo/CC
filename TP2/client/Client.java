package client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import carrier.Carrier;
import client.listener.Listener;
import packets.TCPPacket;
import packets.TCPPacket.Protocol;


public class Client{

    public static String FOLDER;

    private Socket socket;
    private ClientUI clientUI;
    private DatagramSocket listener; 
    private DataInputStream inputstream;
    private DataOutputStream outputstream;


    public Client(Socket socket, String folder) throws IOException{
        Client.FOLDER = folder;
        this.socket = socket;
        this.clientUI = new ClientUI();
        this.listener = new DatagramSocket(Listener.DefaultPort);
        this.inputstream = new DataInputStream(socket.getInputStream());
        this.outputstream = new DataOutputStream(socket.getOutputStream());
    }


    public void run() throws IOException{

        Carrier carrier = Carrier.getInstance();
        InetSocketAddress source = (InetSocketAddress) socket.getLocalSocketAddress();
        InetSocketAddress dest = (InetSocketAddress) socket.getRemoteSocketAddress();

        new Thread(new Listener(listener)).start();
        TCPPacket tcpPacket = this.clientUI.getHELLOTCPPacket(source,dest);

        System.out.println(tcpPacket.toString());

        carrier.sendTCPPacket(outputstream,tcpPacket);

        try{

            while ((tcpPacket = carrier.receiveTCPPacket(inputstream)) != null){

                System.out.println(tcpPacket.toString());

                ClienteControler.handler(tcpPacket);

                if (tcpPacket.getProtocol() == Protocol.EXITACK) throw new EOFException();

                tcpPacket = this.clientUI.getTCPPacket(source,dest);

                System.out.println(tcpPacket.toString());
                carrier.sendTCPPacket(outputstream,tcpPacket);
            }
        }

        catch (EOFException e){
            this.inputstream.close();
            this.outputstream.close();
            this.socket.close();
            this.listener.close();
        }

        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}