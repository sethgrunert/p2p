package Server;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class Server {
	static int serverPort = 5000;
	static int peerPort = 4000;
	static boolean slowmode = false;
	static ServerSender sender = new ServerSender();
	static ServerReceiver receiver = new ServerReceiver("receiver",serverPort);
	static ArrayList<FileData> files = new ArrayList<FileData>();
	static ServerWindow window;
	static String errorMessage = "";
	
	public static void main(String[] args){
		window = new ServerWindow();
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
	/**
	 * 
	 * @param fileName name to seach for
	 * @return index of the file in the arraylist
	 * or -1 if file cannot be found
	 */
	public static int searchFiles(String fileName){
		int index =-1;
		for(int i=0; i<files.size(); i++){
			if(fileName.equals(files.get(i).getFileName()))
				index=i;
		}
		return index;
	}
}
