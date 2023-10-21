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
    private String file;


    public PieceInfo(int hash, int position, String file){
        this.hash = hash;
        this.position = position;
        this.file = file;
    }


    public PieceInfo(byte[] data, int position, String file){
        this.hash = Arrays.hashCode(data);
        this.position = position;
        this.file = file;
    }


    public int getHash(){
        return this.hash;
    }


    public int getPosition(){
        return this.position;
    }


    public String getFile(){
        return this.file;
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
        dataOutputStream.writeUTF(this.file);

        dataOutputStream.flush();
        return byteArrayOutputStream.toByteArray();
    }


    public static PieceInfo deserialize(byte[] data) throws IOException{

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        return new PieceInfo(
            dataInputStream.readInt(),
            dataInputStream.readInt(),
            dataInputStream.readUTF());
    }


    public String toString(){

        StringBuffer buffer = new StringBuffer();

        buffer.append("PieceInfo hash: ").append(this.hash);
        buffer.append("\tPieceInfo position: ").append(this.position);
        buffer.append("\tFile: ").append(this.file);

        return buffer.toString();
    }
}
