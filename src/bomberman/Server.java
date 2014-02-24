package bomberman;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server {

	private List<Player> listOfPlayers;
	private Object[][] grid;

	public Server() {
		setListOfPlayers(new ArrayList<Player>());
	}

	public Server(String filename) {
		setListOfPlayers(new ArrayList<Player>());
	}

	public List<Player> getListOfPlayers() {
		return listOfPlayers;
	}

	public void setListOfPlayers(List<Player> listOfPlayers) {
		this.listOfPlayers = listOfPlayers;
	}

	public Object[][] getGrid() {
		return grid;
	}

	public void setGrid(Object[][] grid) {
		this.grid = grid;
	}

	public static void main (String[] args) throws IOException {
		 DatagramSocket serverSocket = new DatagramSocket(9876); 
		 byte[] receiveData = new byte[1024];  
		 byte[] sendData = new byte[1024];       
		 while(true)                {         
			 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);    
			 serverSocket.receive(receivePacket);   
			 String sentence = new String( receivePacket.getData());  
			 System.out.println("RECEIVED: " + sentence);        
			 InetAddress IPAddress = receivePacket.getAddress();     
			 int port = receivePacket.getPort();         
			 String capitalizedSentence = sentence.toUpperCase();    
			 sendData = capitalizedSentence.getBytes();              
			 DatagramPacket sendPacket =      
					 new DatagramPacket(sendData, sendData.length, IPAddress, port);  
			 serverSocket.send(sendPacket);        
			 }
		 }

}
