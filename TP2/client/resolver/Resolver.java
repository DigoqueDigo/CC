package client.resolver;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import carrier.DNSCarrier;
import packets.DNSPacket;


public class Resolver{

    private static Resolver singleton = null;

    private Resolver() {}


    public static Resolver getInstance(){
        if (Resolver.singleton == null) Resolver.singleton = new Resolver();
        return Resolver.singleton;
    }


    public void send(DNSPacket dnsPacket, String address, int port){

        try{
            Socket socket = new Socket(address,port);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DNSCarrier.getInstance().sendDNSPacket(dataOutputStream,dnsPacket);
            socket.close();
        }

        catch (Exception e) {}
    }


    public DNSPacket resolve(DNSPacket dnsPacket, String address, int port){

        try{

            DNSPacket resultPacket;
            DNSCarrier dnsCarrier = DNSCarrier.getInstance();

            Socket socket = new Socket(address,port);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            dnsCarrier.sendDNSPacket(dataOutputStream,dnsPacket);
            resultPacket = dnsCarrier.receiveDNSPacket(dataInputStream);
            socket.close();

            return resultPacket;
        }

        catch (Exception e) {return null;}
    }
}