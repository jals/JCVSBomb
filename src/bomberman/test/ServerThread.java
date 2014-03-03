package bomberman.test;

import java.io.IOException;

import bomberman.Server;

/**
 * Runs an instance of the Server in a separate thread
 * Used for testing purposes
 * 
 * @author spbyron
 *
 */
class ServerThread extends Thread {

	/**
	 * Run the thread
	 */
	public void run() {
		try {
			// Start the server in "Test" mode (flag=1) on port 9876
			String[] args = { "1", "9876" };
			// Start the server
			Server.main(args);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the path to the log file the server has been logging into
	 * @return
	 */
	public String getLogFile() {
		return Server.getLogFile();
	}

}