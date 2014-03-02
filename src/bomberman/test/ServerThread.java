package bomberman.test;

import java.io.IOException;

import bomberman.Server;

class ServerThread extends Thread {

	public void run() {
		try {
			String[] args = { "1" };
			Server.main(args);
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