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

	/**
	 * Create a new ClientThread, giving the Client the given name
	 * @param player
	 */
	public ClientThread(String player) {
		this(player, 9876);
	}
	
	public ClientThread(String player, int port) {
		try {
			client = new Client(player, "127.0.0.1", port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		client.startClient(true, null); // no need to send a scanner
		
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
	
}
