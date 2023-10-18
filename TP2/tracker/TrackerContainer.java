package tracker;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import packets.TCPPacket;


public class TrackerContainer{

    private Map<String,List<String>> container;
    private ReadLock readlock;
    private WriteLock writelock;

    
    public TrackerContainer(){
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.readlock = lock.readLock();
        this.writelock = lock.writeLock();
        this.container = new HashMap<String,List<String>>();
    }


    public void put(TCPPacket tcpPacket){

        try{
            this.writelock.lock();
            tcpPacket.getFiles().forEach(x -> {
                this.container.putIfAbsent(x, new ArrayList<>());
                this.container.get(x).add(tcpPacket.getIPsource());
            });
        }

        finally {this.writelock.unlock();}
    }


    public List<String> getIPaddresses(String file){

        try{
            this.readlock.lock();
            return this.container.get(file);
        }

        finally {this.readlock.unlock();}
    }


    public void removeIPaddress(String IPaddress){

        try{
            this.writelock.lock();
            this.container.values().stream().forEach(x -> x.remove(IPaddress));
            this.container.entrySet().stream().forEach(x -> {
                if (x.getValue().size() == 0){
                    this.container.remove(x.getKey());
                }
            });
        }

        finally {this.writelock.unlock();}

    }
}