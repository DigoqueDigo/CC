package client.download;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import packets.TCPPacket;
import packets.info.PieceInfo;


public class DownloadSchedule{

    private Map<String,List<PieceInfo>> schedule;

    public DownloadSchedule(){
        this.schedule = new HashMap<String,List<PieceInfo>>();
    }
    
    private void addPieceInfo(String IPaddress, PieceInfo pieceInfo){
        this.schedule.putIfAbsent(IPaddress,new ArrayList<PieceInfo>());
        this.schedule.get(IPaddress).add(pieceInfo);
    }
    
    public void fillSchedule(TCPPacket tcpPacket, String filename){

        Random random = new Random();
    
        for (PieceInfo pieceInfo : tcpPacket.getToClient().getKeys()){

            if (pieceInfo.getFile().equals(filename)){

                List<String> IPaddresses = tcpPacket.getToClient().getValue(pieceInfo);
            
                this.addPieceInfo(
                    IPaddresses.get(random.nextInt(IPaddresses.size())),
                    pieceInfo
                );
            }
        }
    }

    public Set<Map.Entry<String,List<PieceInfo>>> entrySet(){
        return this.schedule.entrySet();
    }
}