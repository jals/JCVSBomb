/**
 * Author: Vinayak Bansal
 * Dated: Mar 3, 2014
 */

package bomberman.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

import bomberman.common.Command;
import bomberman.common.Command.Operation;
import bomberman.common.Utility;
import bomberman.common.model.Square;
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
	private Boolean running = true;
	private Operation lastOp;
	private long lastTime;
	private Object lock = new Object();
	private boolean showGui;

	public Client(String playerName, String host, int port, boolean showGui) throws Exception {
		this.playerName = playerName;
		clientSocket = new DatagramSocket();
		// Next two lines will be overwritten when the client has joined the
		// game
		ip = InetAddress.getByName(host);
		this.port = port;
		listenIp = InetAddress.getByName(host);
		this.listenPort = port;
		started = false;
		this.showGui = showGui;
	}

	/**
	 * Main method for Client. Instantiates the client and moves it using a
	 * scanner which reads in the commands one line at a time.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Please specify two command line arguments. localhost, and port number.");
			return;
		}

		System.out.println("Enter a player name: \n");
		Scanner scanner = new Scanner(System.in);
		Client client = null;

		try {
			client = new Client(sanitizeName(scanner.nextLine()), args[0], Integer.parseInt(args[1]), true);
		} catch (Exception e) {
			System.err.println("ERROR: Client could not be created properly.\n");
			scanner.close();
			return;
		}

		scanner.close();
		client.startClient(false);
	}

	public void startClient(boolean testMode) {
		// Join a game
		try {
			joinGame();
		} catch (IOException e1) {
			if (e1 instanceof SocketException) {
				// Socket was closed, game is over
				return;
			}
			e1.printStackTrace();
		}

		if (!testMode) {
			Operation currentOperation = null;
			while (isRunning()) {
				if (System.currentTimeMillis() > (lastTime + 500)) {
					lastOp = null; // reset the last operation after half a
									// second
				}

				synchronized (lock) {
					if (bc != null) {
						currentOperation = bc.getLastInput();
					}
				}

				// Ensure that the last operation is not the same as the current
				// one,
				// to avoid repeated inputs from same key press
				if (currentOperation != null && lastOp != currentOperation) {
					processCommand(currentOperation);
					lastOp = currentOperation;
					lastTime = System.currentTimeMillis();
				}
			}
		}
	}

	/**
	 * Moves the player in the grid taking the direction as a parameter. The
	 * string must be one of the names defined in ::Command.Operation
	 */
	public void processCommand(Operation operation) {
		try {
			// We send START_GAME and JOIN_GAME to the dedicated listening
			// port of the server. All the other messages go to the specific
			// socket for this client.
			if (operation == Command.Operation.START_GAME || operation == Command.Operation.JOIN_GAME) {
				Utility.sendMessage(clientSocket, new Command(playerName, operation), listenIp, listenPort);
			} else {
				// Don't send messages until the game is started.
				if (isStarted()) {
					Utility.sendMessage(clientSocket, new Command(playerName, operation), ip, port);
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
	 * @throws IOException
	 * 
	 */
	private void joinGame() throws IOException {

		processCommand(Operation.JOIN_GAME);
		// Wait for an ack to know which port to finally communicate with.
		byte[] receiveData = new byte[1024 * 100];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		ip = receivePacket.getAddress();
		port = receivePacket.getPort();

		// Starting a separate thread for listening to updated grids.
		Thread listen = new Thread(new Runnable() {

			@Override
			public void run() {
				while (isRunning()) { // Iterate until LEAVE_GAME is received.
					Object grid = Utility.receiveMessage(clientSocket);
					if (grid instanceof Command.Operation) {
						Command.Operation c = (Command.Operation) grid;
						if (c == Command.Operation.LEAVE_GAME) {
							shutDown();
						} else {
							if (!isStarted()) {
								setStarted(true);
							}
						}
					} else { // we need to refresh the grid.
						if (showGui) {
							synchronized (lock) {
								if (bc == null) {
									bc = new BombermanClient((Square[][]) grid);
									bc.setVisible(true);
								} else {
									if (grid instanceof Square[][]) {
										bc.refresh((Square[][]) grid);
									}
								}
							}
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

	public void shutDown() {
		setRunning(false);
		clientSocket.close();

		if (showGui) {
			bc.dispose();
		}
	}

	public boolean isRunning() {
		synchronized (running) {
			return running;
		}
	}

	private void setRunning(boolean val) {
		synchronized (running) {
			running = val;
		}
	}

	private synchronized Boolean isStarted() {
		synchronized (started) {
			return started;
		}
	}

	private void setStarted(boolean val) {
		synchronized (started) {
			started = val;
		}
	}

}
