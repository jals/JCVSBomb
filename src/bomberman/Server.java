package bomberman;

import java.awt.Point;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Server {

	private List<Player> listOfPlayers;
	private Model grid;
	private DatagramSocket serverSocket;
	private Object refreshed;
	private static Logger logger;
	private Door door;
	private boolean isTesting;

	public Server(int port) throws SocketException {
		setListOfPlayers(new ArrayList<Player>());
		grid = new Model("src/bomberman/gui/defaultMap.txt", null);
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
		refreshed = new Object();

		logger = new Logger();
		logger.start();
	}

	public Server(int port, boolean testing) throws SocketException {
		this(port);
		isTesting = testing;
	}

	public void removePlayer(Player p) {
		listOfPlayers.remove(p);
	}

	public void removeLastPlayer() {
		listOfPlayers.remove(listOfPlayers.size() - 1);
	}

	public void setListOfPlayers(List<Player> listOfPlayers) {
		this.listOfPlayers = listOfPlayers;
	}

	public Square[][] getGrid() {
		return grid.getBoard();
	}

	public synchronized boolean canGo(int x, int y) {
		Square interestedSquare = grid.getBoard()[x][y];
		if (interestedSquare != null) {
			return !interestedSquare.hasWall();
		}
		return false;
	}

	public Point getFreePoint() {
		Point toReturn = null;
		Random random = new Random();
		boolean isOkay = false;
		while (!isOkay) {
			int x = random.nextInt(Model.BOARD_SIZE - 2) + 1;
			int y = random.nextInt(Model.BOARD_SIZE - 2) + 1;
			if (grid.getBoard()[x][y].numPlayers() == 0
					&& !grid.getBoard()[x][y].hasWall()) {
				isOkay = true;
				toReturn = new Point(x, y);
			}
		}
		return toReturn;
	}

	public synchronized void refreshGrid() {
		for (int x = 1; x < 11; x++) {
			for (int y = 1; y < 11; y++) {
				grid.getBoard()[x][y].removePlayers();
			}
		}
		for (Player p : listOfPlayers) {
			grid.getBoard()[(int) p.getLocation().getX()][(int) p.getLocation()
					.getY()].addObject(p);
			if (door != null) {
				if (p.getLocation().x == door.getLocation().x
						&& p.getLocation().y == door.getLocation().y) {
					door.setVisible(true);
				}
			}
		}
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

		synchronized (refreshed) {
			refreshed.notifyAll();
		}

		logger.logRefresh();
		logger.logGrid(grid);
	}

	public static void main(String[] args) {
		Server server = null;
		if(args.length < 2){
			System.out.println("Please specify two command line arguments. 0/1 for not testing/testing, and port number.");
			return;
		}
		if (Integer.parseInt(args[0]) == 0) {
			try {
				server = new Server(Integer.parseInt(args[1]), false);
			} catch (Exception e) {
				System.err.println("ERROR: The server could not be initialized properly! Have you specified a socket?");
			}
		} else {
			try {
				server = new Server(Integer.parseInt(args[1]), true);
			} catch (Exception e) {
				System.err.println("ERROR: The server could not be initialized properly! Have you specified a socket?");
			}
		}
		byte[] receiveData = new byte[1024 * 100];
		DatagramPacket packet = new DatagramPacket(receiveData,
				receiveData.length);
		List<Worker> workers = new LinkedList<Worker>();
		boolean done = false;
		while (!done) {
			try {
				server.getServerSocket().receive(packet);
			} catch (IOException e) {
				System.err.println("ERROR: Could not receive packet from a socket.");
			}
			Object o = null;
			try {
				o = Utility.deserialize(packet.getData());
			} catch (Exception e) {
				System.err.println("ERROR: Could not deserialize the packet data.");
			}
			Command c = (Command) o;

			// Log the command
			logger.logCommand(c);

			try {
				done = server.addPlayer(packet, workers, done, c);
			} catch (SocketException e) {
				System.err.println("ERROR: Couldn't add player.");
			}
		}
		for (Worker worker : workers) {
			worker.start();
		}
		while (true) {
			try {
				server.getServerSocket().receive(packet);
			} catch (IOException e) {
				System.err.println("ERROR: Could not receive packet from a socket.");
			}
			Object o = null;
			try {
				o = Utility.deserialize(packet.getData());
			} catch (Exception e) {
				System.err.println("ERROR: Could not deserialize the packet data.");
			}
			Command c = (Command) o;

			// Log the command
			logger.logCommand(c);

			try {
				done = server.addPlayer(packet, workers, done, c);
			} catch (SocketException e) {
				System.err.println("ERROR: Couldn't add player.");
			}
			server.removeLastPlayer();
		}

	}

	/**
	 * Handles the JOIN_GAME command and adds a player to the grid. Starts the
	 * game when the START_GAME command is received. NOTE: Once the game starts
	 * new players cannot join.
	 * 
	 * @param packet
	 * @param workers
	 * @param done
	 * @param c
	 * @return
	 * @throws SocketException
	 */
	public boolean addPlayer(DatagramPacket packet, List<Worker> workers,
			boolean done, Command c) throws SocketException {
		Player p = null;

		// if JOIN_GAME is received add the player to the game
		if (c.getOperation() == Command.Operation.JOIN_GAME) {
			p = new Player(c.getPlayer());
			p.setIsAlive(true);
			if (!isTesting) {
				p.setLocation(getFreePoint());
			} else {
				if (grid.getBoard()[1][1].numPlayers() == 0) {
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
			DatagramSocket socket = new DatagramSocket();
			workers.add(new Worker(this, refreshed, p, socket));

			// if START_GAME is received start the game
		} else if (c.getOperation() == Command.Operation.START_GAME) {
			refreshGrid();
			done = true;
		}
		return done;
	}

	public DatagramSocket getServerSocket() {
		return serverSocket;
	}

	protected Logger getLogger() {
		return logger;
	}

	public static String getLogFile() {
		return logger.getLogFile();
	}

}

class Worker extends Thread {
	Server server;
	DatagramSocket socket;
	Object refreshed;
	Player p;

	public Worker(final Server server, Object object, final Player p,
			final DatagramSocket socket) {
		refreshed = object;
		this.p = p;
		this.server = server;
		this.socket = socket;
		// just an ack
		Utility.sendMessage(socket, "joined", p.getAddress(), p.getPort());
		new Thread() {
			public void run() {
				while (true) {
					synchronized (refreshed) {
						try {
							refreshed.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Utility.sendMessage(socket, server.getGrid(),
								p.getAddress(), p.getPort());
						if (!p.getIsAlive()) {
							done();
							break;
						}

					}
				}
			}
		}.start();
	}

	public void run() {
		Utility.sendMessage(socket, Command.Operation.START_GAME,
				p.getAddress(), p.getPort()); // just an ack

		while (true) {
			Object o = Utility.receiveMessage(socket);
			Command c = (Command) o;

			// Log the command
			server.getLogger().logCommand(c);

			if (c.getOperation().isMove()) {
				Point location = p.getLocation();
				Point newLocation = Utility.getLocation(c.getOperation(),
						location);
				if (server.canGo(newLocation.x, newLocation.y)) {
					p.setLocation(newLocation);
				} else {
					server.getLogger().logError(c, "Cannot move here");
				}
			} else if (c.getOperation() == Command.Operation.LEAVE_GAME) {
				done();
				break;
			} else {
				// TODO: Drop Bomb
			}
			server.refreshGrid();
		}
		server.refreshGrid();
	}

	private void done() {
		server.removePlayer(p);
		server.refreshGrid();
		Utility.sendMessage(socket, Command.Operation.LEAVE_GAME,
				p.getAddress(), p.getPort());

	}

}
