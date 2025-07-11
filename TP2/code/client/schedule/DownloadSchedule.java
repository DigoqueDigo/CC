package client.schedule;
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

    public void addPieceInfo(String HostName, PieceInfo pieceInfo){
        this.schedule.putIfAbsent(HostName,new ArrayList<PieceInfo>());
        this.schedule.get(HostName).add(pieceInfo);
    }

    public Set<Map.Entry<String,List<PieceInfo>>> entrySet(){
        return this.schedule.entrySet();
    }
}