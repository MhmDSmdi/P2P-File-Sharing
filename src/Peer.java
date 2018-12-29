// Java program to illustrate Client side
// Implementation using DatagramSocket

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.Vector;

public class Peer extends Thread {

    private PeerState state;
    private DatagramSocket socket;
    private Vector<Integer> ports;
    private String port;
    private int serverPort = Server.SERVER_PORT;
    private PacketHandler packetHandler;

    public Peer(String port){
        this.port = port;
        ports = new Vector<>();
        state = PeerState.IDLE;
        packetHandler = new PacketHandler();
        try {
            socket = new DatagramSocket(new Integer(port), InetAddress.getLocalHost());
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void introduce() {
        try {
            byte buf[] = port.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(),  serverPort);
            socket.send(packet);
            getOtherPorts();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getOtherPorts() {
        byte[] received = new byte[1000];
        int flag = 0;
        DatagramPacket datagramPacket = new DatagramPacket(received, received.length);
        try {
            System.out.println(Server.MAX_PEER);
           while (flag != Server.MAX_PEER) {
               socket.receive(datagramPacket);
               System.out.println(Integer.valueOf(DataHandler.data(received)));
               ports.add(Integer.valueOf(DataHandler.data(received)));
               flag ++;
           }
           ports.removeElement(Integer.parseInt(port));
           System.out.println(ports);
           this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            String[] commandSplit = command.split(" ");
            switch (commandSplit[1]) {
                case "-receive":
                    byte buf[] = commandSplit[2].getBytes();
                    for (int i=0 ; i < ports.size() ; i++) {
                        DatagramPacket datagramPacket = null;
                        try {
                            datagramPacket = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), ports.get(i));
                            socket.send(datagramPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    receiveFile();
                    break;
                case "-send":
                    byte fileBuf[] = DataHandler.readFile(commandSplit[3]);
                    byte[] received = new byte[1000];
                    DatagramPacket datagramPacket = new DatagramPacket(received, received.length);
                    try {
                        socket.receive(datagramPacket);
                        if (DataHandler.data(received).equals(commandSplit[2])) {
                            System.out.println("Wanted file is: " + commandSplit[2]);                        // log
                            System.out.println("Receiver port is: " + datagramPacket.getPort());               // log
                            sendFile(fileBuf, datagramPacket.getPort());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
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
            File myFile = new File("/home/mhmd/IdeaProjects/P2P-File-Sharing-master/src/file.txt");
            FileOutputStream fos = new FileOutputStream(myFile);
            fos.write(finalBuf);
//            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Peer a = new Peer("8343");
        a.introduce();
    }
}
