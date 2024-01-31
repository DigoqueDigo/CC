package dns;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;
import packets.DNSPacket;
import packets.DNSPacket.DNSProtocol;


public class DNSContainer{

    private ReadLock readLock;
    private WriteLock writeLock;
    private Map<String,String> container;


    public DNSContainer(){
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
        this.container = new HashMap<String,String>();
    }


    public void putDNSPacket(DNSPacket dnsPacket){

        try{
            this.writeLock.lock();
            this.container.putIfAbsent(
                dnsPacket.getHostName(),
                dnsPacket.getAddress());
        }

        catch (Exception e) {}
        finally {this.writeLock.unlock();}
    }

    public void removeDNSPacket(DNSPacket dnsPacket){

        try{
            this.writeLock.lock();
            this.container.remove(dnsPacket.getHostName());
        }

        catch (Exception e) {}
        finally {this.writeLock.unlock();}
    }


    public DNSPacket resolveDNSPacket(DNSPacket packet){

        try{
            this.readLock.lock();
            return (this.container.containsKey(packet.getHostName())) ?
                new DNSPacket(DNSProtocol.RESPONSE,packet.getHostName(),this.container.get(packet.getHostName())) :
                new DNSPacket(DNSProtocol.ERROR,packet.getHostName());
        }

        catch (Exception e){
            return new DNSPacket(DNSProtocol.ERROR,packet.getHostName());
        }

        finally {this.readLock.unlock();}
    }


    public String toString(){

        StringBuilder buffer = new StringBuilder();

        buffer.append("--------------------------------------------------\n");
        
        if (this.container.size() > 0){
            buffer.append(this.container.entrySet()
                .stream()
                .map(x -> "Hostname: " + x.getKey() + "\tAddress: " + x.getValue())
                .collect(Collectors.joining("\n","","\n")));
        }

        buffer.append("--------------------------------------------------\n");
        return buffer.toString();
    }
}