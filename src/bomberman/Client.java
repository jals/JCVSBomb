package bomberman;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
	private String ip;
	private String playerName;
	private Object[][] grid;
	private DatagramSocket clientSocket;

	public Client(String playerName) throws Exception {
		this.playerName = playerName;
		clientSocket = new DatagramSocket();
		joinGame(); // Code to make the methods not have warnings
		move("up");
		leaveGame();
	}

	public static void main(String[] args) throws Exception {
		new Client("Jarred");
	}

	private void move(String direction) throws Exception {
		Scanner a = new Scanner(System.in);
		while (true) {
			direction = a.nextLine(); // TODO Do error cehcking
			InetAddress IPAddress = InetAddress.getByName(ip);
			DatagramPacket sendPacket = new DatagramPacket(
					direction.getBytes(), direction.getBytes().length,
					IPAddress, 9876);
			clientSocket.send(sendPacket);
		}

	}

	private void joinGame() {
		Thread listen = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					byte[] receiveData = new byte[1024 * 100];
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					try {
						clientSocket.receive(receivePacket);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Object modifiedSentence = null;
					try {
						modifiedSentence = deserialize(Arrays.copyOfRange(
								receivePacket.getData(), 0,
								receivePacket.getLength()));
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("FROM SERVER:" + modifiedSentence + receivePacket.getLength());
					// if (modifiedSentence.equals("Done")) {
					// clientSocket.close();
					// break;
					// }
				}
			}

		});
		listen.start();

	}

	private void leaveGame() {

	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public Object[][] getGrid() {
		return grid;
	}

	public void setGrid(Object[][] grid) {
		this.grid = grid;
	}

	public static Object deserialize(byte[] data) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = null;
		in = new ObjectInputStream(bis);
		return in.readObject();
	}
}
