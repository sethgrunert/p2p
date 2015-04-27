package Peer;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;


public class PeerReceiver extends Thread {

    private int port;
    private DatagramSocket receivingSocket = null;

    public PeerReceiver(String name, int port) {
        super(name);
        this.port = port;
    }

    public void stopListening() {
        if (receivingSocket != null) {
            receivingSocket.close();
        }
    }
    
    public void run() {
        try {
            receivingSocket = new DatagramSocket(port);
            while (true) {
                byte[] buf = new byte[Peer.packetSize];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                receivingSocket.receive(packet);
                if(isACK(packet)){
                	System.out.println("Got ACK");
                	Peer.ack = 1;
                }
            }
        } catch (Exception e) {
            stopListening();
        }
    }
    
    private static boolean isACK(DatagramPacket packet) throws UnknownHostException{
    	byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
		return new String(packetData).contains("ACK") && packet.getAddress().equals(Peer.serverAddress);
	}
}
