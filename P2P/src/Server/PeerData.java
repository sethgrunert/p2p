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
	private int nextACK = 0;
	

	PeerData(byte[] ip, int receivingPort){
		if(ip.length==4)
			this.ip=ip;
		else{
			System.out.println("badly formed IP");
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
	
	public int getACK(){
		return nextACK;
	}
	
	public void incACK(){
		nextACK++;
	}
	/**
	 * tests if 2 peers have the same IP and port
	 * @param p peer to test
	 * @return true if equal
	 */
	public boolean equals(PeerData p){
		byte[] ip = p.getIP();
		for(int i=0; i<4; i++)
			if(this.ip[i]!=ip[i])
				return false;
		if(p.getPort()!=receivingPort)
			return false;
		return true;
	}
	
}
