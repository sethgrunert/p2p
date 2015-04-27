package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import Peer.Peer;



public class ServerReceiver extends Thread{
	private int port;
    private DatagramSocket receivingSocket = null;
    private static int nextPacketNumber = 0;

    public ServerReceiver(String name, int port) {
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
            
            System.out.println("server now accepting packets");
            
            while (true) {
                byte[] buf = new byte[Peer.packetSize];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                receivingSocket.receive(packet);
                handlePacket(packet);
            }
        } catch (Exception e) {
            stopListening();
        }
    }
    
    public static void handlePacket(DatagramPacket packet) throws SocketException, IOException, InterruptedException{
    	System.out.println("Server Received Data from " + packet.getAddress().toString());
    	byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
    	
    	int incPacketNumber = (packetData[0]*256+packetData[1]);
    	int oldCheckSum = ((0xFF & packetData[3]) << 24) | ((0xFF & packetData[2]) << 16) |
                ((0xFF & packetData[5]) << 8) | (0xFF & packetData[4]);
    	byte[] payloadData = new byte[Peer.packetSize-Peer.headerSize];
    	System.arraycopy(packetData, Peer.headerSize, payloadData, 0, Peer.packetSize-Peer.headerSize);
    	if(incPacketNumber!=nextPacketNumber){
    		System.out.println("old data, ignoring packet");
    	}
    	else if(((new String(payloadData).hashCode()) != oldCheckSum)){
    		System.out.println("corrupted data, ignoring packet");
    	}
    	else{
	    	System.out.println("Packet Number : " + incPacketNumber);
	    	System.out.println(new String(payloadData));
	    	nextPacketNumber++;
	    	if(packetData[6]==1){
	    		nextPacketNumber=0;
	    	}
	    	String s = "ACK";
            packetData = s.getBytes();
            if(Server.slowmode)
            	Thread.sleep(1000);
            Server.sender.rdtSend(packetData, packet.getAddress(), Server.peerPort);
    	}
    }
}
