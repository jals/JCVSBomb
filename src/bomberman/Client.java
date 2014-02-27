package bomberman;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class Client {
	private InetAddress ip;
	private int port;
	private String playerName;
	private Object[][] grid;
	private DatagramSocket clientSocket;

	public Client(String playerName) throws Exception {
		this.playerName = playerName;
		clientSocket = new DatagramSocket();
		ip = InetAddress.getByName(null);
		port = 9876;
		joinGame(); // Code to make the methods not have warnings
		move("move_right");
		leaveGame();
	}

	public static void main(String[] args) throws Exception {
		new Client("Jarred");
	}

	private void move(String direction) throws Exception {
		Utility.sendMessage(clientSocket, new Command(playerName,
				Command.Operation.valueOf(direction.toUpperCase())), ip, port);

	}

	private void joinGame() throws Exception {

		move("join_game");

		// ////////////////
		Thread listen = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					byte[] receiveData = new byte[1024 * 100];
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					try {
						clientSocket.receive(receivePacket);
						Object grid = null;
						ip = receivePacket.getAddress();
						port = receivePacket.getPort();

						grid = Utility.deserialize(Arrays.copyOfRange(
								receivePacket.getData(), 0,
								receivePacket.getLength()));
						System.out.println("FROM SERVER:"
								+ Utility.getGridString((Object[][]) grid));
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// TODO update the screen with the grid
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

}
