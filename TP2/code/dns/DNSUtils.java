package dns;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class DNSUtils{

    private static DNSUtils singleton = null;

    private DNSUtils() {}


    public static DNSUtils getInstance(){
        if (DNSUtils.singleton == null) DNSUtils.singleton = new DNSUtils();
        return DNSUtils.singleton;
    }


    public String getHostName(){

        try{
            Process process = Runtime.getRuntime().exec("hostname");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return stdin.readLine();
        }

        catch (Exception e) {return null;}
    }
}