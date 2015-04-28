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
                if(isOK(packet)){
                	System.out.println("Query Successful");
                	Peer.window.output.setText("at least one peer has a copy of the file");
                }
                else if(isERROR(packet)){
                	System.out.println("Query Unsuccessful");
                	Peer.window.output.setText("no peers with file");
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
    
    private static boolean isOK(DatagramPacket packet) throws UnknownHostException{
    	byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
		return new String(packetData).contains("200") && packet.getAddress().equals(Peer.serverAddress);
	}
    private static boolean isERROR(DatagramPacket packet) throws UnknownHostException{
    	byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
		return new String(packetData).contains("400") && packet.getAddress().equals(Peer.serverAddress);
	}
}
