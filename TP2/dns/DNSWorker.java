package dns;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import carrier.DNSCarrier;
import packets.DNSPacket;


public class DNSWorker implements Runnable{
    
    private Socket socket;
    private DNSContainer container;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;


    public DNSWorker(Socket socket, DNSContainer container) throws IOException{
        this.socket = socket;
        this.container = container;
        this.inputStream = new DataInputStream(this.socket.getInputStream());
        this.outputStream = new DataOutputStream(this.socket.getOutputStream());
    }


    private void handlePut(DNSPacket dnsPacket){
        this.container.putDNSPacket(dnsPacket);
    }


    private void handleRemove(DNSPacket dnsPacket){
        this.container.removeDNSPacket(dnsPacket);
    }


    private DNSPacket handleRequest(DNSPacket dnsPacket){
        return this.container.resolveDNSPacket(dnsPacket);
    }


    public void run(){

        try{

            DNSPacket dnsPacket;
            DNSCarrier dnsCarrier = DNSCarrier.getInstance();

            while ((dnsPacket = dnsCarrier.receiveDNSPacket(inputStream)) != null){

                switch (dnsPacket.getProtocol()){

                    case HELLO:
                        this.handlePut(dnsPacket);
                        break;

                    case EXIT:
                        this.handleRemove(dnsPacket);
                        break;
                    
                    case REQUEST:
                        dnsCarrier.sendDNSPacket(outputStream,this.handleRequest(dnsPacket));
                        break;

                    default:
                        break;
                }

                System.out.println("------------------------");
                System.out.println(this.container);
                System.out.println("------------------------");
            }
        }

        catch (Exception e){

            try{
                this.outputStream.close();
                this.inputStream.close();
                this.socket.close();
            }

            catch (Exception f) {}
        }
    }
}