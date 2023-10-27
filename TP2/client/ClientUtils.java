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
import packets.TCPPacket;
import packets.TCPPacket.Protocol;
import packets.info.FileInfo;
import packets.info.PieceInfo;
import packets.messages.ToTracker;
import packets.messages.Message.TYPE;


public class ClientUtils{


    private static boolean fileAlredyExists(String file){
        return new File(file).exists();
    }


    private static List<String> getFiles(String folder){

        return Stream.of(new File(folder).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
    }


    private static List<PieceInfo> getPieces(String file, String folder) throws IOException{

        byte[] data = new byte[PieceInfo.SIZE];
        List<PieceInfo> pieces = new ArrayList<PieceInfo>();
        FileInputStream inputstream = new FileInputStream(folder + file);

        for (int bytes_read, p = 0; (bytes_read = Reader.read(inputstream,data,data.length)) > 0; p++){
            pieces.add(new PieceInfo(Arrays.copyOf(data,bytes_read),p,file));
        }

        inputstream.close();
        return pieces;
    }


    public static TCPPacket getHELLOTCPPacket(String folder, InetSocketAddress source, InetSocketAddress dest){

        TCPPacket tcpPacket;
        ToTracker toTracker = new ToTracker();

        List<FileInfo> files = ClientUtils.getFiles(folder)
                                        .stream()
                                        .map(x -> new FileInfo(x,new File(folder + x).length()))
                                        .collect(Collectors.toList());

        for (FileInfo file : files){

            try {toTracker.put(file,ClientUtils.getPieces(file.getName(),folder));}

            catch (Exception e){
                System.out.println("ERROR while defining pieces");}
        }

        tcpPacket = new TCPPacket(
                            Protocol.HELLO,
                            source.getAddress().getHostAddress(),
                            dest.getAddress().getHostAddress(),
                            source.getPort(),
                            dest.getPort(),
                            TYPE.TOTRACKER);

        tcpPacket.setToTracker(toTracker);
        return tcpPacket;
    }


    public static TCPPacket getTCPPacket(String line, String folder, InetSocketAddress source, InetSocketAddress dest) throws FileAlreadyExistsException{

        TCPPacket tcpPacket;
        ToTracker toTracker = new ToTracker();
        String[] tokens = line.split(" ");

        switch (Protocol.valueOf(tokens[0])){

            case GET:

                String existFile = Stream.of(tokens)
                                        .skip(1)
                                        .filter(x -> ClientUtils.fileAlredyExists(folder + x))
                                        .findFirst()
                                        .orElse(null);

                if (existFile != null) throw new FileAlreadyExistsException(existFile);

                Stream.of(tokens)
                    .skip(1)
                    .map(x -> new FileInfo(x,0))
                    .forEach(x -> toTracker.put(x, new ArrayList<PieceInfo>(0)));
                break;

            default:
                break;
        }

        tcpPacket = new TCPPacket(
                    Protocol.valueOf(tokens[0]),
                    source.getAddress().getHostAddress(),
                    dest.getAddress().getHostAddress(),
                    source.getPort(),
                    dest.getPort(),
                    TYPE.TOTRACKER);

        tcpPacket.setToTracker(toTracker);
        return tcpPacket;
    }
}