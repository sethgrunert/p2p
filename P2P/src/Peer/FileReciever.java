package Peer;

import java.io.*;
import java.net.*;

class FileReceiver extends Thread{
	private int port;
	private DatagramSocket receivingSocket = null;
	private Peer p;
	
	
	FileReceiver(String name, int port){
		super(name);
        this.port = port;
	}
	
	public void stopListening() {
        if (receivingSocket != null) {
            receivingSocket.close();
        }
    }
	
	public void setPeer(Peer p){
		this.p = p;
	}
	
	public void run(){
		try {
			receivingSocket = new DatagramSocket(port);
			while(true){
				System.out.println("gothere");
		        byte[] receiveData = new byte[128];
		        while(true){
		        	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		            try {
		            receivingSocket.receive(receivePacket);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            String packetString = new String(receivePacket.getData());
		            String headerString = getHeader(packetString);
		            System.out.println(packetString);
		            if(isGET(packetString)){
		            	String port="";
			            int i=headerString.length()+1;
			            while(packetString.charAt(i)!=0){
			            	port+=packetString.charAt(i);
			            	i++;
			            }
		            	FileSender fs = new FileSender();
		            	fs.startSender(receivePacket.getAddress(), Integer.parseInt(port));
		            	String requestedFile = packetString.substring(4,headerString.length()-9);
		            	fs.sendFile(p.folderName+requestedFile);
		            }
		            else{
		            	if(p.wantedFile.getOutputStream()==null)
		            		p.wantedFile.startOutputStream(p.folderName);
		            	p.wantedFile.getOutputStream().write(receivePacket.getData());
		            }
		        }
	        }
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getHeader(String s){
		int i=s.length()-1;
		while(s.charAt(i)!='\n' && i!=0){
			i--;
		}
		return s.substring(0, i);
		
	}
	
	public static boolean isGET(String packetString){
		return packetString.contains("GET");
	}
}