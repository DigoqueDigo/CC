package packets.messages;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import packets.info.FileInfo;
import packets.info.PieceInfo;


public class ToTracker extends Message<FileInfo,PieceInfo>{

    private List<Map.Entry<FileInfo,List<PieceInfo>>> message;


    public ToTracker(){
        super(TYPE.TOTRACKER);
        this.message = new ArrayList<>();
    }


    public void put(FileInfo file, List<PieceInfo> pieces){
        this.message.add(
            new AbstractMap.SimpleEntry<FileInfo,List<PieceInfo>>(
                file,
                pieces.stream().collect(Collectors.toList())));
    }


    public int size(){
        return this.message.size();
    }


    public List<FileInfo> getKeys(){
        return this.message
                .stream()
                .map(x -> x.getKey())
                .collect(Collectors.toList());
    }


    public List<PieceInfo> getValue(FileInfo file){
        return this.message
                .stream()
                .filter(x -> x.getKey().equals(file))
                .map(x -> x.getValue().stream().collect(Collectors.toList()))
                .findFirst()
                .orElse(null);
    }


    public byte[] serialize() throws IOException{

        byte[] data_file, data_piece;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeInt(this.message.size());
        
        for (Map.Entry<FileInfo,List<PieceInfo>> entry : this.message){
            
            data_file = entry.getKey().serialize();
            dataOutputStream.writeInt(data_file.length);
            dataOutputStream.write(data_file);
            dataOutputStream.writeInt(entry.getValue().size());

            for (PieceInfo piece : entry.getValue()){
                data_piece = piece.serialize();
                dataOutputStream.writeInt(data_piece.length);
                dataOutputStream.write(data_piece);
            }
        }

        dataOutputStream.flush();
        return byteArrayOutputStream.toByteArray();  
    }


    public static ToTracker deserialize(byte[] data) throws IOException{

        byte[] data_file, data_piece;
        int length_pices, length_files;
        
        ToTracker result = new ToTracker();
        List<PieceInfo> list= new ArrayList<>();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        length_files = dataInputStream.readInt();

        for (int p = 0; p < length_files; p++, list.clear()){

            data_file = new byte[dataInputStream.readInt()];
            dataInputStream.read(data_file,0,data_file.length);
            FileInfo file = FileInfo.deserialize(data_file);

            length_pices = dataInputStream.readInt();

            for (int i = 0; i < length_pices; i++){

                data_piece = new byte[dataInputStream.readInt()];
                dataInputStream.read(data_piece,0,data_piece.length);
                list.add(PieceInfo.deserialize(data_piece));
            }

            result.put(file,list);
        }

        return result;
    }


    public String toString(){

        StringBuffer buffer = new StringBuffer("TYPE: " + super.getType().name() + "\n");

        this.message.forEach(x -> {
            buffer.append(x.getKey().toString());
            buffer.append(x.getValue().stream().map(y -> y.toString()).collect(Collectors.joining("\n", "\n", "\n")));
        });

        return buffer.toString();
    }
}