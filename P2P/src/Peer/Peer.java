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


public class Peer {
	static final int INFORM=1;
	static final int QUERY=2;
	static final int EXIT=3;
	
	static int ack = -1;
	private static int timeout = 3000;
	public static boolean slowMode = true;
	static InetAddress serverAddress = null;
	static int serverPort = 5000;
	static int serverIncPort = 4000;
	static int fileIncPort = 3000; 
	static PeerSender sender = new PeerSender();
	static PeerReceiver receiver = new PeerReceiver("reciever", serverIncPort);
	public static int packetSize = 128;
	public static int headerSize = 10;
	static String fileName = "files.txt";
	static Scanner fileScan;
	static PeerWindow window;
	
	public static void main(String[] args){
		window = new PeerWindow();
		try {
			Peer.serverAddress = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
	}
	
	public static void informAndUpdate(Scanner fileScan) throws ConectionFailureException, IOException, InterruptedException{
		int failures = 0;
		int failThreshold = 10;
		int packetNumber = 0;
		int ackNumber = 0;
		byte[] payloadData = new byte[packetSize-headerSize];
		byte[] headerData = new byte[headerSize];
		byte[] packetData = new byte[packetSize];
		String payloadString = "";
		String newString = "";
		int strLength = 0;
		sender.startSender(serverAddress, serverPort);
		while(failures<failThreshold){
			if(ackNumber==packetNumber){
				if (fileScan.hasNext()){
					payloadString = "";
					if(newString.length()>0)
						payloadString+=newString;
					while(fileScan.hasNext() && payloadString.length()<packetSize-headerSize){
						newString = fileScan.nextLine()+"\n";
						payloadString+=newString;
					}
					if(payloadString.length()>packetSize-headerSize)
						payloadString = payloadString.substring(0, payloadString.length()-newString.length());
					
					payloadData = payloadString.getBytes();
					if (payloadString.length()<packetSize-headerSize)
		                payloadData = Arrays.copyOf(payloadData, packetSize-headerSize);
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
			if(sender.rdtSend(packetData,timeout,slowMode)){
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
	public static void exit() throws IOException, InterruptedException {
		byte[] payloadData = new byte[packetSize-headerSize];
		byte[] headerData = new byte[headerSize];
		byte[] packetData = new byte[packetSize];
		
		sender.startSender(serverAddress, serverPort);
		for(int i=0; i<payloadData.length; i++)
			payloadData[i]=0;
		headerData = createHeader(payloadData,0,true);
		headerData[0]=EXIT;
		System.arraycopy(headerData, 0, packetData, 0, headerData.length);
        System.arraycopy(payloadData, 0, packetData, headerData.length, payloadData.length);
        sender.rdtSend(packetData,timeout,slowMode);
        sender.stopSender();
	}

	/**
	 * @param text
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void query(String fileName) throws IOException, InterruptedException {
		byte[] payloadData = new byte[packetSize-headerSize];
		byte[] headerData = new byte[headerSize];
		byte[] packetData = new byte[packetSize];
		int failures = 0;
		int failThreshold = 10;
		boolean responce=false;
		
		while(failures<failThreshold && responce==false){
			sender.startSender(serverAddress, serverPort);
			byte[] tempData = fileName.getBytes();
			System.arraycopy(tempData, 0, payloadData, 0, tempData.length);
			headerData = createHeader(payloadData,0,true);
			headerData[0]=QUERY;
			System.arraycopy(headerData, 0, packetData, 0, headerData.length);
	        System.arraycopy(payloadData, 0, packetData, headerData.length, payloadData.length);
	        if(sender.rdtSend(packetData,timeout,slowMode))
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
	public static byte[] createHeader(byte[] payloadData, int pktNum,boolean lastPacket){
		byte[] header = new byte[headerSize];
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
