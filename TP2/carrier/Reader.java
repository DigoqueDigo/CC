package carrier;
import java.io.IOException;
import java.io.InputStream;


public class Reader{


    public static int read(InputStream inputStream, byte[] data, int length) throws IOException{

        int bytes_read = 0, attempt = 1;

        for (int rest = length; attempt > 0 && bytes_read < length; rest -= attempt){

            attempt = inputStream.read(data,bytes_read,rest);
            if (attempt > 0) bytes_read += attempt;
        }

        return bytes_read;
    }   
}