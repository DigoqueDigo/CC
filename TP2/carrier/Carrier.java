package carrier;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import packets.TCPPacket;


public class Carrier{


    public static void sendTCPPacket(DataOutputStream outputstream, TCPPacket tcpPacket) throws IOException{
        byte[] message = tcpPacket.serializeTCPPacket();
        outputstream.writeInt(message.length);
        outputstream.write(message);
        outputstream.flush();
    }


    public static TCPPacket receiveTCPPacket(DataInputStream inputstream) throws Exception{
        int packet_size = inputstream.readInt();
        byte[] message = new byte[packet_size];
        
        if (message.length <= 0) throw new EOFException();

        if (Reader.read(inputstream,message,packet_size) != packet_size){
            throw new Exception("TCP packet reading incomplete");
        }

        return TCPPacket.deserializeTCPacket(message);
    }    
}