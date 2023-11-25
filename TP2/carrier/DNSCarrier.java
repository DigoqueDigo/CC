package carrier;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import packets.DNSPacket;


public class DNSCarrier{

    private static DNSCarrier singleton = null;

    private DNSCarrier() {}


    public static DNSCarrier getInstance(){
        if (DNSCarrier.singleton == null) DNSCarrier.singleton = new DNSCarrier();
        return DNSCarrier.singleton;
    }


    public void sendDNSPacket(DataOutputStream outputStream, DNSPacket dnsPacket) throws IOException{
        byte[] message = dnsPacket.serialize();
        outputStream.writeInt(message.length);
        outputStream.write(message);
        outputStream.flush();
    }


    public DNSPacket receiveDNSPacket(DataInputStream inputStream) throws IOException{

        int messageSize = inputStream.readInt();
        byte[] message = new byte[messageSize];

        if (messageSize <= 0) throw new IOException();

        if (Reader.read(inputStream,message,messageSize) != messageSize){
            throw new IOException("DNS packet reading incomplete");
        }

        return DNSPacket.deserialize(message);
    }
}
