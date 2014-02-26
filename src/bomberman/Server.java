package bomberman;

import java.awt.Point;
import java.io.IOException;
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

	public Server() throws SocketException {
		setListOfPlayers(new ArrayList<Player>());
		grid = new Object[10][10];
		serverSocket = new DatagramSocket(9876);
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
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		Server server = new Server();
		Object o = Utility.receiveMessage(server.getServerSocket());
		Command c = (Command) o;
		if (c.getOperation() == Command.Operation.JOIN_GAME) {
			Player p = new Player(c.getPlayer());
			p.setIsAlive(true);
			p.setLocation(new Point (0,0));
			p.setAddress(InetAddress.getByName("localhost")); //fix hardcoding
			p.setPort(9876); // Fix hardcoding
			server.listOfPlayers.add(p);
		}
		
		new Worker(server).start();
	}

	public DatagramSocket getServerSocket() {
		return serverSocket;
	}

}

class Worker extends Thread {
	Server server;

	public Worker(Server server) {
		this.server = server;
	}

	public void run() {

		while (true) {
			Object o = Utility.receiveMessage(server.getServerSocket());
			Command c = (Command) o;
			String playerName = c.getPlayer();
			InetAddress IPAddress = null;
			int port = 0;
			for (Player player : server.getListOfPlayers()) {
				if (player.getName().equals(playerName)) {
					Point location = player.getLocation();
					Point newLocation = getLocation(c.getOperation(), location);
					IPAddress = player.getAddress();
					port = player.getPort();
					player.setLocation(newLocation);
					server.refreshGrid();
				}

			}
			// TODO Do something with the O
			Utility.sendMessage(server.getServerSocket(), server.getGrid(),
					IPAddress, port);
		}

	}

	private Point getLocation(Operation operation, Point location) {
		return location;
	}

}
