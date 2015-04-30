package Peer;


import java.io.*;
import java.net.*;

public class FileSender{
	private int receiverPortNumber = 0;
	private DatagramSocket socket = null;
	private InetAddress internetAddress = null;

	public FileSender() {}
	
	public void startSender(InetAddress targetAddress, int receiverPortNumber) throws SocketException, UnknownHostException {
       socket = new DatagramSocket();
       internetAddress = targetAddress;
       this.receiverPortNumber = receiverPortNumber;
	}
	
	public void stopSender(){
        if (socket!=null){
            socket.close();
        }
    }
	
	public void sendFile(String fileName){
		BufferedReader br = null;
		try {
			 br= new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    byte[] sendData = new byte[128];
	    char[] sendChar = new char[128];
	    String packetString = "";
	    DatagramPacket sendPacket;
	    try {
			while(br.read(sendChar, 0, sendChar.length)!=-1){
				packetString = new String(sendChar);
				packetString.trim();
				sendData = new byte[sendChar.length];
				sendData=packetString.getBytes();
				System.out.println("Sending HTTP 200 to " +  internetAddress.toString() + "  " +receiverPortNumber);
				sendPacket = new DatagramPacket(sendData, sendData.length, internetAddress, receiverPortNumber);
				socket.send(sendPacket);
				sendChar = new char[128];
			}
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
   }
	
	public void sendRequest(String fileName, int port){
		String requestString ="GET " + fileName + " http/1.1\n";
		requestString+=Integer.toString(port);
	    byte[] sendData = requestString.getBytes();
	    DatagramPacket sendPacket;
	    try {
	    	System.out.println("Sending HTTP GET to " +  internetAddress.toString() + "  " +receiverPortNumber);
			sendPacket = new DatagramPacket(sendData, sendData.length, internetAddress, receiverPortNumber);
			socket.send(sendPacket);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}