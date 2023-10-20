package tracker;
import packets.TCPPacket;
import packets.TCPPacket.Protocol;
import packets.messages.ToClient;
import packets.messages.Message.TYPE;
import tracker.containers.TrackerContainer;


public class TrackerWorkerControler{

    private TrackerContainer trackercontainer;


    public TrackerWorkerControler(TrackerContainer trackercontainer){
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
        System.out.println("ANTES-------------------------------------------ANTES");
        System.out.println(this.trackercontainer.toString());
        System.out.println("ANTES-------------------------------------------ANTES");

        TCPPacket result;
        ToClient toClient = new ToClient();

        switch (tcpPacket.geProtocol()){

            case HELLO:
                this.executeHELLO(tcpPacket);
                break;

            case GET:
                toClient = this.executeGET(tcpPacket);
                break;

            case EXIT:
                this.executeEXIT(tcpPacket);
                break;

            default:
                break;
        }

        System.out.println("DEPOIS-------------------------------------------DEPOIS");
        System.out.println(this.trackercontainer.toString());
        System.out.println("DEPOIS-------------------------------------------DEPOIS");

        result = new TCPPacket(
                        Protocol.ACK,
                        tcpPacket.getIPdest(),
                        tcpPacket.getIPsource(),
                        tcpPacket.getPortdest(),
                        tcpPacket.getPortsource(),
                        TYPE.TOCLIENT);

        result.setToClient(toClient);
        return result;
    }
}