package packets.info;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import packets.Binary;


public class FileInfo implements Binary{

    private String name;
    private long size;


    public FileInfo(String name, long size){
        this.name = name;
        this.size = size;
    }


    public String getName(){
        return this.name;
    }


    public long getSize(){
        return this.size;
    }


    public boolean equals(Object obj){

        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        FileInfo that = (FileInfo) obj;
        return this.name.equals(that.getName());
    }


    public int hashCode(){
        return this.name.hashCode();
    }


    public byte[] serialize() throws IOException{

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeUTF(this.name);
        dataOutputStream.writeLong(this.size);

        dataOutputStream.flush();
        return byteArrayOutputStream.toByteArray();
    }


    public static FileInfo deserialize(byte[] data) throws IOException{

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        return new FileInfo(
            dataInputStream.readUTF(),
            dataInputStream.readLong());
    }


    public String toString(){

        StringBuffer buffer = new StringBuffer();

        buffer.append("FileInfo name: ").append(this.name);
        buffer.append("\tFileInfo size: ").append(this.size);

        return buffer.toString();
    }
}
