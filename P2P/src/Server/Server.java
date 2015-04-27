package Server;

import java.net.SocketException;
import java.net.UnknownHostException;


public class Server {
	static int serverPort = 5000;
	static int peerPort = 4000;
	static boolean slowmode = true;
	static ServerSender sender = new ServerSender();
	static ServerReceiver receiver = new ServerReceiver("receiver",serverPort);
	
	public static void main(String[] args){
		
		receiver.start();
		try {
			sender.startSender();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		while(true);
	}
}
