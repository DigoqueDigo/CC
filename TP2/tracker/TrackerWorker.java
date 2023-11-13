package tracker;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import carrier.Carrier;
import packets.TCPPacket;


public class TrackerWorker implements Runnable{

    private Socket socket;
    private Carrier carrier;
    private DataInputStream inputstream;
    private DataOutputStream outputstream;
    private TrackerWorkerControler trackerworkercontroler;


    public TrackerWorker(Socket socket, TrackerWorkerControler trackerworkercontroler) throws IOException{
        this.socket = socket;
        this.carrier = Carrier.getInstance();
        this.trackerworkercontroler = trackerworkercontroler;
        this.inputstream = new DataInputStream(socket.getInputStream());
        this.outputstream = new DataOutputStream(socket.getOutputStream());
    }


    public void run(){

        TCPPacket tcpPacket;

        try{

            while ((tcpPacket = carrier.receiveTCPPacket(inputstream)) != null){

                System.out.println(tcpPacket.toString());

                tcpPacket = this.trackerworkercontroler.execute(tcpPacket);

                System.out.print(tcpPacket.toString());

                carrier.sendTCPPacket(outputstream,tcpPacket);
            }
        }

        catch (EOFException e){

            try{
                System.out.println("Client disconnected");
                inputstream.close();
                outputstream.close();
                socket.close();
            }

            catch (Exception f) {f.printStackTrace();}
        }

        catch (Exception e) {e.printStackTrace();}
    }
}