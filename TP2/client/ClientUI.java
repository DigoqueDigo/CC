package client;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import packets.TCPPacket;


public class ClientUI{

    private static final String RESET = "\033[0m";
    private static final String RED = "\033[1;31m";
    public static final String YELLOW_BOLD = "\033[1;33m";

    private ClientUtils clientUtils;
    private BufferedReader scanner;


    public ClientUI(ClientUtils clientUtils){
        this.clientUtils = clientUtils;
        this.scanner = new BufferedReader(new InputStreamReader(System.in));
    }


    public TCPPacket getHELLOPacket(){
        return this.clientUtils.createTCPPacket("HELLO");
    }


    public TCPPacket getUserInput(){

        TCPPacket tcpPacket = null;

        while (tcpPacket == null){

            System.out.print(YELLOW_BOLD + ">>> " + RESET);

            try {tcpPacket = this.clientUtils.createTCPPacket(scanner.readLine());}

            catch (Exception e){
                System.out.println(RED + "Comando inválido" + RESET);
            }
        }

        return tcpPacket;
    }
}
