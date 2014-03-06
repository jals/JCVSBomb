package bomberman.test;

import java.net.SocketException;

import bomberman.Server;

/**
 * Runs an instance of the Server in a separate thread Used for testing purposes
 * 
 * @author spbyron
 * 
 */
class ServerThread extends Thread {

	private Server server;
	
	public ServerThread() {
		try {
			server = new Server(9876, true);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Run the thread
	 */
	public void run() {
		server.startServer();
		
		while(server.isRunning()) {
		}
		
	}

	/**
	 * Returns the path to the log file the server has been logging into
	 * 
	 * @return
	 */
	public String getLogFile() {
		return Server.getLogFile();
	}
	
	public void shutdown() {
		server.shutdownServer();
	}

}