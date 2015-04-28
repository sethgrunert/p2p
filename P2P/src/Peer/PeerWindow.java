/**
 * 
 */
package Peer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.*;

/**
 * @author Seth Grunert sethgrunert@my.ccsu.edu
 *
 */
public class PeerWindow extends JFrame {
	JCheckBox slowMode = new JCheckBox();
	JButton informButton = new JButton("Inform and Update");
	JButton queryButton = new JButton("Query");
	JButton exitButton = new JButton("Exit");
	public JTextField output = new JTextField("-----------------WAITING FOR INPUT-----------------");
	JTextField input = new JTextField("enter query here");
	JTextField portField = new JTextField("3000",5);
	
	public PeerWindow(){
		this.setSize(300, 200);
		this.setResizable(false);
		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
		this.setVisible(true);
		
		JPanel canvas = new JPanel();
		canvas.add(new JLabel("slowmode"));
		canvas.add(slowMode);
		informButton.addActionListener(new ButtonListener());
		canvas.add(informButton);
		canvas.add(input);
		queryButton.addActionListener(new ButtonListener());
		canvas.add(queryButton);
		exitButton.addActionListener(new ButtonListener());
		canvas.add(exitButton);
		output.setEditable(false);
		output.setMinimumSize(new Dimension(400, 15));
		canvas.add(new JLabel("Enter port to transfer files on"));
		canvas.add(portField);
		canvas.add(output);
		this.add(canvas);
	}
	
	class ButtonListener implements ActionListener{
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Peer.slowMode= slowMode.isSelected();
			if(Peer.fileReciever==null){
				Peer.fileIncPort = Integer.parseInt(portField.getText());
				Peer.fileReciever = new FileReceiver("fileReciever",Peer.fileIncPort);
				portField.setEditable(false);
				Peer.fileReciever.run();
			}
			
			if(e.getSource()==informButton){
				Peer.receiver = new PeerReceiver("reciever", Peer.serverIncPort);
				Peer.receiver.start();
				try {
					Peer.fileScan = new Scanner(new File(Peer.fileName));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				
				byte[] data = new byte[1000];
				for(int i=0; i<data.length; i++){
					data[i]=(byte) (65+i%23);
				}
				ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
				try {
					Peer.informAndUpdate(Peer.fileScan);
					output.setText("ALL DATA SENT SUCCSSFULY");
				}catch (IOException ex) {
					ex.printStackTrace();
				} catch (ConectionFailureException ex) {
					ex.printStackTrace();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				finally{
					Peer.receiver.stopListening();
				}
			}
			else if(e.getSource()==exitButton){
				Peer.receiver = new PeerReceiver("reciever", Peer.serverIncPort);
				Peer.receiver.start();
				try {
					System.out.println("exiting");
					Peer.exit();
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally{
					Peer.receiver.stopListening();
				}
			}
			else if(e.getSource()==queryButton){
				Peer.receiver = new PeerReceiver("reciever", Peer.serverIncPort);
				Peer.receiver.start();
				try {
					System.out.println("querying");
					Peer.query(input.getText());
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally{
					Peer.receiver.stopListening();
				}
			}
		}
		
	}
}
