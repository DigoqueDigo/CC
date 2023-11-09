package client;
import java.net.Socket;


public class Main{


    public static void main(String[] args){

        try{

            if (args.length == 3){
                
                int port = Integer.parseInt(args[2]);
                Socket socket = new Socket(args[1],port);
                Client client = new Client(socket,args[0]);
                
                client.run();
            }
        }

        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
