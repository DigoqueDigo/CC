package client;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import packets.TCPPacket;


public class ClientUI{

    private static final String RESET = "\033[0m";
    private static final String RED = "\033[1;31m";
    public static final String YELLOW_BOLD = "\033[1;33m";
    private BufferedReader scanner;


    public ClientUI(){
        this.scanner = new BufferedReader(new InputStreamReader(System.in));
    }


    public TCPPacket getHELLOTCPPacket(String folder, InetSocketAddress source, InetSocketAddress dest){
        return ClientUtils.getHELLOTCPPacket(folder,source,dest);
    }


    public TCPPacket getTCPPacket(InetSocketAddress source, InetSocketAddress dest){

        TCPPacket tcpPacket = null;

        while (tcpPacket == null){

            System.out.print(YELLOW_BOLD + ">>> " + RESET);

            try {
                String line = scanner.readLine();
                if (line != null && line.length() > 0){
                    tcpPacket = ClientUtils.getTCPPacket(line,source,dest);
                }
            }

            catch (Exception e){
                System.out.println(RED + "Comando inválido" + RESET);
            }
        }

        return tcpPacket;
    }
}
