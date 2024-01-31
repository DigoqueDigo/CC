package client;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.file.FileAlreadyExistsException;
import dns.DNSUtils;
import packets.DNSPacket;
import packets.TCPPacket;
import packets.DNSPacket.DNSProtocol;


public class ClientUI{

    public static final String RESET = "\033[0m";
    public static final String RED = "\033[1;31m";
    public static final String YELLOW_BOLD = "\033[1;33m";

    private ClientUtils clientUtils;
    private BufferedReader scanner;


    public ClientUI(){
        this.clientUtils = ClientUtils.getInstance();
        this.scanner = new BufferedReader(new InputStreamReader(System.in));
    }


    public TCPPacket getHELLOTCPPacket(InetSocketAddress source, InetSocketAddress dest){
        return this.clientUtils.getHELLOTCPPacket(source,dest);
    }


    public DNSPacket getHELLODNSPacket(InetSocketAddress source){
        return new DNSPacket(DNSProtocol.HELLO,DNSUtils.getInstance().getHostName(),source.getAddress().getHostAddress());
    }


    public DNSPacket getEXIDNSPacket(){
        return new DNSPacket(DNSProtocol.EXIT,DNSUtils.getInstance().getHostName());
    }


    public TCPPacket getTCPPacket(InetSocketAddress source, InetSocketAddress dest){

        TCPPacket tcpPacket = null;

        while (tcpPacket == null){

            System.out.print(YELLOW_BOLD + ">>> " + RESET);

            try {
                String line = scanner.readLine();
                if (line != null && line.length() > 0){
                    tcpPacket = this.clientUtils.getTCPPacket(line,source,dest);
                }
            }

            catch (FileAlreadyExistsException e){
                System.out.println(RED + "File already acquired: " + e.getMessage() + RESET);
            }

            catch (Exception e){
                System.out.println(RED + "Invalid command" + RESET);
            }
        }

        return tcpPacket;
    }
}
