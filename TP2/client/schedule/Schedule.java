package client.schedule;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import packets.TCPPacket;
import packets.info.PieceInfo;


public class Schedule{

    private Map<String,DownloadSchedule> shcedule;

    public Schedule(){
        this.shcedule = new HashMap<String,DownloadSchedule>();
    }

    public void fillSchedule(TCPPacket tcpPacket){

        Random random = new Random();

        for (PieceInfo piece : tcpPacket.getToClient().getKeys()){

            List<String> IPaddresses = tcpPacket.getToClient().getValue(piece);

            this.shcedule.putIfAbsent(piece.getFile(),new DownloadSchedule());
            this.shcedule.get(piece.getFile()).addPieceInfo(
                IPaddresses.get(random.nextInt(IPaddresses.size())),
                piece);
        }
    }

    public List<String> getKeys(){
        return this.shcedule.keySet().stream().collect(Collectors.toList());
    }

    public DownloadSchedule getValue(String key){
        return this.shcedule.get(key);
    }
}