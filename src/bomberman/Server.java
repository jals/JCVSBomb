package bomberman;

import java.awt.Point;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import bomberman.Command.Operation;

public class Server {

	private List<Player> listOfPlayers;
	private Object[][] grid;
	private DatagramSocket serverSocket;
	private Object refreshed;

	public Server() throws SocketException {
		setListOfPlayers(new ArrayList<Player>());
		grid = new Object[10][10];
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

	public void setGrid(Object[][] grid) {
		this.grid = grid;
	}

	public void refreshGrid() {
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				if (grid[x][y] instanceof Player) {
					grid[x][y] = null;
				}
			}
			for (Player p : listOfPlayers) {
				grid[(int) p.getLocation().getX()][(int) p.getLocation().getY()] = p;
			}
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
//			System.out.println("hello");
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

	public Worker(Server server, Object object, Player p, DatagramSocket socket) {
		refreshed = object;
		this.p = p;
		this.server = server;
		this.socket = socket;
//		System.out.println(this.socket.getLocalPort());
//		System.out.println(this.socket.getPort());
		Utility.sendMessage(socket, "hello", p.getAddress(), p.getPort()); //just an ack
//		System.out.println(this.socket.getLocalPort());
//		System.out.println(this.socket.getPort());
	}

	public void run() {

		while (true) {
			Object o = Utility.receiveMessage(socket);
			Command c = (Command) o;
			InetAddress IPAddress = null;
			int port = 0;
			Point location = p.getLocation();
			Point newLocation = getLocation(c.getOperation(), location);
			IPAddress = p.getAddress();
			port = p.getPort();
			p.setLocation(newLocation);
			server.refreshGrid();
			// TODO Do something with the O
			Utility.sendMessage(socket, server.getGrid(), IPAddress, port);
		}

	}

	private Point getLocation(Operation operation, Point location) {
		int x = (int) location.getX();
		int y = (int) location.getY();
		int newX = 5, newY = 5;

		if (Operation.MOVE_DOWN == operation) {
			newX = Math.min(x + 1, 9);
			newY = y;
		} else if (Operation.MOVE_UP == operation) {
			newY = Math.max(x - 1, 0);
			newY = y;
		} else if (Operation.MOVE_LEFT == operation) {
			newY = Math.max(y - 1, 0);
			newX = x;
		} else if (Operation.MOVE_RIGHT == operation) {
			newY = Math.min(y + 1, 9);
			newX = x;
		}
		return new Point(newX, newY);
	}

}
