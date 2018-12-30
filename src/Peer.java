// Java program to illustrate Client side
// Implementation using DatagramSocket

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

public class Peer {

    private static final int MULTICAST_SOCKET_PORT = 7337;
    private static final String MULTICAST_SOCKET_IP = "231.0.0.0";
    private static  final String SERVE_PORT = "7353";
    private PeerState state;
    private DatagramSocket socket;
    private HashMap<String, String> myFiles;
    private String receivePort;
    private Thread broadCastThread;
    private Thread peerControllingThread;
    private PacketHandler packetHandler;
    public String senderMessage = "p2p -send file.txt /home/mhmd/IdeaProjects/P2P-File-Sharing-master/src/file.txt";
    public String receiverMessage = "p2p -receive file.txt";

    public Peer(String port){
        this.receivePort = port;
        myFiles = new HashMap<>();
        state = PeerState.IDLE;
        packetHandler = new PacketHandler();
        broadCastThread = new Thread(broadCastRunnable);
        broadCastThread.start();
        peerControllingThread = new Thread(peerControllRunnable);
        peerControllingThread.start();
    }

    private Runnable broadCastRunnable = () -> {
        try {
            byte[] buf = new byte[PacketHandler.CHUNK_SIZE];
            MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_SOCKET_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_SOCKET_IP);
            multicastSocket.joinGroup(group);
            while (true) {
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(datagramPacket);
                processIncommingMessage(buf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    };


    public void introduce() {
        try {
            byte buf[] = "Hello World".getBytes();
            MulticastSocket s = new MulticastSocket();
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(MULTICAST_SOCKET_IP), MULTICAST_SOCKET_PORT);
            s.send(datagramPacket);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable peerControllRunnable = new Runnable() {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter your command ->  ");
                String command = scanner.nextLine();
//                String command = senderMessage
                String[] commandSplit = command.split(" ");
                switch (commandSplit[1].toLowerCase()) {
                    case "-receive":
                        try {
                            state = PeerState.RECEIVER;
                            byte buf[] = ("receive," + receivePort + "," + commandSplit[2]).getBytes();
                            socket = new DatagramSocket(Integer.parseInt(receivePort), InetAddress.getLocalHost());
                            MulticastSocket s = new MulticastSocket();
                            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(MULTICAST_SOCKET_IP), MULTICAST_SOCKET_PORT);
                            s.send(datagramPacket);
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        receiveFile();
                        break;
                    case "-send":
                        try {
                            state = PeerState.SENDER;
                            socket = new DatagramSocket(Integer.parseInt(SERVE_PORT), InetAddress.getLocalHost());
                            myFiles.put(commandSplit[2], commandSplit[3]);
//                            byte fileBuf[] = DataHandler.readFile(commandSplit[3]);
//                            byte[] received = new byte[1000];
//                            DatagramPacket datagramPacket = new DatagramPacket(received, received.length);
//                            socket.receive(datagramPacket);
//                            if (DataHandler.data(received).equals(commandSplit[2])) {
//                                System.out.println("Wanted file is: " + commandSplit[2]);
//                                System.out.println("Receiver port is: " + datagramPacket.getPort());
//                                sendFile(fileBuf, datagramPacket.getPort());
//                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
    };

    private void processIncommingMessage(byte[] data) {
        String message = DataHandler.data(data);
        System.out.println("Broadcast Message : " + message);
        String[] params = message.split(",");
        if (state.equals(PeerState.SENDER) && params[0].equals("receive") && myFiles.containsKey(params[2])) {
            int destPort = Integer.parseInt(params[1]);
            byte[] file = DataHandler.readFile(myFiles.get(params[2]));
            sendFile(file, destPort);
        }
        else {
            receiveFile();
        }
    }


    public boolean sendFile(byte fileBuf[], int port) {
        System.out.println("File buffer array length is: " + fileBuf.length);
        Vector<byte[]> buferVector;
        buferVector = packetHandler.segmentFile(fileBuf);
        System.out.println("Vector size after segmentation is: " + buferVector.size());

        for (int i=0 ; i < buferVector.size() ; i++){
            System.out.println("Packet" +(i+1)+ " is sending ...");
            DatagramPacket packet = null;
            try {
                packet = new DatagramPacket(buferVector.get(i), buferVector.get(i).length, InetAddress.getLocalHost(),  port);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public void receiveFile() {
        Vector<byte[]> receiveVector = new Vector<>();
        boolean finished = false;
        while(!finished){
            byte[] received = new byte[60000];
            DatagramPacket datagramPacket = new DatagramPacket(received, received.length);
            try {
                socket.receive(datagramPacket);
                if (received[1] == 1)
                    finished = true;
                receiveVector.add(received[0], received);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte finalBuf[] = packetHandler.reassembleFile(receiveVector);
        try {
            File myFile = new File("/home/mhmd/IdeaProjects/P2P-File-Sharing-master/src/IN_file.txt");
            FileOutputStream fos = new FileOutputStream(myFile);
            fos.write(finalBuf);
//            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
//        Peer a = new Peer("8465");
        Peer b = new Peer("8456");
//        b.start();
//        a.introduce();
    }
}
