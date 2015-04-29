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
	private ArrayList<PeerData> peerList = new ArrayList<PeerData>();
	
	public FileData(String fileName, int size){
		this.fileName=fileName;
		this.size=size;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public int getSize(){
		return size;
	}
	
	public void setSize(int size){
		this.size=size;
	}
	
	public void addPeer(PeerData p){
		int index = searchPeers(p);
		if(index==-1)
			peerList.add(new PeerData(p.getIP(),p.getPort()));
	}
	
	public int searchPeers(PeerData p){
		int index = -1;
		for(int i=0; i<peerList.size(); i++){
			if(p.equals(peerList.get(i)))
				index=i;
		}
		return index;
	}
	
	public boolean deletePeer(PeerData p){
		int index = searchPeers(p);
		if(index==-1)
			return false;
		else{
			peerList.remove(index);
			return true;
		}
	}
	
	public int numPeers(){
		return peerList.size();
	}
	
	public ArrayList<PeerData> getPeers(){
		return peerList;
	}
}
