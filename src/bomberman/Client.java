/**
 * Author: Vinayak Bansal
 * Dated: Mar 3, 2014
 */

package bomberman;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import bomberman.gui.BombermanClient;

/**
 * 
 * Client class which handles one player's UDP communication to the server and
 * instantiates the GUI. Another name for this class can be Player. But that has
 * already been used for the Player(Model).
 * 
 */

public class Client {
	private InetAddress ip;
	private int port;
	// The next two are the address for the dedicated listening port of the
	// server.
	private InetAddress listenIp;
	private int listenPort;
	private String playerName;
	private DatagramSocket clientSocket;
	private Boolean started;
	// The class required for GUI
	private BombermanClient bc = null;

	public Client(String playerName, String host, int port) throws Exception {

		this.playerName = playerName;
		clientSocket = new DatagramSocket();
		// Next two lines will be overwritten when the client has joined
		// the game
		ip = InetAddress.getByName(host);
		this.port = port;
		listenIp = InetAddress.getByName(host);
		this.listenPort = port;
		started = Boolean.FALSE;
		joinGame();
	}

	/**
	 * Main method for Client. Instantiates the client and moves it using a
	 * scanner which reads in the commands one line at a time.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		@SuppressWarnings("resource")
		Scanner a = new Scanner(System.in);
		Client j = new Client(sanitizeName(a.nextLine()), args[0],
				Integer.parseInt(args[1]));
		// TODO: We are aware that this thread should terminate once there is a 
		// LEAVE_GAME received. But we are yet to find an elegant way to do it.
		while (true) {
			j.move(a.nextLine());
		}
	}

	/**
	 * Moves the player in the grid taking the direction as a parameter.
	 * The string must be one of the names defined in ::Command.Operation
	 */
	public void move(String direction) {
		try {
			Command.Operation operation = Command.Operation.valueOf(direction
					.toUpperCase());
			// We send START_GAME and JOIN_GAME to the dedicated listening
			// port of the server. All the other messages go to the specific
			// socket for this client.
			if (operation == Command.Operation.START_GAME
					|| operation == Command.Operation.JOIN_GAME) {
				Utility.sendMessage(clientSocket, new Command(playerName,
						operation), listenIp, listenPort);
			} else {
				synchronized (started) {
					// Don't send messages until the game is started.
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

	/**
	 * Handles the communication with the server that is necessary in order to
	 * join a game. Creates a Thread to handle
	 * 
	 */
	private void joinGame() throws Exception {

		move("join_game");
		// Wait for an ack to know which port to finally communicate with.
		byte[] receiveData = new byte[1024 * 100];
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);
		clientSocket.receive(receivePacket);
		ip = receivePacket.getAddress();
		port = receivePacket.getPort();

		// Starting a separate thread for listening to updated grids.
		Thread listen = new Thread(new Runnable() {

			@Override
			public void run() {

				boolean done = false;
				while (!done) { // Iterate until LEAVE_GAME is received.
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
					} else { // we need to refresh the grid.
						if (bc == null) {
							bc = new BombermanClient((Square[][]) grid);
							bc.setVisible(true);
						} else {
							bc.refresh((Square[][]) grid);
						}
					}
				}
			}

		});
		listen.start();

	}

	/**
	 * Static method for removing spaces and non-alphanumeric characters.
	 */
	private static String sanitizeName(String name) {
		// Remove anything not alpha-numerical
		name = name.replaceAll("[^A-Za-z0-9 ]", "");

		// Remove spaces
		name = name.replaceAll(" ", "");

		return name;
	}

}
