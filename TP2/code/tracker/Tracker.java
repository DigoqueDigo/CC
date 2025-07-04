package tracker;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import tracker.containers.TrackerContainer;


public class Tracker{

    public static final int DefaultPort = 12345;

    public static void main(String[] args) throws IOException{

        Socket socket;
        ServerSocket serversocket = new ServerSocket(DefaultPort);
        TrackerContainer trackercontainer = new TrackerContainer();
        TrackerWorkerController trackerworkercontroller = new TrackerWorkerController(trackercontainer);

        while ((socket = serversocket.accept()) != null){

            Thread trackerworker = new Thread(new TrackerWorker(socket,trackerworkercontroller));
            trackerworker.start();
        }

        serversocket.close();
    }
}