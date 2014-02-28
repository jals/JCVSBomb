package bomberman;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

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
		leaveGame();
	}

	public static void main(String[] args) throws Exception {
		@SuppressWarnings("resource")
		Scanner a = new Scanner(System.in);
		Client j = new Client(a.nextLine().substring(0, 1));
		while (true) {
			j.move(a.nextLine());
		}

	}

	public void move(String direction) throws Exception {
		// System.out.println(clientSocket.getLocalPort());
		try {
			Command.Operation operation = Command.Operation.valueOf(direction
					.toUpperCase());
			Utility.sendMessage(clientSocket,
					new Command(playerName, operation), ip, port);
		} catch (IllegalArgumentException e) {
			System.out.println("Incorrect command entered. Please try again.");
		}
	}

	private void joinGame() throws Exception {

		move("join_game");
		// Wait for an ack to know which port to finally communicate with.
		byte[] receiveData = new byte[1024 * 100];
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);
		clientSocket.receive(receivePacket);
		ip = receivePacket.getAddress();
		port = receivePacket.getPort();

		// ////////////////
		Thread listen = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					Object grid = Utility.receiveMessage(clientSocket);
					if (grid instanceof String) {
						clientSocket.close();
						break;
					}

					System.out.println(Utility.getGridString((Square[][]) grid));
					// TODO update the screen with the grid
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