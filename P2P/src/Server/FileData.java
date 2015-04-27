package Server;
import java.util.ArrayList;

/**
 * 
 */

/**
 * Stores peers for a single file
 * @author Seth Grunert sethgrunert@my.ccsu.edu
 */
public class FileData {
	private String fileName = "";
	private int size = 0;
	private ArrayList<byte[]> peerList = new ArrayList<byte[]>();
	
	FileData(String fileName, int size){
		this.fileName=fileName;
		this.size=size;
	}
	
	public void addPeer(byte[] ip, int port){
		peerList.add(ip);
		
	}
}
