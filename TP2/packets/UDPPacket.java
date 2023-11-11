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
        this.piece = new PieceInfo();
        this.data = new byte[0];        
    }


    public UDPPacket(UDPProtocol protocol, String IP, int Port, PieceInfo piece, byte[] data){
        this.protocol = protocol;
        this.IP = IP;
        this.Port = Port;
        this.SeqNum = 0;
        this.piece = piece.clone();
        this.data = Arrays.copyOf(data,data.length);
    }


    public UDPPacket clone(){
        return new UDPPacket(
            this.protocol,
            this.IP,
            this.Port,
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


    public void setProtocol(UDPProtocol protocol){
        this.protocol = protocol;
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
        catch (Exception e) {return false;}
    }


    public byte[] serializeUDPPacket() throws IOException{

        byte[] data_piece;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataoutputStream = new DataOutputStream(byteArrayOutputStream);

        data_piece = this.piece.serialize();

        dataoutputStream.writeUTF(this.protocol.name());
        dataoutputStream.writeUTF(this.IP);
        dataoutputStream.writeInt(this.Port);
        dataoutputStream.writeInt(data_piece.length);
        dataoutputStream.write(data_piece);
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

        byte[] data_piece = new byte[dataInputStream.readInt()];
        Reader.read(dataInputStream, data_piece, data_piece.length);

        byte[] data_message = new byte[dataInputStream.readInt()];
        Reader.read(dataInputStream,data_message,data_message.length);

        return new UDPPacket(protocol,IP,Port,PieceInfo.deserialize(data_piece),data_message);
    }
}