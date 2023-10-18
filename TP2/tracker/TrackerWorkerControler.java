package tracker;
import java.util.ArrayList;
import java.util.List;
import packets.TCPPacket;
import packets.TCPPacket.Protocol;


public class TrackerWorkerControler{

    private TrackerContainer trackercontainer;


    public TrackerWorkerControler(TrackerContainer trackercontainer){
        this.trackercontainer = trackercontainer;
    }


    private void executeHELLO(TCPPacket tcpPacket){
        this.trackercontainer.put(tcpPacket);
    }


    private void executeEXIT(TCPPacket tcpPacket){
        this.trackercontainer.removeIPaddress(tcpPacket.getIPsource());
    }


    private List<String> executeGET(TCPPacket tcpPacket){
        return this.trackercontainer.getIPaddresses(tcpPacket.getFiles().get(0));
    }


    public TCPPacket execute(TCPPacket tcpPacket){

        List<String> files = new ArrayList<String>();

        switch (tcpPacket.geProtocol()){

            case HELLO:
                this.executeHELLO(tcpPacket);
                break;

            case GET:
                List<String> resutl = this.executeGET(tcpPacket);
                if (resutl != null) files.addAll(resutl);
                break;

            case EXIT:
                this.executeEXIT(tcpPacket);
                break;

            default:
                break;
        }

        return new TCPPacket(
            Protocol.ACK,
            tcpPacket.getIPdest(),
            tcpPacket.getIPsource(),
            tcpPacket.getPortdest(),
            tcpPacket.getPortsource(),
            files);
    }
}