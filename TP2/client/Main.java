package client;
import java.net.Socket;
import tracker.Tracker;


public class Main{


    public static void main(String[] args){

        try{

            int port = Tracker.DefaultPort;
            if (args.length == 3) port = Integer.parseInt(args[2]);

            Socket socket = new Socket(args[1],port);
            Client client = new Client(socket,args[0]);
            client.run();
        }

        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
