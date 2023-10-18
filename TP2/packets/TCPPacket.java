package packets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class TCPPacket{

    public static final int MAX_SIZE = 4096;
    public enum Protocol {HELLO, GET, EXIT, ACK};

    private Protocol protocol;
    private String IPsource;
    private String IPdest;
    private int Portsource;
    private int Portdest;
    private int length;
    private List<String> files;


    public TCPPacket(Protocol protocol, String IPsource, String IPdest, int Portsource, int Portdest, Collection<String> files){
        this.protocol = protocol;
        this.IPsource = IPsource;
        this.IPdest = IPdest;
        this.Portsource = Portsource;
        this.Portdest = Portdest;
        this.length = files.size();
        this.files = new ArrayList<String>(files);
    }


    public Protocol geProtocol(){
        return this.protocol;
    }


    public String getIPsource(){
        return this.IPsource;
    }


    public String getIPdest(){
        return this.IPdest;
    }


    public int getPortsource(){
        return this.Portsource;
    }


    public int getPortdest(){
        return this.Portdest;
    }


    public List<String> getFiles(){
        return this.files.stream().collect(Collectors.toList());
    }


    public byte[] serializeTCPPacket() throws IOException{

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeUTF(this.protocol.name());
        dataOutputStream.writeUTF(this.IPsource);
        dataOutputStream.writeUTF(this.IPdest);
        dataOutputStream.writeInt(this.Portsource);
        dataOutputStream.writeInt(this.Portdest);
        dataOutputStream.writeInt(this.length);

        for (String file : this.files){
            try {dataOutputStream.writeUTF(file);}
            catch (Exception e) {e.printStackTrace();}
        }

        dataOutputStream.flush();
        return byteArrayOutputStream.toByteArray();
    }


    public static TCPPacket deserializeTCPacket(byte[] data) throws IOException{

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        Protocol protocol = Protocol.valueOf(dataInputStream.readUTF());
        String IPsource = dataInputStream.readUTF();
        String IPdest = dataInputStream.readUTF();
        int Portsource = dataInputStream.readInt();
        int Portdest = dataInputStream.readInt();
        int length = dataInputStream.readInt();
        List<String> files = new ArrayList<String>();

        for (int p = 0; p < length; p++){
            files.add(dataInputStream.readUTF());
        }

        return new TCPPacket(protocol, IPsource, IPdest, Portsource, Portdest, files);
    }
}