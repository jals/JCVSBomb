package bomberman.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Thread for logging Commands as they are processed by the server Also logs any
 * errors (e.g. trying to move into a wall), and logs the game state everytime
 * the server sends out a refresh of the board
 * 
 * @author spbyron
 * 
 */

public abstract class Logger extends Thread {

	// String constants. Are written in the log file
	public static final String PLAYER = "PLAYER";
	public static final String COMMAND = "COMMAND";
	public static final String OPERATION = "OPERATION";
	public static final String BOARD_STATE = "BOARD_STATE";
	public static final String REFRESH = "REFRESH";
	public static final String ERROR = "ERROR";
	public static final String ID = "ID";
	public static final String TIME = "TIME";

	// A BufferedWriter for writing out to the log file
	protected BufferedWriter log;
	// The full path to the log file
	private String logFilePath;
	// Boolean to control when the thread should shut down
	private boolean run = true;

	/**
	 * Create a new logger object Write the log to the given file
	 * 
	 * @param logFilePath
	 */
	public Logger(String logFilePath) {
		this.logFilePath = logFilePath;

		try {
			File directory = new File("logs");
			directory.mkdir();
			log = new BufferedWriter(new FileWriter(new File(logFilePath)));
		} catch (IOException e) {
			System.out.println("ERROR: Could not create log file: "
					+ logFilePath);
			e.printStackTrace();
		}
	}

	/**
	 * Loop while run is true (we haven't been shut down)
	 */
	public void run() {
		while (run) {
		}

		// Close the buffered reader before exiting
		try {
			close();
		} catch (IOException e) {
			System.err.println("ERROR: Could not close file.");
		}
	}

	/**
	 * Close the log file
	 * 
	 * @throws IOException
	 */
	private void close() throws IOException {
		log.close();
	}

	/**
	 * Get the log file location
	 * 
	 * @return
	 */
	public String getLogFile() {
		return logFilePath;
	}

	/**
	 * Stop the logger thread
	 */
	public void shutdown() {
		run = false;
	}

	/**
	 * Write a string to the log
	 * 
	 * @param s
	 */
	protected void writeStringToLog(String s) {
		synchronized (log) {
			try {
				log.write(s);
				log.newLine();
				log.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
