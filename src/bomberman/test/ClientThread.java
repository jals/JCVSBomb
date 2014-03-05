package bomberman.test;

import bomberman.Client;

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
		try {
			client = new Client(player, "127.0.0.1", 9876);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the Client object that is being run by the thread
	 * @return
	 */
	public Client getClient() {
		return client;
	}
	
}
