import java.io.IOException;
import java.net.*;

public class Server extends Thread {
    private DatagramSocket socket;
    private String[] peerPorts;
    public final static int MAX_PEER = 4;
    public static final int SERVER_PORT = 8484;

    public Server(int maxPeer) {
        try {
            socket = new DatagramSocket(SERVER_PORT);
            peerPorts = new String[MAX_PEER];
            this.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    private void sendPortListToPeer(String[] ports) {
        for(int i = 0 ; i < MAX_PEER; i++) {
            for (int j = 0; j < MAX_PEER; j++) {
                try {
                    DatagramPacket datagramPacket = new DatagramPacket(ports[j].getBytes(), ports[j].length(), InetAddress.getLocalHost(), new Integer(ports[i]));
                    socket.send(datagramPacket);
                    System.out.println("Send Port " + j + " to peer = " + i);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        int index = 0;
        byte[] receive = new byte[50];
        DatagramPacket DpReceive = null;
        DpReceive = new DatagramPacket(receive, receive.length);
        while (true) {
            try {
                socket.receive(DpReceive);
                peerPorts[index] = DataHandler.data(receive);
                index ++;
                System.out.println(MAX_PEER);
                if (index == MAX_PEER) {
                    sendPortListToPeer(peerPorts);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    public static void main(String[] args){
        Server server = new Server(4);
    }
}


