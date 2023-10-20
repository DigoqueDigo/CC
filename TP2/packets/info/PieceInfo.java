package packets.info;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import packets.Binary;


public class PieceInfo implements Binary{

    public static final int SIZE = 20;

    private int hash;
    private int position;


    public PieceInfo(int hash, int position){
        this.hash = hash;
        this.position = position;
    }


    public PieceInfo(byte[] data, int position){
        this.hash = Arrays.hashCode(data);
        this.position = position;
    }
    

    public int getHash(){
        return this.hash;
    }


    public int getPosition(int position){
        return this.position;
    }


    public boolean equals(Object obj){

        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        PieceInfo that = (PieceInfo) obj;
        return this.hash == that.getHash();
    }


    public int hashCode(){
        return this.hash;
    }


    public byte[] serialize() throws IOException{

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeInt(this.hash);
        dataOutputStream.writeInt(this.position);

        dataOutputStream.flush();
        return byteArrayOutputStream.toByteArray();  
    }


    public static PieceInfo deserialize(byte[] data) throws IOException{

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        return new PieceInfo(
            dataInputStream.readInt(),
            dataInputStream.readInt());
    }


    public String toString(){

        StringBuffer buffer = new StringBuffer();

        buffer.append("PieceInfo hash: ").append(this.hash);
        buffer.append("\tPieceInfo position: ").append(this.position);

        return buffer.toString();
    }
}