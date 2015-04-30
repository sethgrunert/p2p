package Peer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

import Server.FileData;
import Server.Server;


public class Peer{
	public static final int INFORM=1;
	public static final int QUERY=2;
	public static final int EXIT=3;
	public static final int PACKETSIZE = 128;
	public static final int HEADERSIZE = 10;
	public static final int TIMEOUT = 3000;
	public static final int SERVERPORT = 5000;
	public static final int SERVERINCPORT = 6000;
	static InetAddress serverAddress = null;
	static int numPeers = 0; //number of peers running on this machine
	static boolean testMode =true;
	
	int ack = -1;
	boolean slowMode = true;
	
	int fileIncPort = 10000; 
	PeerSender sender = new PeerSender();
	PeerReceiver receiver = new PeerReceiver("reciever", SERVERINCPORT);
	PeerWindow window;
	FileSender fileSender= null;
	FileReceiver fileReciever = null;
	Scanner fileScan;
	FileData wantedFile = null;
	String fileList = "";
	String folderName= "";
	
	Peer(){
		numPeers++;
		fileIncPort=10000+numPeers*1000;
		folderName = "Peer"+Integer.toString(numPeers)+"/";
		fileList=folderName+"files.txt";
	}
	
	public static void main(String[] args){
		Peer p1 = new Peer();
		p1.window = new PeerWindow(p1);
		if(testMode){
			Peer p2 = new Peer();
			p2.window = new PeerWindow(p2);
			//Peer p3 = new Peer();
			//p3.window = new PeerWindow(p3);
			//Server.main(args);
		}
		
		try {
			serverAddress = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
		}
	}
	
	public void informAndUpdate(Scanner fileScan) throws ConectionFailureException, IOException, InterruptedException{
		int failures = 0;
		int failThreshold = 10;
		int packetNumber = 0;
		int ackNumber = 0;
		byte[] payloadData = new byte[PACKETSIZE-HEADERSIZE];
		byte[] headerData = new byte[HEADERSIZE];
		byte[] packetData = new byte[PACKETSIZE];
		String payloadString = "";
		String newString = "";
		int strLength = 0;
		sender.startSender(serverAddress, SERVERPORT);
		while(failures<failThreshold){
			if(ackNumber==packetNumber){
				if (fileScan.hasNext()){
					payloadString = "";
					if(newString.length()>0)
						payloadString+=newString;
					while(fileScan.hasNext() && payloadString.length()<PACKETSIZE-HEADERSIZE){
						newString = fileScan.nextLine()+"\n";
						payloadString+=newString;
					}
					if(payloadString.length()>PACKETSIZE-HEADERSIZE)
						payloadString = payloadString.substring(0, payloadString.length()-newString.length());
					
					payloadData = payloadString.getBytes();
					if (payloadString.length()<PACKETSIZE-HEADERSIZE)
		                payloadData = Arrays.copyOf(payloadData, PACKETSIZE-HEADERSIZE);
					headerData = createHeader(payloadData,packetNumber,!fileScan.hasNext());
					headerData[0]=INFORM;
					System.arraycopy(headerData, 0, packetData, 0, headerData.length);
			        System.arraycopy(payloadData, 0, packetData, headerData.length, payloadData.length);
		            packetNumber++;
				}
				else{
					sender.stopSender();
					return;
				}
			}
			if(sender.rdtSend(packetData,this)){
            	ackNumber++;
            	failures=0;
			}
			else
				failures++;
		}
		sender.stopSender();
		throw new ConectionFailureException();
	}
	
	

	/**
	 * @throws InterruptedException 
	 * @throws IOException 
	 * 
	 */
	public void exit() throws IOException, InterruptedException {
		byte[] payloadData = new byte[PACKETSIZE-HEADERSIZE];
		byte[] headerData = new byte[HEADERSIZE];
		byte[] packetData = new byte[PACKETSIZE];
		
		sender.startSender(serverAddress, SERVERPORT);
		for(int i=0; i<payloadData.length; i++)
			payloadData[i]=0;
		headerData = createHeader(payloadData,0,true);
		headerData[0]=EXIT;
		System.arraycopy(headerData, 0, packetData, 0, headerData.length);
        System.arraycopy(payloadData, 0, packetData, headerData.length, payloadData.length);
        sender.rdtSend(packetData,this);
        sender.stopSender();
	}

	/**
	 * @param text
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void query(String fileName) throws IOException, InterruptedException {
		byte[] payloadData = new byte[PACKETSIZE-HEADERSIZE];
		byte[] headerData = new byte[HEADERSIZE];
		byte[] packetData = new byte[PACKETSIZE];
		int failures = 0;
		int failThreshold = 10;
		boolean responce=false;
		
		while(failures<failThreshold && responce==false){
			sender.startSender(serverAddress, SERVERPORT);
			byte[] tempData = fileName.getBytes();
			System.arraycopy(tempData, 0, payloadData, 0, tempData.length);
			headerData = createHeader(payloadData,0,true);
			headerData[0]=QUERY;
			System.arraycopy(headerData, 0, packetData, 0, headerData.length);
	        System.arraycopy(payloadData, 0, packetData, headerData.length, payloadData.length);
	        if(sender.rdtSend(packetData,this))
	        	responce=true;
	        else
	        	failures++;
		}
        sender.stopSender();
	}
	
	/**
	 * Header:
	 * [0] Method(1=inform and update,2=query,3=exit)
	 * [2-2] = packet number%65535
	 * [3-6] = checksum
	 * [7] = last packet(0,1)
	 * [8-9] = port number to use for file transfers
	 */
	public byte[] createHeader(byte[] payloadData, int pktNum,boolean lastPacket){
		byte[] header = new byte[HEADERSIZE];
		pktNum = pktNum%65536;
		header[1]=(byte) (pktNum/256);
		header[2]=(byte) (pktNum%256);
		Integer chkSum = (new String(payloadData)).hashCode();
		for (int i = 3; i < 7; i++) {
		    header[i] = (byte)(chkSum >>> ((i-3) * 8));
		}
		if(lastPacket)
			header[7]=1;
		else
			header[7]=0;
		header[8]=(byte) (fileIncPort/256);
		header[9]=(byte) (fileIncPort%256);
		return header;
	}
}
