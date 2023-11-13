package client;
import java.net.DatagramSocket;
import java.net.Socket;
import client.listener.Listener;


public class Main{


    public static void main(String[] args){

        try{

            if (args.length == 3){
                
                int client_port = Integer.parseInt(args[2]);
                Socket client_socket = new Socket(args[1],client_port);

                int port = Listener.DefaultPort;

            //    if (args[0].equals("C2/")) port = 33333;
                DatagramSocket listener_socket = new DatagramSocket(port);

                Listener listener = new Listener(listener_socket);
                Client client = new Client(client_socket,args[0]);
                new Thread(listener).start();
                
                client.run();
                listener_socket.close();
            }
        }

        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
