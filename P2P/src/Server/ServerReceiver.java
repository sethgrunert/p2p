package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import Peer.ConectionFailureException;
import Peer.Peer;



public class ServerReceiver extends Thread{
	private int port;
    private DatagramSocket receivingSocket = null;
    private static ArrayList<PeerData> peers = new ArrayList<PeerData>();
    private static PeerData currentPeer = null;
    

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
                byte[] buf = new byte[Peer.PACKETSIZE];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                receivingSocket.receive(packet);
                handlePacket(packet);
            }
        } catch (Exception e) {
            stopListening();
        }
    }
    
    public static void handlePacket(DatagramPacket packet) throws SocketException, IOException, InterruptedException{
    	byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
    	System.out.println("Server Received Data from " + packet.getAddress().toString());
    	switch(packetData[0]){
    		case 1:
    			Server.errorMessage="";
    			System.out.println("adding files to the database");
    			informAndUpdate(packet);
    			
    			return;
    		case 2:
    			System.out.println("querying a file from the database");
    			query(packet);
    			return;
    		case 3:
    			System.out.println("removeing files from the database");
    			exit(packet);
    			return;
    		default:
    			System.out.println("invalid method code");
    	}
    }
    
    /**
	 * @param packet
	 */
	private static void exit(DatagramPacket packet) {
		int filesRemoved=0;
		int port=((0xFF & packet.getData()[8])<<8)|(0xFF & packet.getData()[9]);
		System.out.println(port);
		currentPeer = new PeerData(packet.getAddress().getAddress(),port);
    	for(int i=0; i<Server.files.size(); i++){
    		if(Server.files.get(i).deletePeer(currentPeer)){
    			filesRemoved++;
    			if(Server.files.get(i).numPeers()==0){
    				Server.files.remove(i);
    				i--;
    			}
    		}
    		if(filesRemoved==0)
    			Server.errorMessage="No record of peer in database";
    	}
	}

	/**
	 * @param packet
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws SocketException 
	 */
	private static void query(DatagramPacket packet) throws SocketException, IOException, InterruptedException {
		byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
		byte[] payloadData = new byte[Peer.PACKETSIZE-Peer.HEADERSIZE];
    	System.arraycopy(packetData, Peer.HEADERSIZE, payloadData, 0, Peer.PACKETSIZE-Peer.HEADERSIZE);
		String fileName = "";
		int i=0;
		while(payloadData[i]!=0){
			fileName+=(char)(payloadData[i]);
			i++;
		}
		int fileIndex=-1;
		for(i=0; i<Server.files.size(); i++){
			if(Server.files.get(i).getFileName().equals(fileName))
				fileIndex=i;
		}
		
		String packetString="";
		if(fileIndex==-1)
			packetString+="400 ACK \n";
		else{
			packetString+="200 ACK ";
			packetString+= Integer.toString(Server.files.get(fileIndex).getSize());
			packetString+=" \n";
			ArrayList<PeerData> peerList = Server.files.get(fileIndex).getPeers();
			for(i=0; i<peerList.size(); i++){
				packetString+= new String(peerList.get(i).getIP());
				packetString+=" " + Integer.toString(peerList.get(i).getPort());
				packetString+=" \n";
			}
		}
		packetData = packetString.getBytes();
		Server.sender.rdtSend(packetData, packet.getAddress(), Server.peerPort);
	}

	public static void informAndUpdate(DatagramPacket packet) throws SocketException, IOException, InterruptedException{
    	byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
    	int port=((0xFF & packet.getData()[8])<<8)|(0xFF & packet.getData()[9]);
    	currentPeer= new PeerData(packet.getAddress().getAddress(),port);
    	int peerNum = -1;
    	for(int i=0; i<peers.size(); i++){
    		if(peers.get(i).equals(currentPeer)){
    			currentPeer=peers.get(i);
    			peerNum=i;
    		}
    	}
    	if(peerNum==-1){
    		peers.add(currentPeer);
    		peerNum=peers.size()-1;
    	}
    	
    	int incPacketNumber = (packetData[1]*256+packetData[2]);
    	int oldCheckSum = ((0xFF & packetData[6]) << 24) | ((0xFF & packetData[5]) << 16) |
                ((0xFF & packetData[4]) << 8) | (0xFF & packetData[3]);
    	byte[] payloadData = new byte[Peer.PACKETSIZE-Peer.HEADERSIZE];
    	System.arraycopy(packetData, Peer.HEADERSIZE, payloadData, 0, Peer.PACKETSIZE-Peer.HEADERSIZE);
    	if(incPacketNumber!=currentPeer.getACK()){
    		Server.errorMessage = "old data, ignoring packet" + " " + currentPeer.getACK() + " " + incPacketNumber;
    	}
    	else if(((new String(payloadData).hashCode()) != oldCheckSum)){
    		Server.errorMessage = "corrupted data, ignoring packet";
    	}
    	else{
	    	System.out.println("Packet Number : " + incPacketNumber);
	    	String fileName = "";
	    	String fileSize = "";
	    	int size = 0;
	    	
	    	for(int i=0; i<payloadData.length; i++){
	    		fileName = "";
		    	fileSize = "";
		    	size = 0;
	    		while((char)(payloadData[i])!=' ' && i<payloadData.length){
	    			fileName+=(char)payloadData[i];
	    			i++;
	    		}
	    		while((char)(payloadData[i])==' ' && i<payloadData.length)
	    			i++;
	    		while((char)(payloadData[i])!='\n' && i<payloadData.length){
	    			fileSize+=(char)payloadData[i];
	    			i++;
	    		}
	    		size=Integer.parseInt(fileSize);
	    		int fileIndex = Server.searchFiles(fileName);
	    		if(fileIndex==-1){
	    			Server.files.add(new FileData(fileName,size));
	    			Server.files.get(Server.files.size()-1).addPeer(currentPeer);
	    		}
	    		else
	    			Server.files.get(fileIndex).addPeer(currentPeer);
	    		if(i<payloadData.length-1){
		    		if(payloadData[i+1]==0){
		    			i=payloadData.length;
		    		}
	    		}
	    	}
	    	
	    	currentPeer.incACK();
	    	if(packetData[7]==1)
	    		peers.remove(currentPeer);
    	}
	    	String s = "ACK";
            packetData = s.getBytes();
            if(Server.slowmode)
            	Thread.sleep(1000);
            Server.sender.rdtSend(packetData, packet.getAddress(), Server.peerPort);
    	
    }
}
