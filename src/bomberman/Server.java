package bomberman;

import java.awt.Point;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server {

	private List<Player> listOfPlayers;
	private Square[][] grid;
	private DatagramSocket serverSocket;
	private Object refreshed;

	public Server() throws SocketException {
		setListOfPlayers(new ArrayList<Player>());
		grid = new Square[10][10];
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				grid[x][y] = new Square();
			}
		}
		serverSocket = new DatagramSocket(9876);
		refreshed = new Object();
	}

	public Server(String filename) {
		setListOfPlayers(new ArrayList<Player>());
	}

	public List<Player> getListOfPlayers() {
		return listOfPlayers;
	}

	public void setListOfPlayers(List<Player> listOfPlayers) {
		this.listOfPlayers = listOfPlayers;
	}

	public Object[][] getGrid() {
		return grid;
	}

	public synchronized void refreshGrid() {
		// System.out.println("ggg");
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				grid[x][y].removePlayers();
			}
		}
		for (Player p : listOfPlayers) {
			grid[(int) p.getLocation().getX()][(int) p.getLocation().getY()].addObject(p);
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
		while (true) {
			server.getServerSocket().receive(packet);
			// System.out.println("hello");
			Object o = Utility.deserialize(packet.getData());
			Command c = (Command) o;
			Player p = null;
			if (c.getOperation() == Command.Operation.JOIN_GAME) {
				p = new Player(c.getPlayer());
				p.setIsAlive(true);
				p.setLocation(new Point(0, 0));
				p.setAddress(packet.getAddress());
				p.setPort(packet.getPort());
				server.listOfPlayers.add(p);
				server.refreshGrid();
				DatagramSocket socket = new DatagramSocket();
				new Worker(server, server.refreshed, p, socket).start();
			}
		}
	}

	public DatagramSocket getServerSocket() {
		return serverSocket;
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
		// System.out.println(this.socket.getLocalPort());
		// System.out.println(this.socket.getPort());
		Utility.sendMessage(socket, "hello", p.getAddress(), p.getPort()); // just
																			// an
																			// ack
		// System.out.println(this.socket.getLocalPort());
		// System.out.println(this.socket.getPort());
		new Thread() {
			public void run() {
				while (true) {
					synchronized (refreshed) {
						try {
							refreshed.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
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

		while (true) {
			Object o = Utility.receiveMessage(socket);
			// System.out.println("dsaf");
			Command c = (Command) o;
			Point location = p.getLocation();
			Point newLocation = Utility.getLocation(c.getOperation(), location);
			p.setLocation(newLocation);
			server.refreshGrid();
			// Utility.sendMessage(socket, server.getGrid(), p.getAddress(),
			// p.getPort());
		}

	}

}
