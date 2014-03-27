package bomberman.test;

import bomberman.client.Client;

/**
 * Runs an instance of the client in a separate thread
 * Used for testing purposes
 * 
 * @author spbyron
 *
 */
public class ClientThread extends Thread {

	// Underlying client object, that the thread runs
	private Client client;
	private boolean isTesting;

	/**
	 * Create a new ClientThread, giving the Client the given name
	 * @param player
	 */
	public ClientThread(String player) {
		this(player, 9876, true);
	}
	
	public ClientThread(String player, int port, boolean gui) {
		this(player, "127.0.0.1", port, gui, true);
	}
	
	public ClientThread(String player, String host, int port, boolean gui, boolean isTesting) {
		this.isTesting = isTesting;
		try {
			client = new Client(player, host, port, gui);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		client.startClient(isTesting);
		
		while (client.isRunning()) {
			
		}
	}

	/**
	 * Returns the Client object that is being run by the thread
	 * @return
	 */
	public Client getClient() {
		return client;
	}
	
	public void shutdown() {
		client.shutDown();
	}
	
	public String getLogFile() {
		return client.getLogFile();
	}
	
}
