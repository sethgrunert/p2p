/**
 * 
 */
package Server;

/**
 * @author Seth Grunert sethgrunert@my.ccsu.edu
 *
 */
public class PeerData {
	private byte[] ip = null;
	private int receivingPort = 0;
	private String name;
	

	PeerData(String name,byte[] ip, int receivingPort){
		this.name = name;
		if(ip.length==4)
			this.ip=ip;
		else{
			System.out.println("badly formated IP");
			return;
		}
		if(receivingPort>=0 && receivingPort<=65535)
			this.receivingPort=receivingPort;
		else{
			System.out.println("Port is out of range");
			return;
		}
	}
	
	public byte[] getIP(){
		return ip;
	}
	
	public int getPort(){
		return receivingPort;
	}
	
	public String getName(){
		return name;
	}
	
}
