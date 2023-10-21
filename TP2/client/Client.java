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

    private Socket socket;
    private ClientUI clientUI;
    private final InetSocketAddress source;
    private final InetSocketAddress dest;
    private DataInputStream inputstream;
    private DataOutputStream outputstream;


    public Client(Socket socket, String folder) throws IOException{
        this.socket = socket;
        this.clientUI = new ClientUI(folder);
        this.source = (InetSocketAddress) socket.getLocalSocketAddress();
        this.dest = (InetSocketAddress) socket.getRemoteSocketAddress();
        this.inputstream = new DataInputStream(socket.getInputStream());
        this.outputstream = new DataOutputStream(socket.getOutputStream());
    }


    public void run() throws IOException{

        TCPPacket tcpPacket = this.clientUI.getHELLOTCPPacket(this.source,this.dest);

        System.out.println(tcpPacket.toString());

        Carrier.sendTCPPacket(outputstream,tcpPacket);

        try{

            while ((tcpPacket = Carrier.receiveTCPPacket(inputstream)) != null){

                System.out.println(tcpPacket.toString());

                // remover o if e acrescentar um metodo no pŕoximo comentário

                if (tcpPacket.getProtocol() == Protocol.EXITACK) throw new EOFException();

                // trabalhar o pacote acabado de receber

                tcpPacket = this.clientUI.getTCPPacket(this.source,this.dest);

                System.out.println(tcpPacket.toString());
                Carrier.sendTCPPacket(outputstream,tcpPacket);
            }
        }

        catch (EOFException e){
            this.inputstream.close();
            this.outputstream.close();
            this.socket.close();
        }

        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}