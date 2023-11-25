package client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import carrier.TCPCarrier;
import client.resolver.Resolver;
import packets.DNSPacket;
import packets.TCPPacket;
import packets.TCPPacket.TCPProtocol;


public class Client{

    public static String FOLDER;
    public static String DNSAddress;
    public static int DNSPort;

    private Socket socket;
    private ClientUI clientUI;
    private ClientController clientController; 
    private DataInputStream inputstream;
    private DataOutputStream outputstream;
    

    public Client(Socket socket, String folder, String dnsAddress, int dnsPort) throws IOException{
        Client.FOLDER = folder;
        Client.DNSAddress = dnsAddress;
        Client.DNSPort = dnsPort;
        this.socket = socket;
        this.clientUI = new ClientUI();
        this.clientController = new ClientController();
        this.inputstream = new DataInputStream(socket.getInputStream());
        this.outputstream = new DataOutputStream(socket.getOutputStream());
    }


    public void run() throws IOException{

        TCPCarrier carrier = TCPCarrier.getInstance();
        Resolver resolver = Resolver.getInstance();

        InetSocketAddress source = (InetSocketAddress) socket.getLocalSocketAddress();
        InetSocketAddress dest = (InetSocketAddress) socket.getRemoteSocketAddress();
        
        DNSPacket dnsPacket = this.clientUI.getHELLODNSPacket(source);
        TCPPacket tcpPacket = this.clientUI.getHELLOTCPPacket(source,dest);

        carrier.sendTCPPacket(outputstream,tcpPacket);
        resolver.send(dnsPacket,Client.DNSAddress,Client.DNSPort);

        try{

            while ((tcpPacket = carrier.receiveTCPPacket(inputstream)) != null){

                this.clientController.handler(tcpPacket);

                if (tcpPacket.getProtocol() == TCPProtocol.GETAK){
                    tcpPacket = this.clientUI.getHELLOTCPPacket(source,dest);
                }

                else tcpPacket = this.clientUI.getTCPPacket(source,dest);

                carrier.sendTCPPacket(outputstream,tcpPacket);
            }
        }

        catch (EOFException e){
            this.inputstream.close();
            this.outputstream.close();
            this.socket.close();
            resolver.send(this.clientUI.getEXIDNSPacket(),DNSAddress,DNSPort);
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }
}