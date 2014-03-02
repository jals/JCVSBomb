package bomberman;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import bomberman.gui.BombermanClient;

public class Client {
	private InetAddress ip;
	private int port;
	private InetAddress listenIp;
	private int listenPort;
	private String playerName;
	private Object[][] grid;
	private DatagramSocket clientSocket;
	private Boolean started;
	private BombermanClient bc = null;

	public Client(String playerName) throws Exception {
		this.playerName = playerName;
		clientSocket = new DatagramSocket();
		ip = InetAddress.getByName(null);
		port = 9876;
		listenIp = InetAddress.getByName(null);
		listenPort = 9876;
		started = Boolean.FALSE;
		joinGame(); // Code to make the methods not have warnings
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
//		System.out.println("sdfas");
		try {
			Command.Operation operation = Command.Operation.valueOf(direction
					.toUpperCase());
			if (operation == Command.Operation.START_GAME
					|| operation == Command.Operation.JOIN_GAME) {
				Utility.sendMessage(clientSocket, new Command(playerName,
						operation), listenIp, listenPort);
			} else {
				synchronized (started) {
					if (started) {
						Utility.sendMessage(clientSocket, new Command(
								playerName, operation), ip, port);
					}
				}
			}

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

//		System.out.println("fdfdf");
		// ////////////////
		Thread listen = new Thread(new Runnable() {

			@Override
			public void run() {

				boolean done = false;
				while (!done) {
					Object grid = Utility.receiveMessage(clientSocket);
					if (grid instanceof Command.Operation) {
						Command.Operation c = (Command.Operation) grid;
						if (c == Command.Operation.LEAVE_GAME) {
							clientSocket.close();
							done = true;
						} else {
							synchronized (started) {
								started = Boolean.TRUE;
							}
						}
					} else {
//						System.out.println(Utility
//								.getGridString((Square[][]) grid)); //TODO SEND TO GUI
						if (bc == null){
							bc = new BombermanClient((Square[][]) grid);
							bc.setVisible(true);
						} else {
							bc.refresh((Square[][]) grid);
						}
					}
					// TODO update the screen with the grid
				}
			}

		});
		listen.start();

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
