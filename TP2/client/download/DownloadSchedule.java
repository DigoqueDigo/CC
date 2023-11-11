package client.download;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import packets.info.PieceInfo;


public class DownloadSchedule{

    private Map<String,List<PieceInfo>> schedule;

    public DownloadSchedule(){
        this.schedule = new HashMap<String,List<PieceInfo>>();
    }

    public void addPieceInfo(String IPaddress, PieceInfo pieceInfo){
        this.schedule.putIfAbsent(IPaddress,new ArrayList<PieceInfo>());
        this.schedule.get(IPaddress).add(pieceInfo);
    }

    public Set<Map.Entry<String,List<PieceInfo>>> entrySet(){
        return this.schedule.entrySet();
    }

    public int size(){
        return this.schedule.size();
    }

    public int getNumberOfPieces(){
        return this.schedule.values().stream().mapToInt(x -> x.size()).sum();
    }
}