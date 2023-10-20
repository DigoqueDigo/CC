package client;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import packets.TCPPacket;
import packets.TCPPacket.Protocol;
import packets.info.FileInfo;
import packets.info.PieceInfo;
import packets.messages.ToTracker;
import packets.messages.Message.TYPE;


public class ClientUtils{


    private static List<String> getFiles(String folder){

        return Stream.of(new File(folder).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
    }


    private static List<PieceInfo> getPieces(String path) throws IOException{

        byte[] data = new byte[PieceInfo.SIZE];
        List<PieceInfo> pieces = new ArrayList<PieceInfo>();
        FileInputStream inputstream = new FileInputStream(path);

        for (int bytes_read, p = 0; (bytes_read = inputstream.read(data)) > 0; p++){
            pieces.add(new PieceInfo(Arrays.copyOf(data,bytes_read),p));
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

            try {toTracker.put(file,ClientUtils.getPieces(folder + file.getName()));}

            catch (Exception e){
                System.out.println("ERRO ao definir pieces");}
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


    public static TCPPacket getTCPPacket(String line, InetSocketAddress source, InetSocketAddress dest){

        TCPPacket tcpPacket;
        ToTracker toTracker = new ToTracker();
        String[] tokens = line.split(" ");

        switch (Protocol.valueOf(tokens[0])){

            case GET:
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