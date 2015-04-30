package Peer;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import Server.PeerData;


public class PeerReceiver extends Thread {

    private int port;
    private DatagramSocket receivingSocket = null;
    private Peer p;

    public PeerReceiver(String name, int port) {
        super(name);
        this.port = port;
    }

    public void stopListening() {
        if (receivingSocket != null) {
            receivingSocket.close();
        }
    }
    
    public void setPeer(Peer p){
    	this.p=p;
    }
    
    public void run() {
        try {
            receivingSocket = new DatagramSocket(port);
            while (true) {
                byte[] buf = new byte[Peer.PACKETSIZE];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                receivingSocket.receive(packet);
                if(isACK(packet)){
                	System.out.println("Got ACK");
                	p.ack = 1;
                }
                if(isOK(packet)){
                	System.out.println("Query Successful");
                	p.window.output.setText("at least one peer has a copy of the file");
                	byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
                	System.arraycopy(packetData, 8, packetData, 0, packetData.length-8);
                	int i=0;
                	String size="";
            		while(packetData[i]!=' '){
            			size+=(char)packetData[i];
            			i++;
            		}
            		p.wantedFile.setSize(Integer.parseInt(size));
            		i+=2;
            		byte[] ip = new byte[4];
            		String port = "";
            		while(i<packetData.length && packetData[i]!=0){
            			//System.out.println(packetData[i] + " " + packetData[i+1] + " " +packetData[i+2]+ " " + packetData[i+3]);
            			ip[0]=packetData[i];
            			ip[1]=packetData[i+1];
            			ip[2]=packetData[i+2];
            			ip[3]=packetData[i+3];
            			i+=5;
            			while(packetData[i]!=' '){
                			port+=(char)packetData[i];
                			i++;
                		}
            			i+=2;
            			p.wantedFile.addPeer(new PeerData(ip,Integer.parseInt(port)));
            			port = "";
            			ip = new byte[4];
            		}
            		p.fileSender = new FileSender();
            		p.fileSender.startSender(InetAddress.getByAddress(p.wantedFile.getPeers().get(0).getIP()), p.wantedFile.getPeers().get(0).getPort());
            		p.fileSender.sendRequest(p.wantedFile.getFileName(),p.fileIncPort);
                }
                else if(isERROR(packet)){
                	System.out.println("Query Unsuccessful");
                	p.window.output.setText("no peers with file");
                	p.wantedFile=null;
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
