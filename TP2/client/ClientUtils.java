package client;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import packets.TCPPacket;
import packets.TCPPacket.Protocol;


public class ClientUtils{

    private String folder;
    private Socket socket;


    public ClientUtils(String folder, Socket socket){
        this.folder = folder;
        this.socket = socket;
    }


    public List<String> getFiles(){

        return Stream.of(new File(this.folder).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
    }


    public TCPPacket createTCPPacket(String line){

        String[] tokens = line.split(" ");
        List<String> files = new ArrayList<String>();

        switch (Protocol.valueOf(tokens[0])){

            case GET:
                Stream.of(tokens).forEach(x -> files.add(x));
                files.remove(0);
                break;

            case HELLO:
                files.addAll(this.getFiles());
                break;

            default:
                break;
        }

        InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();

        return new TCPPacket(
            Protocol.valueOf(tokens[0]),
            socket.getLocalAddress().getHostAddress(),
            address.getAddress().getHostAddress(),
            socket.getLocalPort(),
            address.getPort(),
            files);
    }
}
