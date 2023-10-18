package client;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import packets.TCPPacket;


public class Client{

    private Socket socket;
    private DataInputStream inputstream;
    private DataOutputStream outputstream;
    private BufferedReader scanner;
    private ClientUI clientUI;


    public Client(Socket socket, String folder) throws IOException{
        this.socket = socket;
        this.clientUI = new ClientUI(new ClientUtils(folder,socket));
        this.inputstream = new DataInputStream(socket.getInputStream());
        this.outputstream = new DataOutputStream(socket.getOutputStream());
    }


    public void run() throws IOException{

        TCPPacket tcpPacket = this.clientUI.getHELLOPacket();
        byte[] request = tcpPacket.serializeTCPPacket();
        byte[] response = new byte[TCPPacket.MAX_SIZE];

        outputstream.writeInt(request.length);
        outputstream.write(request,0,request.length);
        outputstream.flush();

        try{

            for (int packet_size; (packet_size = inputstream.readInt()) > 0;){

                if (inputstream.read(response,0,packet_size) != packet_size){
                    throw new Exception("A leitura do pacote TCP não foi atómica");
                }

                tcpPacket = TCPPacket.deserializeTCPacket(response);
                // trabalar o pacote acabado de receber

                System.out.println(tcpPacket.toString());


                tcpPacket = this.clientUI.getUserInput();
                request = tcpPacket.serializeTCPPacket();

                outputstream.writeInt(request.length);
                outputstream.write(request,0,request.length);
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