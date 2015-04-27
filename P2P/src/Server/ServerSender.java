package Server;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class ServerSender {
	 private int receiverPortNumber = 0;
	    private DatagramSocket socket = null;
	    private InetAddress internetAddress = null;

	    public ServerSender() {
	        
	    }
	 
	    public void startSender() throws SocketException, UnknownHostException {
	        socket = new DatagramSocket();
	        
	    }
	    
	    public void stopSender(){
	        if (socket!=null){
	            socket.close();
	        }
	    }
	    
	    /**
	     * Receive data and pass it to the current state
	     *
	     * @param data
	     */
	    public boolean rdtSend(byte[] packetData, InetAddress targetAddress,int receiverPortNumber) throws SocketException, IOException, InterruptedException {
	    	internetAddress = targetAddress;
	        this.receiverPortNumber = receiverPortNumber;
	    	int timer=0;
	        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, internetAddress, receiverPortNumber);
	        socket.send(packet);
	        return true;
	    }
}
