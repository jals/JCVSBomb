package bomberman;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import bomberman.gui.BombermanClient;

/**
 * 
 * Client class which handles one player's UDP communication to the server and
 * instantiates the GUI.
 * 
 */
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

	public Client(String playerName, String host, int port) throws Exception {

		this.playerName = playerName;
		clientSocket = new DatagramSocket();
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
	public static void main(String[] args) {
		if(args.length < 2){
			System.out.println("Please specify two command line arguments. localhost, and port number.");
			return;
		}
		System.out.println("Enter a player name: \n");
		@SuppressWarnings("resource")
		Scanner a = new Scanner(System.in);
		Client client = null;
		try {
			client = new Client(sanitizeName(a.nextLine()), args[0],
					Integer.parseInt(args[1]));
		} catch (Exception e) {
			System.err.println("ERROR: Client could not be created properly.\n");
			return;
		}
		System.out.println("Enter player commands: \n");
		while (true) {
			try {
				client.move(a.nextLine());
			} catch (Exception e) {
				System.err.println("ERROR: Command failed.\n");
				
			}
		}
	}

	/**
	 * Moves the player in the grid taking the direction as a parameter.
	 * 
	 * @param direction
	 * @throws Exception
	 */
	public void move(String direction) throws Exception {
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

	/**
	 * Handles the communication with the server that is necessary in order to
	 * join a game. Creates a Thread to handle
	 * 
	 * @throws Exception
	 */
	private void joinGame() throws Exception{

		move("join_game");
		// Wait for an ack to know which port to finally communicate with.
		byte[] receiveData = new byte[1024 * 100];
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);
		clientSocket.receive(receivePacket);
		ip = receivePacket.getAddress();
		port = receivePacket.getPort();

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
	 * Getter for playerName
	 * 
	 * @return
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * Setter for playerName
	 * 
	 * @param playerName
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	/**
	 * Getter for grid
	 * 
	 * @return
	 */
	public Object[][] getGrid() {
		return grid;
	}

	/**
	 * Setter for grid.
	 * 
	 * @param grid
	 */
	public void setGrid(Object[][] grid) {
		this.grid = grid;
	}

	/**
	 * Static method for removing spaces and non-alphanumeric characters.
	 * 
	 * @param name
	 * @return
	 */
	private static String sanitizeName(String name) {
		// Remove anything not alpha-numerical
		name = name.replaceAll("[^A-Za-z0-9 ]", "");

		// Remove spaces
		name = name.replaceAll(" ", "");

		return name;
	}

}
