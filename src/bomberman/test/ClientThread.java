package bomberman.test;

import bomberman.Client;

public class ClientThread extends Thread {
	
	private Client client;
	
	public ClientThread(String player) {
		try {
			client = new Client(player, "locahost", 9876);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
			while(true) {
			}				
	}
	
	public Client getClient() {
		return client;
	}
	
}
