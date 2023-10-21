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

        TCPPacket tcpPacket;
        byte[] request, response;

        try{

            for (int packet_size; (packet_size = inputstream.readInt()) > 0;){

                request = new byte[packet_size];

                if (inputstream.read(request,0,packet_size) != packet_size){
                    throw new Exception("A leitura do pacote TCP não foi atómica");
                }

                System.out.println(TCPPacket.deserializeTCPacket(request).toString());

                tcpPacket = TCPPacket.deserializeTCPacket(request);
                tcpPacket = this.trackerworkercontroler.execute(tcpPacket);

                System.out.print(tcpPacket.toString());
                System.out.println("\n\n");

                response = tcpPacket.serializeTCPPacket();

                System.out.println(TCPPacket.deserializeTCPacket(response));
                System.out.println("\n\n");

                outputstream.writeInt(response.length);;
                outputstream.write(response,0,response.length);
                outputstream.flush();
            }
        }

        catch (EOFException e){

            try{
                System.out.println("FIM DA THREAD DO SERVIDOR");
                inputstream.close();
                outputstream.close();
                socket.close();
            }

            catch (Exception f) {f.printStackTrace();}
        }

        catch (Exception e) {e.printStackTrace();}
    }
}
