package Peer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;


/**
 * Simple sender, takes passed data breaks it into packets and sends them
 * to the receiver
 * @author Chad Williams
 */
public class PeerSender {
    private int receiverPortNumber = 0;
    private DatagramSocket socket = null;
    private InetAddress internetAddress = null;

    public PeerSender() {
        
    }
 
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
    
    /**
     * Receive data and pass it to the current state
     *
     * @param data
     */
    public boolean rdtSend(byte[] packetData, Peer p) throws SocketException, IOException, InterruptedException {
    	int timer=0;
    	if(p.slowMode)
        	Thread.sleep(1000);
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, internetAddress, receiverPortNumber);
        socket.send(packet);
        while(timer<Peer.TIMEOUT && p.ack==-1){
        	Thread.sleep(1);
        	timer++;
        }
        
        if(p.ack==-1)
        	return false;
        if(p.ack==1){
        	p.ack=-1;
        	return true;
        }
        return false;
    }
}
