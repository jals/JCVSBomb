/**
 * Author: Vinayak Bansal
 * Dated: March 2, 2014
 * 
 * This class interacts with all the clients (Players).
 * It has an instance of the model for keeping track of
 * what is happening on grid.
 */

package bomberman.server;

import java.awt.Point;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import bomberman.common.Command;
import bomberman.common.Utility;
import bomberman.common.model.Bomb;
import bomberman.common.model.Door;
import bomberman.common.model.Model;
import bomberman.common.model.Player;
import bomberman.common.model.PowerUp;
import bomberman.common.model.Square;

public class Server {

	// All the players that are alive
	private List<Player> listOfPlayers;
	private Model grid;
	private DatagramSocket serverSocket; // dedicated listening socket
	private static Logger logger;
	private Door door; // where is the door?
	// If we are testing, we put the players at specific locations
	private boolean isTesting;
	private int playerId = 1;
	private boolean running = true;
	private ReadWriteLock gridLock;

	/**
	 * Instantiates a Server object with the given map
	 * @param port
	 * @param testing
	 * @param map
	 * @throws SocketException
	 */
	public Server(int port, boolean testing, String map) throws SocketException {
		listOfPlayers = new ArrayList<Player>();
		grid = new Model(map, null);
		serverSocket = new DatagramSocket(port);

		if (!grid.hasDoor()) {
			Point doorPoint = getFreePoint();
			door = new Door(doorPoint, false);
			grid.getBoard()[doorPoint.x][doorPoint.y].addObject(door);
		} else {
			door = grid.getDoor();
			grid.getBoard()[door.getLocation().x][door.getLocation().y]
					.addObject(door); // if loaded from file
		}

		gridLock = new ReentrantReadWriteLock();
		logger = new Logger();
		logger.start();
		isTesting = testing;
	}

	/**
	 * Instantiates a Server object with the default map
	 * @param port
	 * @param testing
	 * @throws SocketException
	 */
	public Server(int port, boolean testing) throws SocketException {
		this(port, testing, "src/bomberman/gui/defaultMap.txt");
	}

	protected void removePlayer(Player p) {
		listOfPlayers.remove(p);
	}

	/**
	 * Removes the lsat player from the list of players. It is required when a
	 * player dies.
	 */
	private void removeLastPlayer() {
		listOfPlayers.remove(listOfPlayers.size() - 1);
	}

	/**
	 * This method is not synchronized as this is being used only for reading
	 * purposes.
	 * 
	 * @return the grid of Square of Objects
	 */
	protected Square[][] getGrid() {
		return grid.getBoard();
	}

	/**
	 * For the give point, checks to see if there is a wall or not.
	 * 
	 * @param x
	 *            : The x location of the point
	 * @param y
	 *            : The y location of the point
	 * @return: True if there isnt a wall at the given point
	 */
	protected boolean canGo(int x, int y) {
		synchronized (gridLock.readLock()) {
			Square interestedSquare = grid.getBoard()[x][y];
			if (interestedSquare != null) {
				return interestedSquare.canGo();
			}
			return false;
		}
	}

	/**
	 * Randomly returns a square where there are no players, and no walls. It
	 * cannot be a utility method as it needs access to the grid::Model.
	 * 
	 * @return: A point where a player can be placed.
	 */
	private Point getFreePoint() {
		Point toReturn = null;
		Random random = new Random();
		boolean isOkay = false;
		while (!isOkay) { // Keep trying until you find a spot
			int x = random.nextInt(Model.BOARD_SIZE - 2) + 1;
			int y = random.nextInt(Model.BOARD_SIZE - 2) + 1;
			boolean condition;
			synchronized (gridLock.readLock()) {
				condition = grid.getBoard()[x][y].numPlayers() == 0
						&& grid.getBoard()[x][y].canGo();
			}
			if (condition) {
				isOkay = true;
				toReturn = new Point(x, y);
			}
		}
		return toReturn;
	}

	/**
	 * This is an important method that updates the grid. It changes the model,
	 * so it must be synchronized. It updates the location of the players. Then
	 * checks to see if players are at the same location or not. If that is the
	 * case, then it kills them.
	 */
	protected void refreshGrid() {
		synchronized (gridLock.writeLock()) {
			for (int x = 1; x < 11; x++) {
				for (int y = 1; y < 11; y++) {
					grid.getBoard()[x][y].removePlayers();
				}
			}
			// Open the door if the player is at the door.
			for (Player p : listOfPlayers) {
				grid.getBoard()[(int) p.getLocation().getX()][(int) p
						.getLocation().getY()].addObject(p);
				if (door != null) {
					if (p.getLocation().x == door.getLocation().x
							&& p.getLocation().y == door.getLocation().y) {
						door.setVisible(true);
					}
				}
				PowerUp powerUp = grid.getBoard()[p.getLocation().x][p
						.getLocation().y].removePowerUp();
				if (powerUp != null) {
					p.addPowerUp(powerUp);
				}
			}
			// Check if two players are at the same location.
			// if yes, kill both of them
			for (Player p : listOfPlayers) {
				for (Player q : listOfPlayers) {
					if (p != q && p.getLocation().equals(q.getLocation())) {
						p.setIsAlive(false);
						q.setIsAlive(false);
					}
				}
			}
			List<Player> newPlayers = new ArrayList<Player>();
			for (Player p : listOfPlayers) {
				if (p.getIsAlive()) {
					newPlayers.add(p);
				} else {
					grid.getBoard()[p.getLocation().x][p.getLocation().y]
							.removePlayers();
				}
			}
			listOfPlayers = newPlayers;
		}

		logger.logRefresh();
		synchronized (gridLock.readLock()) {
			logger.logGrid(grid);
		}
	}

