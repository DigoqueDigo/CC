package packets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class DNSPacket implements Binary{

    public enum DNSProtocol {HELLO, REQUEST, RESPONSE, EXIT, ERROR};

    private DNSProtocol protocol;
    private String hostname;
    private String address;


    public DNSPacket(DNSProtocol protocol, String hostname){
        this.protocol = protocol;
        this.hostname = hostname;
        this.address = "";
    }


    public DNSPacket(DNSProtocol protocol, String hostname, String address){
        this.protocol = protocol;
        this.hostname = hostname;
        this.address = address;
    }


    public DNSProtocol getProtocol(){
        return this.protocol;
    }


    public String getHostName(){
        return this.hostname;
    }


    public String getAddress(){
        return this.address;
    }


    public byte[] serialize() throws IOException{

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeUTF(this.protocol.name());
        dataOutputStream.writeUTF(this.hostname);
        dataOutputStream.writeUTF(this.address);
        dataOutputStream.flush();

        return byteArrayOutputStream.toByteArray();
    }


    public static DNSPacket deserialize(byte[] data) throws IOException{

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        DNSProtocol protocol = DNSProtocol.valueOf(dataInputStream.readUTF());
        String hostname = dataInputStream.readUTF();
        String address = dataInputStream.readUTF();

        return new DNSPacket(protocol,hostname,address);
    }
}
