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

import Server.FileData;

/**
 * @author Seth Grunert sethgrunert@my.ccsu.edu
 *
 */
public class PeerWindow extends JFrame {
	JCheckBox slowModeButton = new JCheckBox();
	JButton informButton = new JButton("Inform and Update");
	JButton queryButton = new JButton("Query");
	JButton exitButton = new JButton("Exit");
	public JTextField output = new JTextField("-----------------WAITING FOR INPUT-----------------");
	JTextField input = new JTextField("enter query here");
	JTextField portField = new JTextField("",5);
	Peer p = null;
	
	public PeerWindow(Peer p){
		this.p=p;
		this.setSize(300, 200);
		this.setResizable(false);
		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
		this.setVisible(true);
		
		JPanel canvas = new JPanel();
		canvas.add(new JLabel("slowmode"));
		canvas.add(slowModeButton);
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
		portField.setText(Integer.toString(p.fileIncPort));
		canvas.add(portField);
		canvas.add(output);
		this.add(canvas);
	}
	
	class ButtonListener implements ActionListener{
		
		@Override
		public void actionPerformed(ActionEvent e) {
			p.slowMode = slowModeButton.isSelected();
			if(p.fileReciever==null){
				p.fileIncPort = Integer.parseInt(portField.getText());
				p.fileReciever = new FileReceiver("fileReciever",p.fileIncPort);
				portField.setEditable(false);
				p.fileReciever.start();
			}
			
			if(e.getSource()==informButton){
				p.receiver = new PeerReceiver("reciever", Peer.SERVERINCPORT);
				p.receiver.setPeer(p);
				p.receiver.start();
				try {
					p.fileScan = new Scanner(new File(p.fileList));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				
				byte[] data = new byte[1000];
				for(int i=0; i<data.length; i++){
					data[i]=(byte) (65+i%23);
				}
				ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
				try {
					p.informAndUpdate(p.fileScan);
					output.setText("ALL DATA SENT SUCCSSFULY");
				}catch (IOException ex) {
					ex.printStackTrace();
				} catch (ConectionFailureException ex) {
					ex.printStackTrace();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				finally{
					p.receiver.stopListening();
				}
			}
			else if(e.getSource()==exitButton){
				p.receiver = new PeerReceiver("reciever", Peer.SERVERINCPORT);
				p.receiver.setPeer(p);
				p.receiver.start();
				try {
					System.out.println("exiting");
					p.exit();
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
					p.receiver.stopListening();
				}
			}
			else if(e.getSource()==queryButton){
				p.receiver = new PeerReceiver("reciever", Peer.SERVERINCPORT);
				p.receiver.setPeer(p);
				p.receiver.start();
				try {
					System.out.println("querying");
					p.wantedFile = new FileData(input.getText(), 0);
					p.query(input.getText());
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
					p.receiver.stopListening();
				}
			}
		}
		
	}
}
