package tracker;
import dns.DNSUtils;
import packets.TCPPacket;
import packets.TCPPacket.TCPProtocol;
import packets.messages.ToClient;
import packets.messages.Message.TYPE;
import tracker.containers.TrackerContainer;


public class TrackerWorkerController{

    private TrackerContainer trackercontainer;


    public TrackerWorkerController(TrackerContainer trackercontainer){
        this.trackercontainer = trackercontainer;
    }


    private void executeHELLO(TCPPacket tcpPacket){
        this.trackercontainer.put(tcpPacket);
    }


    private void executeEXIT(TCPPacket tcpPacket){
        this.trackercontainer.removeClient(tcpPacket);
    }


    private ToClient executeGET(TCPPacket tcpPacket){
        return this.trackercontainer.getPieces(tcpPacket);
    }


    public TCPPacket execute(TCPPacket tcpPacket){
        System.out.println("BEFORE-------------------------------------------BEFORE");
        System.out.println(this.trackercontainer.toString());
        System.out.println("BEFORE-------------------------------------------BEFORE");

        TCPPacket result;
        TCPProtocol protocol;
        ToClient toClient = new ToClient();

        switch (tcpPacket.getProtocol()){

            case HELLO:
                protocol = TCPProtocol.HELLOACK;
                this.executeHELLO(tcpPacket);
                break;

            case GET:
                protocol = TCPProtocol.GETAK;
                toClient = this.executeGET(tcpPacket);
                break;

            case EXIT:
                protocol = TCPProtocol.EXITACK;
                this.executeEXIT(tcpPacket);
                break;

            default:
                protocol = TCPProtocol.ACK;
                break;
        }

        System.out.println("AFTER-------------------------------------------AFTER");
        System.out.println(this.trackercontainer.toString());
        System.out.println("AFTER-------------------------------------------AFTER");

        result = new TCPPacket(
            protocol,
            DNSUtils.getInstance().getHostName(),
            tcpPacket.getIPdest(),
            tcpPacket.getIPsource(),
            tcpPacket.getPortdest(),
            tcpPacket.getPortsource(),
            TYPE.TOCLIENT);

        result.setToClient(toClient);
        return result;
    }
}