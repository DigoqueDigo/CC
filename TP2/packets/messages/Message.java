package packets.messages;
import java.io.IOException;
import java.util.List;
import packets.Binary;


public abstract class Message<T,V> implements Binary{

    public enum TYPE {TOCLIENT, TOTRACKER};
    private TYPE type;


    public Message(TYPE type){
        this.type = type;
    }

    public TYPE getType(){
        return this.type;
    }

    public String toString(){
        return this.type.name();
    }

    public abstract List<T> getKeys();

    public abstract List<V> getValue(T key);

    public abstract byte[] serialize() throws IOException;
}
