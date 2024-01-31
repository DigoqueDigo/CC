package dns;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class DNS{

    public static final int DefaultPort = 22222;

    public static void main(String[] args) throws IOException{

        Socket socket;
        ServerSocket serverSocket = new ServerSocket(DNS.DefaultPort);
        DNSContainer dnsContainer = new DNSContainer();

        while ((socket = serverSocket.accept()) != null){
            new Thread(new DNSWorker(socket,dnsContainer)).start();
        }

        serverSocket.close();
    }
}