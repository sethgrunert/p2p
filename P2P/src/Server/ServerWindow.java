/**
 * 
 */
package Server;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.*;

/**
 * @author Seth Grunert sethgrunert@my.ccsu.edu
 *
 */
public class ServerWindow extends JFrame{
	public ServerWindow(){
		this.setSize(300, 200);
		this.setResizable(false);
		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
		this.add(new ServerCanvas());
	}
	
	class ServerCanvas extends JPanel{
		ServerCanvas(){
			setBackground(Color.WHITE);
		}
		
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			g.drawString("CURRENT NUMBER OF FILES: " + Server.files.size(), 50, 50);
			g.drawString(Server.errorMessage, 50, 100);
			repaint();
			}
	}
}
