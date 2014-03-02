package bomberman;

import java.awt.Point;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Server {

	private List<Player> listOfPlayers;
	private  Model grid;
	private DatagramSocket serverSocket;
	private Object refreshed;
	private static Logger logger;

	public Server() throws SocketException {
		setListOfPlayers(new ArrayList<Player>());
		grid = new Model("", null);//Square[10][10];
//		for (int x = 0; x < 10; x++) {
//			for (int y = 0; y < 10; y++) {
//				grid[x][y] = new Square();
//			}
//		}
		serverSocket = new DatagramSocket(9876);
		refreshed = new Object();
		logger = new Logger();
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
		return grid.getBoard()[x][y].hasWall();
	}

	public synchronized void refreshGrid() {
		// System.out.println("ggg");
		for (int x = 1; x < 11; x++) {
			for (int y = 1; y < 11; y++) {
				grid.getBoard()[x][y].removePlayers();
			}
		}
		for (Player p : listOfPlayers) {
			grid.getBoard()[(int) p.getLocation().getX()][(int) p.getLocation().getY()]
					.addObject(p);
		}
		synchronized (refreshed) {
			refreshed.notifyAll();
		}
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		Server server = new Server();
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
			
//			System.out.println(c.getOperation());
			Player p = null;
			if (c.getOperation() == Command.Operation.JOIN_GAME) {
				p = new Player(c.getPlayer());
				p.setIsAlive(true);
				p.setLocation(new Point(1, 1));
				p.setAddress(packet.getAddress());
				p.setPort(packet.getPort());
				server.listOfPlayers.add(p);
				server.refreshGrid();
				DatagramSocket socket = new DatagramSocket();
				workers.add(new Worker(server, server.refreshed, p, socket));
			} else if (c.getOperation() == Command.Operation.START_GAME) {
				done = true;
			}
		}
//		System.out.println("hello");
		for (Worker worker : workers) {
			worker.start();
		}
	}

	public DatagramSocket getServerSocket() {
		return serverSocket;
	}

	public Logger getLogger() {
		return logger;
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
		Utility.sendMessage(socket, "joined", p.getAddress(), p.getPort()); // just
		// an
		// ack
		// System.out.println(this.socket.getLocalPort());
		// System.out.println(this.socket.getPort());
		// System.out.println(this.socket.getLocalPort());
		// System.out.println(this.socket.getPort());
		new Thread() {
			public void run() {
				while (true) {
					synchronized (refreshed) {
						try {
							refreshed.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// System.out.println("hello" + p.getName());
						Utility.sendMessage(socket, server.getGrid(),
								p.getAddress(), p.getPort());

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
			// System.out.println("dsaf");
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
				if (server.canGo(location.x, location.y));
				p.setLocation(newLocation);
			} else if (c.getOperation() == Command.Operation.LEAVE_GAME) {
				server.removePlayer(p);
				Utility.sendMessage(socket, Command.Operation.LEAVE_GAME, p.getAddress(), p.getPort());
				break;
			} else {
				// TODO: Drop Bomb
			}
			server.refreshGrid();
		}
		server.refreshGrid();
	}

}
