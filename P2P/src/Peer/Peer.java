package Peer;
import ConectionFailureException;

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
	static int ack = -1;
	private static int timeout = 3000;
	private static boolean slowMode = true;
	static InetAddress serverAddress = null;
	static int serverPort = 5000;
	static int incPort = 4000;
	static PeerSender sender = new PeerSender();
	static PeerReceiver receiver = new PeerReceiver("reciever", incPort);
	static int packetSize = 128;
	static int headerSize = 8;
	static String fileName = "files.txt";
	static Scanner fileScan;
	
	public static void main(String[] args){
		try {
			serverAddress = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		receiver.start();
		try {
			fileScan = new Scanner(new File(fileName));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		byte[] data = new byte[1000];
		for(int i=0; i<data.length; i++){
			data[i]=(byte) (65+i%23);
		}
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
		
		try {
			sendFileData(fileScan);
			System.out.println("ALL DATA SENT SUCCSSFULY");
		}catch (IOException e) {
				e.printStackTrace();
		} catch (ConectionFailureException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally{
			sender.stopSender();
			receiver.stopListening();
		}
	}
	
	private static void sendFileData(Scanner fileScan) throws ConectionFailureException, IOException, InterruptedException{
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
	
	/*
	 * Header: 
	 * [0-1] = packet number%65535
	 * [2-5] = checksum
	 * [6] = last packet(0,1)
	 * [7] = '\n'
	 */
	public static byte[] createHeader(byte[] payloadData, int pktNum,boolean lastPacket){
		byte[] header = new byte[headerSize];
		pktNum = pktNum%65536;
		header[0]=(byte) (pktNum/256);
		header[1]=(byte) (pktNum%256);
		Integer chkSum = (new String(payloadData)).hashCode();
		for (int i = 2; i < 6; i++) {
		    header[i] = (byte)(chkSum >>> (i * 8));
		}
		if(lastPacket)
			header[6]=1;
		else
			header[6]=0;
		header[7]='\n';
		return header;
	}
}
