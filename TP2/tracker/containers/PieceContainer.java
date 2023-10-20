package tracker.containers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import packets.info.PieceInfo;


public class PieceContainer{

    private Map<PieceInfo,List<String>> container;


    public PieceContainer(){
        this.container = new HashMap<PieceInfo,List<String>>();
    }


    public void put(PieceInfo piece, String IPaddress){
        this.container.putIfAbsent(piece,new ArrayList<String>());
        this.container.get(piece).add(IPaddress);
    }


    public int size(){
        return this.container.size();
    }


    public List<PieceInfo> getKeys(){
        return this.container.keySet().stream().collect(Collectors.toList());
    }


    public List<String> getValue(PieceInfo piece){
        List<String> result = this.container.get(piece);
        if (result != null) result = result.stream().collect(Collectors.toList());
        return result;
    }


    public void removeClient(String IPaddress){
        this.container.values().forEach(x -> x.remove(IPaddress));
        this.container.entrySet().stream().forEach(x -> {
            if (x.getValue().size() == 0) this.container.remove(x.getKey());
        });
    }


    public String toString(){

        StringBuffer buffer = new StringBuffer();

        this.container.entrySet().forEach(x -> {
                buffer.append(x.getKey().toString() + "\n");
                x.getValue().forEach(y -> buffer.append(y + "\n"));
        });

        return buffer.toString();
    }
}
