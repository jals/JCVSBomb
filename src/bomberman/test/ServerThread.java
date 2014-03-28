package bomberman.test;

import java.net.SocketException;

import bomberman.server.Server;

/**
 * Runs an instance of the Server in a separate thread Used for testing purposes
 * 
 * @author spbyron
 * 
 */
public class ServerThread extends Thread {

	private Server server;

	public ServerThread() {
		this(9876, false);
	}

	public ServerThread(int port, boolean enemies) {
		this(port, true, enemies);
	}
	
	public ServerThread(boolean testing){
		this(9876, testing, true);
	}
	
	public ServerThread(int port, boolean testing, boolean enemies) {
		try {
			server = new Server(port, testing, enemies);
		} catch (SocketException e) {
			System.err.println("Port is already active (another server may already be running)");
		}
	}

	/**
	 * Run the thread
	 */
	public void run() {
		server.startServer();

		while (server.isRunning()) {
		}

	}

	/**
	 * Returns the path to the log file the server has been logging into
	 * 
	 * @return
	 */
	public String getLogFile() {
		return server.getLogFile();
	}

	public void shutdown() {
		server.shutdownServer();
	}

	public Server getServer() {
		return server;
	}

}