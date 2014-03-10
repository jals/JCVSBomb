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
import bomberman.common.model.Box;
import bomberman.common.model.Door;
import bomberman.common.model.Explosion;
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
	private BombFactory bombFactory;

	/**
	 * Instantiates a Server object with the given map
	 * @param port
	 * @param testing
	 * @param map
	 * @throws SocketException
	 */
	public Server(int port, boolean testing, String map) throws SocketException {
		listOfPlayers = new ArrayList<Player>();
		/**Set the map variable to the empty string to see the box creation.
			//TODO Read files in a new way to allow for boxes with things inside of them
		**/
		//map = "";
		grid = new Model(map, null);
		serverSocket = new DatagramSocket(port);
		gridLock = new ReentrantReadWriteLock();

		if (!grid.hasDoor()) {
			Box b = grid.getFreeBox();
			door = new Door(new Point(b.getLocation().x, b.getLocation().y), false);
			b.setDoor(door);
		} else {
			door = grid.getDoor();
			grid.getBoard()[door.getLocation().x][door.getLocation().y]
					.addObject(door); // if loaded from file
		}

		if(map.isEmpty()){
			Box b = grid.getFreeBox();
			PowerUp p = new PowerUp(new Point(b.getLocation().x, b.getLocation().y));
			b.setPowerUp(p);
		}
		
		bombFactory = new BombFactory(this);
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
	 * 
	 * @return the grid of Square of Objects
	 */
	protected Square[][] getGrid() {
		return grid.getBoard();
	}
	
	protected ReadWriteLock getLock() {
		return gridLock;
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
				grid.getBoard()[(int) p.getLocation().getX()][(int) p.getLocation().getY()].addObject(p);
				if(!p.getName().equals("Enemy")){ //Don't do specific player actions for enemies
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
			}
			// Check if two players are at the same location.
			// if yes, kill both of them
			for (Player p : listOfPlayers) {
				for (Player q : listOfPlayers) {
					if (p != q && p.getLocation().equals(q.getLocation())) {
						p.takeHit();
						q.takeHit();
					}
				}
			}
			prunePlayers();
		}

		logger.logRefresh();
		synchronized (gridLock.readLock()) {
			logger.logGrid(grid);
		}
	}

	/**
	 * 
	 */
	protected void prunePlayers() {
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

			try {
				done = addPlayer(packet, workers, c, false);
			} catch (SocketException e) {
				System.err.println("ERROR: Couldn't add player.");
			}
			
			// Log the command
			Player player = getPlayer(c.getPlayer());
			if (player != null) {
				logger.logCommand(c, player.getIdentifier());
			}
		}
		// TODO (Sean Byron): What to do with this while testing?
		// TODO (Jarred Linthorne): Figure out a way to represent him differently
		// TODO (Vinayak Bansal): If an enemy hits a player, the enemy dies too.
		// Do we want this?
		// TODO (Jarred Linthorne): The enemy can open doors, as of now
		final Player enemy = getNewPlayer("Enemy");
		listOfPlayers.add(enemy);
		new Thread(new Enemy(this, enemy)).start();
		
		bombFactory.start();
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

			try {
				addPlayer(packet, workers, c, true);
			} catch (SocketException e) {
				System.err.println("ERROR: Couldn't add player.");
			}
			
			// Log the command
			Player player = getPlayer(c.getPlayer());
			if (player != null) {
				logger.logCommand(c, player.getIdentifier());
			}
			
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
	 * @param hasStarted
	 * 			  : True if the game has started
	 * @return: True if it was a start_game call
	 */
	private boolean addPlayer(DatagramPacket packet, List<Worker> workers,
			Command c, boolean hasStarted) throws SocketException {
		Player p = null;

		// if JOIN_GAME is received add the player to the game
		if (c.getOperation() == Command.Operation.JOIN_GAME) {
			p = getNewPlayer(c.getPlayer());
			p.setAddress(packet.getAddress());
			p.setPort(packet.getPort());
			if (!hasStarted) {
				listOfPlayers.add(p);
			}
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
	
	private Player getNewPlayer(String name) {
		Player toReturn;
		if(name.equals("Enemy")){
			toReturn = new Player(name, 0);
		} else {
			toReturn = new Player(name, playerId);
			playerId++;
			if (playerId > 4) { // Only have 4 different colours for players
				playerId = 1;
			}
		}
		
		toReturn.setIsAlive(true);
		if (!isTesting) {
			// Random location if we are not testing.
			toReturn.setLocation(getFreePoint());
		} else {
			int numPlayers;
			synchronized (gridLock.readLock()) {
				numPlayers = grid.getBoard()[1][1].numPlayers();
			}
			if (numPlayers == 0) {
				toReturn.setLocation(new Point(1, 1));
			} else {
				toReturn.setLocation(new Point(Model.BOARD_SIZE - 2,
						Model.BOARD_SIZE - 2));

			}
		}
		return toReturn;
		
	}

	protected void addBomb(int x, int y) {
		Random rand = new Random();
		int time = rand.nextInt(3000) + 3000; //between 3 and 6 seconds
		synchronized (gridLock.writeLock()) {
			Bomb b = new Bomb(new Point(x, y), time);
			grid.getBoard()[x][y].addObject(b);
			bombFactory.addBomb(b);
		}
	}

	protected void bombExploded(int x, int y) {
		synchronized (gridLock.writeLock()) {
			grid.getBoard()[x][y].removeBomb();
			grid.getBoard()[x][y].addObject(new Explosion(true, new Point(x, y)));
			for (Player p: listOfPlayers) {
				if (p.getLocation().distance(new Point(x,y)) <= 1) {
					p.takeHit();
				}
			}
			
			for(Box b: grid.getBoxes()){
				if(b.getLocation().distance(new Point(x, y)) <=1 ){
					if(b.getDoor() != null){
						grid.getBoard()[b.getLocation().x][b.getLocation().y].addObject(b.getDoor());
						b.setDoor(null);
					} else if(b.getPowerUp() != null){
						grid.getBoard()[b.getLocation().x][b.getLocation().y].addObject(b.getPowerUp());
						b.setPowerUp(null);
					}
					
					grid.getBoard()[b.getLocation().x][b.getLocation().y].removeBox();
				}
			}
			final Point p = new Point(x, y);
			Thread explosion = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					grid.getBoard()[p.x][p.y].removeExplosion();
				}

			});
			explosion.start();
			
			prunePlayers();
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
	
	private Player getPlayer(String name) {
		for(Player player : listOfPlayers) {
			if (player.getName().equals(name)) {
				return player;
			}
		}
		return null;
	}
}