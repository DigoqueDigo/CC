package packets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import carrier.Reader;
import packets.info.PieceInfo;


public class UDPPacket{

    public static final int MaxSize = 4096;

    public enum UDPProtocol {HELLO, GET, DATA, ACK};

    private UDPProtocol protocol;
    private String IPsource;
    private String IPdest;
    private int Portsource;
    private int Portdest;
    private int SeqNum;
    private PieceInfo piece;
    private byte[] data;


    public UDPPacket(UDPProtocol protocol, String IPsource, String IPdest, int Portsource, int Portdest){
        this.protocol = protocol;
        this.IPsource = IPsource;
        this.IPdest = IPdest;
        this.Portsource = Portsource;
        this.Portdest = Portdest;
        this.SeqNum = 0;
        this.piece = null;
        this.data = new byte[0];        
    }


    private UDPPacket(UDPProtocol protocol, String IPsource, String IPdest, int Portsource, int Portdest, int SeqNum, PieceInfo piece, byte[] data){
        this.protocol = protocol;
        this.IPsource = IPsource;
        this.IPdest = IPdest;
        this.Portsource = Portsource;
        this.Portdest = Portdest;
        this.SeqNum = SeqNum;
        this.piece = (piece != null) ? piece.clone() : null;
        this.data = Arrays.copyOf(data,data.length);
    }


    public UDPPacket clone(){
        return new UDPPacket(
            this.protocol,
            this.IPsource,
            this.IPdest,
            this.Portsource,
            this.Portdest,
            this.SeqNum,
            this.piece,
            this.data
        );
    }


    public UDPProtocol getProtocol(){
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


    public int getSeqNum(){
        return this.SeqNum;
    }

    
    public PieceInfo getPiece(){
        return this.piece;
    }


    public byte[] getData(){
        return Arrays.copyOf(this.data,this.data.length);
    }


    public void setIPsource(SocketAddress address){
        InetSocketAddress inetAddres = (InetSocketAddress) address;
        this.IPsource = inetAddres.getAddress().getHostAddress();
    }


    public void setSeqNum(int SeqNum){
        this.SeqNum = SeqNum;
    }


    public void setPiece(PieceInfo piece){
        this.piece = piece.clone();
    }


    public void setData(byte[] data){
        this.data = Arrays.copyOf(data,data.length);
    }


    public boolean checkSHA1(){
        
        try{
            return (this.piece != null && this.protocol == UDPProtocol.DATA) ?
                this.piece.getHash().equals(PieceInfo.SHA1(this.data)) :
                true;
        }

        catch (Exception e) {return false;}
    }


    public String toString(){

        StringBuilder buffer = new StringBuilder();

        buffer.append("UDPProtocolo: ").append(this.protocol.name());
        buffer.append("\nIP source: ").append(this.IPsource);
        buffer.append("\nIP dest: ").append(this.IPdest);
        buffer.append("\nPort source: ").append(this.Portsource);
        buffer.append("\nPort dest: ").append(this.Portdest);
        buffer.append("\nSeqNum: ").append(this.SeqNum);
        buffer.append("\nPayload Size: ").append(this.data.length);
        if (this.piece != null) buffer.append("\n" + this.piece.toString());

        return buffer.toString();
    }


    public boolean equals(Object obj){
        if (obj == null || obj.getClass() != this.getClass()) return false;
        UDPPacket that = (UDPPacket) obj;
        return this.SeqNum == that.getSeqNum();
    }


    public int hashCode(){
        return this.SeqNum;
    }


    public byte[] serializeUDPPacket() throws IOException{

        byte[] data_piece;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataoutputStream = new DataOutputStream(byteArrayOutputStream);
        
        dataoutputStream.writeUTF(this.protocol.name());
        dataoutputStream.writeUTF(this.IPsource);
        dataoutputStream.writeUTF(this.IPdest);
        dataoutputStream.writeInt(this.Portsource);
        dataoutputStream.writeInt(this.Portdest);
        dataoutputStream.writeInt(this.SeqNum);
        dataoutputStream.writeBoolean(this.piece != null);
        
        if (this.piece != null){
            data_piece = this.piece.serialize();
            dataoutputStream.writeInt(data_piece.length);
            dataoutputStream.write(data_piece);
        }

        dataoutputStream.writeInt(this.data.length);
        dataoutputStream.write(this.data);

        return byteArrayOutputStream.toByteArray();
    }


    public static UDPPacket deserializeUDPPacket(byte[] data) throws IOException{

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        UDPProtocol protocol = UDPProtocol.valueOf(dataInputStream.readUTF());
        String IPsource = dataInputStream.readUTF();
        String IPdest = dataInputStream.readUTF();
        int Portsource = dataInputStream.readInt();
        int Portdest = dataInputStream.readInt();
        int SeqNum = dataInputStream.readInt();
        PieceInfo piece = null;

        if (dataInputStream.readBoolean()){
            byte[] data_piece = new byte[dataInputStream.readInt()];
            Reader.read(dataInputStream, data_piece, data_piece.length);
            piece = PieceInfo.deserialize(data_piece);
        }

        byte[] data_message = new byte[dataInputStream.readInt()];
        Reader.read(dataInputStream,data_message,data_message.length);

        return new UDPPacket(protocol,IPsource,IPdest,Portsource,Portdest,SeqNum,piece,data_message);
    }
}