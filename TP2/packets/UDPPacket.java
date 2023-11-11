package packets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import carrier.Reader;
import packets.info.PieceInfo;


public class UDPPacket{

    public static final int MaxSize = 4096;

    public enum UDPProtocol {HELLO, GET, DATA, ACK};

    private UDPProtocol protocol;
    private String IP;
    private int Port;
    private int SeqNum;
    private PieceInfo piece;
    private byte[] data;


    public UDPPacket(UDPProtocol protocol){
        this.protocol = protocol;
        this.IP = "";
        this.Port = 0;
        this.SeqNum = 0;
        this.piece = null;
        this.data = new byte[0];        
    }


    private UDPPacket(UDPProtocol protocol, String IP, int Port, int SeqNum, PieceInfo piece, byte[] data){
        this.protocol = protocol;
        this.IP = IP;
        this.Port = Port;
        this.SeqNum = SeqNum;
        this.piece = (piece != null) ? piece.clone() : null;
        this.data = Arrays.copyOf(data,data.length);
    }


    public UDPPacket clone(){
        return new UDPPacket(
            this.protocol,
            this.IP,
            this.Port,
            this.SeqNum,
            this.piece,
            this.data
        );
    }


    public UDPProtocol getProtocol(){
        return this.protocol;
    }


    public String getIP(){
        return this.IP;
    }


    public int getPort(){
        return this.Port;
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


    public void setIP(String IP){
        this.IP = IP;
    }


    public void setPort(int Port){
        this.Port = Port;
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
        try {return this.piece.getHash().equals(PieceInfo.SHA1(this.data));}
        catch (Exception e) {return true;}
    }


    public String toString(){

        StringBuilder buffer = new StringBuilder();

        buffer.append("UDPProtocolo: ").append(this.protocol.name());
        buffer.append("\nIP source: ").append(this.IP);
        buffer.append("\nPort source: ").append(this.Port);
        buffer.append("\nSeqNum: ").append(this.SeqNum);
        buffer.append("\nPayload Size: ").append(this.data.length);
        if (this.piece != null) buffer.append("\n" + this.piece.toString());

        return buffer.toString();
    }


    public boolean equals(Object obj){
        if (obj == null || obj.getClass() != this.getClass()) return false;
        UDPPacket that = (UDPPacket) obj;
        return this.piece.equals(that.getPiece());
    }


    public int hashCode(){
        return this.piece.hashCode();
    }


    public byte[] serializeUDPPacket() throws IOException{

        byte[] data_piece;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataoutputStream = new DataOutputStream(byteArrayOutputStream);
        
        dataoutputStream.writeUTF(this.protocol.name());
        dataoutputStream.writeUTF(this.IP);
        dataoutputStream.writeInt(this.Port);
        dataoutputStream.writeInt(this.SeqNum);
        dataoutputStream.writeBoolean(this.piece != null);
        
        if (this.piece != null){
            data_piece = this.piece.serialize();
            dataoutputStream.writeInt(data_piece.length);
            dataoutputStream.write(data_piece);
        }

        dataoutputStream.write(this.data.length);
        dataoutputStream.write(this.data);

        return byteArrayOutputStream.toByteArray();
    }


    public static UDPPacket deserializeUDPPacket(byte[] data) throws IOException{

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        UDPProtocol protocol = UDPProtocol.valueOf(dataInputStream.readUTF());
        String IP = dataInputStream.readUTF();
        int Port = dataInputStream.readInt();
        int SeqNum = dataInputStream.readInt();
        PieceInfo piece = null;

        if (dataInputStream.readBoolean()){
            byte[] data_piece = new byte[dataInputStream.readInt()];
            Reader.read(dataInputStream, data_piece, data_piece.length);
            piece = PieceInfo.deserialize(data_piece);
        }

        byte[] data_message = new byte[dataInputStream.readInt()];
        Reader.read(dataInputStream,data_message,data_message.length);

        return new UDPPacket(protocol,IP,Port,SeqNum,piece,data_message);
    }
}