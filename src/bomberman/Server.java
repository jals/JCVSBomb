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
	private Door d;
	private boolean isTesting;

	public Server() throws SocketException {
		setListOfPlayers(new ArrayList<Player>());
		grid = new Model(
				"M:\\git\\JCVSBomb\\src\\bomberman\\gui\\defaultMap.txt", null);// Square[10][10];
		// for (int x = 0; x < 10; x++) {
		// for (int y = 0; y < 10; y++) {
		// grid[x][y] = new Square();
		// }
		// }
		Point doorPoint = getFreePoint();
		d = new Door(doorPoint, false);
		grid.getBoard()[doorPoint.x][doorPoint.y].addObject(d);
		serverSocket = new DatagramSocket(9876);
		refreshed = new Object();
		logger = new Logger();
	}

	public Server(boolean testing) throws SocketException {
		this();
		isTesting = false;
	}

	public Server(String filename) {
		setListOfPlayers(new ArrayList<Player>());
	}

	public List<Player> getListOfPlayers() {
		return listOfPlayers;
	}

	public void removePlayer(Player p) {
		listOfPlayers.remove(p);
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
			int x = random.nextInt(10) + 1;
			int y = random.nextInt(10) + 1;
			if (grid.getBoard()[x][y].numPlayers() == 0
					&& !grid.getBoard()[x][y].hasWall()) {
				isOkay = true;
				toReturn = new Point(x, y);
			}
		}
		return toReturn;
	}

	public synchronized void refreshGrid() {
		// System.out.println("ggg");
		for (int x = 1; x < 11; x++) {
			for (int y = 1; y < 11; y++) {
				grid.getBoard()[x][y].removePlayers();
			}
		}
		for (Player p : listOfPlayers) {
			grid.getBoard()[(int) p.getLocation().getX()][(int) p.getLocation()
					.getY()].addObject(p);
			if(p.getLocation().x == d.getLocation().x && p.getLocation().y == d.getLocation().y){
				d.setVisible(true);
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

		try {
			logger.logRefresh();
			logger.logGrid(grid);
		} catch (IOException e) {
			// Unable to log the refresh
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		Server server = null;
		if (Integer.parseInt(args[0]) == 0) {
			server = new Server(false);
		} else {
			server = new Server(true);
		}
		byte[] receiveData = new byte[1024 * 100];
		DatagramPacket packet = new DatagramPacket(receiveData,
				receiveData.length);
		List<Worker> workers = new LinkedList<Worker>();
		boolean done = false;
		while (!done) {
			server.getServerSocket().receive(packet);
			Object o = Utility.deserialize(packet.getData());
			Command c = (Command) o;

			// Log the command
			logger.logCommand(c);

			// System.out.println(c.getOperation());
			done = server.addPlayer(packet, workers, done, c);
		}
		// System.out.println("hello");
		for (Worker worker : workers) {
			worker.start();
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
				p.setLocation(new Point(1, 1));
			}
			p.setAddress(packet.getAddress());
			p.setPort(packet.getPort());
			listOfPlayers.add(p);
			refreshGrid();
			DatagramSocket socket = new DatagramSocket();
			workers.add(new Worker(this, refreshed, p, socket));

			// if START_GAME is received start the game
		} else if (c.getOperation() == Command.Operation.START_GAME) {
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
			try {
				server.getLogger().logCommand(c);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (c.getOperation().isMove()) {
				Point location = p.getLocation();
				Point newLocation = Utility.getLocation(c.getOperation(),
						location);
				if (server.canGo(newLocation.x, newLocation.y)) {
					p.setLocation(newLocation);
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
		Utility.sendMessage(socket, Command.Operation.LEAVE_GAME,
				p.getAddress(), p.getPort());

	}

}
