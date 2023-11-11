package packets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import carrier.Reader;
import packets.info.FileInfo;
import packets.info.PieceInfo;
import packets.messages.Message;
import packets.messages.ToClient;
import packets.messages.ToTracker;
import packets.messages.Message.TYPE;


public class TCPPacket{

    public enum Protocol {HELLO, GET, EXIT, HELLOACK, GETAK, EXITACK, ACK};

    private Protocol protocol;
    private String IPsource;
    private String IPdest;
    private int Portsource;
    private int Portdest;
    private Message<FileInfo,PieceInfo> Totracker;
    private Message<PieceInfo,String> Toclient;


    public TCPPacket(Protocol protocol, String IPsource, String IPdest, int Portsource, int Portdest, TYPE type){
        this.protocol = protocol;
        this.IPsource = IPsource;
        this.IPdest = IPdest;
        this.Portsource = Portsource;
        this.Portdest = Portdest;
        this.Toclient = null;
        this.Totracker = null;

        if (type == TYPE.TOCLIENT) this.Toclient = new ToClient();
        else this.Totracker = new ToTracker();
    }


    public Protocol getProtocol(){
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


    public TYPE getType(){
        if (this.Toclient != null) return this.Toclient.getType();
        return this.Totracker.getType();
    }


    public Message<PieceInfo,String> getToClient(){
        return this.Toclient;
    }


    public Message<FileInfo,PieceInfo> getToTracker(){
        return this.Totracker;
    }


    public void setToClient(Message<PieceInfo,String> Toclient){
        this.Toclient = Toclient;
    }


    public void setToTracker(Message<FileInfo,PieceInfo> Totracker){
        this.Totracker = Totracker;
    }


    public byte[] serializeTCPPacket() throws IOException{

        byte[] data_message;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeUTF(this.protocol.name());
        dataOutputStream.writeUTF(this.IPsource);
        dataOutputStream.writeUTF(this.IPdest);
        dataOutputStream.writeInt(this.Portsource);
        dataOutputStream.writeInt(this.Portdest);
        dataOutputStream.writeUTF(this.getType().name());

        if (this.Toclient != null) data_message = this.Toclient.serialize();
        else data_message = this.Totracker.serialize();

        dataOutputStream.writeInt(data_message.length);
        dataOutputStream.write(data_message);

        dataOutputStream.flush();
        return byteArrayOutputStream.toByteArray();
    }


    public static TCPPacket deserializeTCPacket(byte[] data) throws IOException{

        TCPPacket result;
        byte[] data_message;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        Protocol protocol = Protocol.valueOf(dataInputStream.readUTF());
        String IPsource = dataInputStream.readUTF();
        String IPdest = dataInputStream.readUTF();
        int Portsource = dataInputStream.readInt();
        int Portdest = dataInputStream.readInt();
        TYPE type = TYPE.valueOf(dataInputStream.readUTF());

        data_message = new byte[dataInputStream.readInt()];
        Reader.read(dataInputStream,data_message,data_message.length);

        result = new TCPPacket(protocol,IPsource,IPdest,Portsource,Portdest,type);

        if (type == TYPE.TOCLIENT) result.setToClient(ToClient.deserialize(data_message));
        else result.setToTracker(ToTracker.deserialize(data_message));

        return result;
    }


    public String toString(){

        StringBuilder buffer = new StringBuilder();

        buffer.append("Protocolo: ").append(this.protocol.toString());
        buffer.append("\nIP source: ").append(this.IPsource);
        buffer.append("\nIP dest: ").append(this.IPdest);
        buffer.append("\nPort source: ").append(this.Portsource);
        buffer.append("\nPort dest: ").append(this.Portdest);
        buffer.append("\npayload");

        if (this.Toclient != null) buffer.append("\n" + this.Toclient.toString());
        else buffer.append("\n" + this.Totracker.toString());

        return buffer.toString();
    }
}