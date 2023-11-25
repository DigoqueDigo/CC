package client;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import carrier.Reader;
import dns.DNSUtils;
import packets.TCPPacket;
import packets.TCPPacket.TCPProtocol;
import packets.info.FileInfo;
import packets.info.PieceInfo;
import packets.messages.ToTracker;
import packets.messages.Message.TYPE;


public class ClientUtils{

    private List<String> HELLOfiles;
    private static ClientUtils singleton = null;

    private ClientUtils(){
        this.HELLOfiles = new ArrayList<>();
    }

    public static ClientUtils getInstance(){
        if (ClientUtils.singleton == null){
            ClientUtils.singleton = new ClientUtils();
        }
        return ClientUtils.singleton;
    }


    private boolean fileAlredyExists(String file){
        return HELLOfiles.contains(file);
    }


    private List<String> getFiles(String folder){

        return Stream.of(new File(folder).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .filter(file -> !HELLOfiles.contains(file))
                .collect(Collectors.toList());
    }


    private List<PieceInfo> getPieces(String file) throws IOException{

        HELLOfiles.add(file);
        byte[] data = new byte[PieceInfo.SIZE];
        List<PieceInfo> pieces = new ArrayList<PieceInfo>();
        FileInputStream inputstream = new FileInputStream(Client.FOLDER + file);

        for (int bytes_read, p = 0; (bytes_read = Reader.read(inputstream,data,data.length)) > 0; p++){
            pieces.add(new PieceInfo(Arrays.copyOf(data,bytes_read),p,file));
        }

        inputstream.close();
        return pieces;
    }


    public TCPPacket getHELLOTCPPacket(InetSocketAddress source, InetSocketAddress dest){

        TCPPacket tcpPacket;
        ToTracker toTracker = new ToTracker();

        List<FileInfo> files = getFiles(Client.FOLDER)
                                        .stream()
                                        .map(x -> new FileInfo(x,new File(Client.FOLDER + x).length()))
                                        .collect(Collectors.toList());

        for (FileInfo file : files){

            try {toTracker.put(file,getPieces(file.getName()));}

            catch (Exception e) {System.out.println("ERROR while defining pieces");}
        }

        tcpPacket = new TCPPacket(
            TCPProtocol.HELLO,
            DNSUtils.getInstance().getHostName(),
            source.getAddress().getHostAddress(),
            dest.getAddress().getHostAddress(),
            source.getPort(),
            dest.getPort(),
            TYPE.TOTRACKER);

        tcpPacket.setToTracker(toTracker);
        return tcpPacket;
    }


    public TCPPacket getTCPPacket(String line, InetSocketAddress source, InetSocketAddress dest) throws FileAlreadyExistsException{

        TCPPacket tcpPacket;
        ToTracker toTracker = new ToTracker();
        String[] tokens = line.split(" ");

        switch (TCPProtocol.valueOf(tokens[0])){

            case GET:

                String existFile = Stream.of(tokens)
                                        .skip(1)
                                        .filter(x -> fileAlredyExists(x))
                                        .findFirst()
                                        .orElse(null);

                if (existFile != null) throw new FileAlreadyExistsException(existFile);

                Stream.of(tokens)
                    .skip(1)
                    .distinct()
                    .map(x -> new FileInfo(x,0))
                    .forEach(x -> toTracker.put(x, new ArrayList<PieceInfo>(0)));
                
                break;

            default:
                break;
        }

        tcpPacket = new TCPPacket(
            TCPProtocol.valueOf(tokens[0]),
            DNSUtils.getInstance().getHostName(),
            source.getAddress().getHostAddress(),
            dest.getAddress().getHostAddress(),
            source.getPort(),
            dest.getPort(),
            TYPE.TOTRACKER);

        tcpPacket.setToTracker(toTracker);
        return tcpPacket;
    }
}