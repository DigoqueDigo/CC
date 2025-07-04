package client;
import java.net.DatagramSocket;
import java.net.Socket;
import client.listener.Listener;


public class Main{

    public static void main(String[] args){

        try{

            if (args.length == 5){

                Socket client_socket = new Socket(args[1],Integer.parseInt(args[2]));
                DatagramSocket listener_socket = new DatagramSocket(Listener.DefaultPort);

                Listener listener = new Listener(listener_socket);
                Client client = new Client(client_socket,args[0],args[3],Integer.parseInt(args[4]));
                
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