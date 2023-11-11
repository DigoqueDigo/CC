package client;
import java.io.EOFException;
import client.download.Downloader;
import packets.TCPPacket;


public class ClienteControler{

    public static void handler(TCPPacket tcpPacket) throws Exception{

        switch (tcpPacket.getProtocol()){

            case GETAK:

//                tcpPacket.getToClient().getKeys().stream().
                
                if (tcpPacket.getToClient().size() != 0){
                    new Thread(new Downloader(tcpPacket)).start();
                }
                
                break;

            case EXITACK:
                throw new EOFException();
        
            default:
                break;
        }
    }
}