	public static void main(String[] args) {
		Server server = null;
		if (args.length < 2) {
			System.out
					.println("Please specify two command line arguments. 0/1 for not testing/testing, and port number.");
			return;
		}
		if (Integer.parseInt(args[0]) == 0) {
			try {
				server = new Server(Integer.parseInt(args[1]), false);
			} catch (Exception e) {
				System.err
						.println("ERROR: The server could not be initialized properly! Have you specified a socket?");
			}
		} else {
			try {
				server = new Server(Integer.parseInt(args[1]), true);
			} catch (Exception e) {
				System.err
						.println("ERROR: The server could not be initialized properly! Have you specified a socket?");
			}
		}
		server.startServer();
	}

	/**
	 * Starts up the server. 
	 */
	public void startServer() {
		byte[] receiveData = new byte[1024 * 100];
		DatagramPacket packet = new DatagramPacket(receiveData,
				receiveData.length);
		List<Worker> workers = new LinkedList<Worker>();
		boolean done = false;
		while (!done) {
			try {
				getServerSocket().receive(packet);
			} catch (IOException e) {
				if (e instanceof SocketException) {
					// Socket was closed, just return (no error)
					return;
				}
				System.err
						.println("ERROR: Could not receive packet from a socket.");
			}
			Object o = null;
			try {
				o = Utility.deserialize(packet.getData());
			} catch (Exception e) {
				System.err
						.println("ERROR: Could not deserialize the packet data.");
			}
			Command c = (Command) o;

			// Log the command
			logger.logCommand(c);

			try {
				done = addPlayer(packet, workers, c);
			} catch (SocketException e) {
				System.err.println("ERROR: Couldn't add player.");
			}
		}
		for (Worker worker : workers) {
			worker.start();
		}
		while (isRunning()) {
			try {
				getServerSocket().receive(packet);
			} catch (IOException e) {
				if (e instanceof SocketException) {
					// Socket was closed, just return (no error)
					return;
				}
				System.err
						.println("ERROR: Could not receive packet from a socket.");
			}
			Object o = null;
			try {
				o = Utility.deserialize(packet.getData());
			} catch (Exception e) {
				System.err
						.println("ERROR: Could not deserialize the packet data.");
			}
			Command c = (Command) o;

			// Log the command
			logger.logCommand(c);

			try {
				done = addPlayer(packet, workers, c);
			} catch (SocketException e) {
				System.err.println("ERROR: Couldn't add player.");
			}
			removeLastPlayer();
		}

		serverSocket.close();
		return;
	}

	/**
	 * Handles the JOIN_GAME command and adds a player to the grid. Starts the
	 * game when the START_GAME command is received. NOTE: Once the game starts,
	 * new players requesting to join become spectators.
	 * 
	 * @param packet
	 *            : Required to see where the packet came from.
	 * @param workers
	 *            : List of players that are waiting to run.
	 * @param c
	 *            : What was the command that a client sent.
	 * @return: True if it was a start_game call
	 */
	private boolean addPlayer(DatagramPacket packet, List<Worker> workers,
			Command c) throws SocketException {
		Player p = null;

		// if JOIN_GAME is received add the player to the game
		if (c.getOperation() == Command.Operation.JOIN_GAME) {
			p = new Player(c.getPlayer(), playerId);
			playerId++;
			if (playerId > 4) { // Only have 4 different colours for players
				playerId = 1;
			}
			p.setIsAlive(true);
			if (!isTesting) {
				// Random location if we are not testing.
				p.setLocation(getFreePoint());
			} else {
				int numPlayers;
				synchronized (gridLock.readLock()) {
					numPlayers = grid.getBoard()[1][1].numPlayers();
				}
				if (numPlayers == 0) {
					p.setLocation(new Point(1, 1));
				} else {
					p.setLocation(new Point(Model.BOARD_SIZE - 2,
							Model.BOARD_SIZE - 2));

				}
			}
			p.setAddress(packet.getAddress());
			p.setPort(packet.getPort());
			listOfPlayers.add(p);
			refreshGrid();
			// Create a fresh socket over which the client will now communicate
			DatagramSocket socket = new DatagramSocket();
			workers.add(new Worker(this, p, socket));

			// if START_GAME is received start the game
		} else if (c.getOperation() == Command.Operation.START_GAME) {
			refreshGrid();
			return true;
		}
		return false;
	}

	protected void addBomb(int x, int y) {
		synchronized (gridLock.writeLock()) {
			// TODO Change the fuse time
			// TODO Start a thread for a ticking bomb
			grid.getBoard()[x][y].addObject(new Bomb(new Point(x, y), 100));
		}
	}

	protected DatagramSocket getServerSocket() {
		return serverSocket;
	}

	protected Logger getLogger() {
		return logger;
	}

	public static String getLogFile() {
		return logger.getLogFile();
	}

	public void shutdownServer() {
		running = false;
		getServerSocket().close();
	}

	public synchronized boolean isRunning() {
		return running;
	}
}