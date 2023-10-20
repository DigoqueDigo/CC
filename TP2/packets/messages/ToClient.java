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
import packets.info.PieceInfo;


public class ToClient extends Message<PieceInfo,String>{

    private List<Map.Entry<PieceInfo,List<String>>> message;


    public ToClient(){
        super(TYPE.TOCLIENT);
        this.message = new ArrayList<>();
    }

    
    public void put(PieceInfo piece, List<String> IPaddresses){
        this.message.add(
            new AbstractMap.SimpleEntry<PieceInfo,List<String>>(
                piece,
                IPaddresses.stream().collect(Collectors.toList())));
    }

    
    public int size(){
        return this.message.size();
    }


    public List<PieceInfo> getKeys(){
        return this.message
                    .stream()
                    .map(x -> x.getKey())
                    .collect(Collectors.toList());
    }


    public List<String> getValue(PieceInfo piece){
        return this.message
                    .stream()
                    .filter(x -> x.getKey().equals(piece))
                    .map(x -> x.getValue().stream().collect(Collectors.toList()))
                    .findFirst()
                    .orElse(null);
    }


    public byte[] serialize() throws IOException{

        byte[] data_piece;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeInt(this.message.size());
        
        for (Map.Entry<PieceInfo,List<String>> entry : this.message){

            data_piece = entry.getKey().serialize();
            dataOutputStream.writeInt(data_piece.length);
            dataOutputStream.write(data_piece);
            dataOutputStream.writeInt(entry.getValue().size());

            for (String IPaddress : entry.getValue()){
                dataOutputStream.writeUTF(IPaddress);
            }
        }

        dataOutputStream.flush();
        return byteArrayOutputStream.toByteArray();
    }


    public static ToClient deserialize(byte[] data) throws IOException{

        byte[] data_piece;
        int length_pices, length_addresses;

        ToClient result = new ToClient();
        List<String> list= new ArrayList<>();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        length_pices = dataInputStream.readInt();

        for (int p = 0; p < length_pices; p++, list.clear()){

            data_piece = new byte[dataInputStream.readInt()];
            dataInputStream.read(data_piece,0,data_piece.length);
            PieceInfo piece = PieceInfo.deserialize(data_piece);

            length_addresses = dataInputStream.readInt();

            for (int i = 0; i < length_addresses; i++){
                list.add(dataInputStream.readUTF());
            }

            result.put(piece,list);
        }

        return result;
    }


    public String toString(){

        StringBuffer buffer = new StringBuffer("TYPE: " + super.getType().name() + "\n");

        this.message.forEach(x -> {
            buffer.append(x.getKey().toString());
            buffer.append(x.getValue().stream().collect(Collectors.joining("\n", "\n", "\n")));
        });

        return buffer.toString();
    }
}
