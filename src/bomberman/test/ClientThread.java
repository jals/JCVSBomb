package bomberman.test;

import bomberman.Client;

public class ClientThread extends Thread {
	
	private static Client client;
	
	public ClientThread(String player) {
		try {
			client = new Client(player);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			client.main(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Client getClient() {
		return client;
	}
	
}
