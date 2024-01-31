package tracker.containers;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import packets.TCPPacket;
import packets.info.FileInfo;
import packets.messages.ToClient;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;


public class TrackerContainer{

    private Map<FileInfo,PieceContainer> container;
    private ReadLock readlock;
    private WriteLock writelock;


    public TrackerContainer(){
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.readlock = lock.readLock();
        this.writelock = lock.writeLock();
        this.container = new HashMap<FileInfo,PieceContainer>();
    }


    public void put(TCPPacket tcpPacket){

        try{
            this.writelock.lock();
            tcpPacket.getToTracker().getKeys().forEach(x -> {
                this.container.putIfAbsent(x, new PieceContainer());
                tcpPacket.getToTracker().getValue(x).forEach(y -> {
                    this.container.get(x).put(y,tcpPacket.getHostName());
                });
            });
        }

        finally {this.writelock.unlock();}
    }


    public ToClient getPieces(TCPPacket tcpPacket){

        try{
            this.readlock.lock();
            ToClient toClient = new ToClient();

            for (FileInfo file : tcpPacket.getToTracker().getKeys()){

                PieceContainer piececontainer = this.container.get(file);

                if (piececontainer != null){
                    piececontainer.getKeys().forEach(x -> {
                        toClient.put(x,piececontainer.getValue(x));
                    });
                }
            }

            return toClient;
        }

        finally {this.readlock.unlock();}
    }


    public void removeClient(TCPPacket tcpPacket){

        try{
            this.writelock.lock();
            this.container.values().stream().forEach(x -> x.removeClient(tcpPacket.getHostName()));

            Iterator <Map.Entry<FileInfo,PieceContainer>> iterator = this.container.entrySet().iterator();

            while (iterator.hasNext()){
                if (iterator.next().getValue().size() == 0) iterator.remove();
            }
        }

        finally {this.writelock.unlock();}
    }


    public String toString(){

        StringBuilder buffer = new StringBuilder();

        buffer.append("---------------------------------------------------------------------------------------------------\n");

        buffer.append(this.container.values()
            .stream()
            .map(x -> x.toString())
            .collect(Collectors.joining("\n","","\n")));

        buffer.append("---------------------------------------------------------------------------------------------------\n");
        return buffer.toString();
    }
}
