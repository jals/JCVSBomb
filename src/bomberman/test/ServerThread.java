package bomberman.test;

import java.io.IOException;

import bomberman.Server;

class ServerThread extends Thread {
	
	public void run() {
		try {
			Server.main(null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getLogFile() {
		return Server.getLogFile();
	}
	
}