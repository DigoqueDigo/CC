package client;
import java.io.EOFException;
import java.util.List;
import java.util.stream.Collectors;
import client.download.Downloader;
import packets.TCPPacket;


public class ClienteControler{

    private static List<String> getFilesName(TCPPacket tcpPacket){
        return tcpPacket.getToClient().getKeys().stream().map(x -> x.getFile()).distinct().collect(Collectors.toList());
    }

    public static void handler(TCPPacket tcpPacket) throws Exception{

        switch (tcpPacket.getProtocol()){

            case GETAK:

                for (String filename : ClienteControler.getFilesName(tcpPacket)){
                    new Thread(new Downloader(filename,tcpPacket)).start();
                }

                break;

            case EXITACK:
                throw new EOFException();
        
            default:
                break;
        }
    }
}
