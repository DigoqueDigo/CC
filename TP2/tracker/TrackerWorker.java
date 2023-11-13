package tracker;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import carrier.TCPCarrier;
import packets.TCPPacket;


public class TrackerWorker implements Runnable{

    private Socket socket;
    private TCPCarrier carrier;
    private DataInputStream inputstream;
    private DataOutputStream outputstream;
    private TrackerWorkerController trackerworkercontroller;


    public TrackerWorker(Socket socket, TrackerWorkerController trackerworkercontroller) throws IOException{
        this.socket = socket;
        this.carrier = TCPCarrier.getInstance();
        this.trackerworkercontroller = trackerworkercontroller;
        this.inputstream = new DataInputStream(socket.getInputStream());
        this.outputstream = new DataOutputStream(socket.getOutputStream());
    }


    public void run(){

        TCPPacket tcpPacket;

        try{

            while ((tcpPacket = carrier.receiveTCPPacket(inputstream)) != null){

                System.out.println(tcpPacket.toString());

                tcpPacket = this.trackerworkercontroller.execute(tcpPacket);

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