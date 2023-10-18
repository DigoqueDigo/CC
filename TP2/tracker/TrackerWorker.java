package tracker;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import packets.TCPPacket;

public class TrackerWorker implements Runnable{

    private Socket socket;
    private DataInputStream inputstream;
    private DataOutputStream outputstream;
    private TrackerWorkerControler trackerworkercontroler;


    public TrackerWorker(Socket socket, TrackerWorkerControler trackerworkercontroler) throws IOException{
        this.socket = socket;
        this.trackerworkercontroler = trackerworkercontroler;
        this.inputstream = new DataInputStream(socket.getInputStream());
        this.outputstream = new DataOutputStream(socket.getOutputStream());
    }


    public void run(){

        try{
            
            byte[] data = new byte[TCPPacket.MAX_SIZE];

            for (int packet_size; (packet_size = inputstream.readInt()) > 0;){

                if (inputstream.read(data,0,packet_size) != packet_size){
                    throw new Exception("A leitura do pacote TCP não foi atómica");
                }

                TCPPacket tcpPacket = this.trackerworkercontroler.execute(TCPPacket.deserializeTCPacket(data));
                byte[] response = tcpPacket.serializeTCPPacket();

                outputstream.writeInt(response.length);;
                outputstream.write(response,0,response.length);
                outputstream.flush();
            }
        }

        catch (EOFException e){
            
            try{
                inputstream.close();
                outputstream.close();
                socket.close();
            }

            catch (Exception f) {f.printStackTrace();}
        }

        catch (Exception e) {e.printStackTrace();}
    }
}