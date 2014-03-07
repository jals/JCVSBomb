package bomberman.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import bomberman.common.Command;
import bomberman.common.model.Model;
import bomberman.common.model.Player;
import bomberman.common.model.Square;


/**
 * Thread for logging Commands as they are processed by the server
 * Also logs any errors (e.g. trying to move into a wall), and logs
 * the game state everytime the server sends out a refresh of the board
 * 
 * @author spbyron
 *
 */

public class Logger extends Thread {

	// String constants. Are writted in the log file
	public static final String PLAYER = "PLAYER";
	public static final String COMMAND = "COMMAND";
	public static final String OPERATION = "OPERATION";
	public static final String BOARD_STATE = "BOARD_STATE";
	public static final String REFRESH = "REFRESH";
	public static final String ERROR = "ERROR";
	public static final String ID = "ID";
	
	// A BufferedWriter for writing out to the log file
	private static BufferedWriter log;
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
			System.out.println("ERROR: Could not create log file: " + logFilePath);
			e.printStackTrace();
		}
	}

	/**
	 * Create a new logger object with the date and time as a filename
	 */
	public Logger() {
		this("logs/bomberman-" + getDate() + ".log");
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
	 * Log a command
	 * 
	 * @param command
	 */
	public void logCommand(Command command, int player) {
		writeStringToLog(COMMAND + "," + PLAYER + "=" + command.getPlayer()
				+ "," + OPERATION + "=" + command.getOperation() + "," + ID + "=" + player);
	}
	
	/**
	 * Log a command
	 * 
	 * @param command
	 */
	/*public void logCommand(Command command) {
		writeStringToLog(COMMAND + "," + PLAYER + "=" + command.getPlayer()
				+ "," + OPERATION + "=" + command.getOperation());
	}*/

	/**
	 * Log a grid refresh
	 * 
	 */
	public void logRefresh() {
		writeStringToLog(REFRESH);
	}

	/**
	 * Log the state of the grid
	 * 
	 * @param model
	 */
	public void logGrid(Model model) {
		try {
			String grid = new String();
			Square[][] board = model.getBoard();

			synchronized (log) {
				for (int i = 0; i < Model.BOARD_SIZE; i++) {
					for (int j = 0; j < Model.BOARD_SIZE; j++) {
						if (board[i][j] == null) {
							// Border
							if (i==0 || i==11) {
								grid+="-";
							} else {
								grid+="|";
							}
						} else if (!board[i][j].canGo()) {
							// Wall
							grid += "w";
						} else if (board[i][j].numPlayers() > 0) {
							// Player
							Player player = board[i][j].getPlayer();
							grid += player.getIdentifier();
						} else if (board[i][j].hasDoor()) {
							// Door
							grid += "D";
						} else {
							// Open space
							grid += " ";
						}
					}
					
					// Make it look all pretty
					if (i>9) {
						log.write(BOARD_STATE + ",Row" + i + "," + grid);
					} else {
						log.write(BOARD_STATE + ",Row " + i + "," + grid);
					}
					
					log.newLine();
					grid = new String();
				}
				log.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Log an error
	 * 
	 * @param command
	 * @param error
	 */
	public void logError(Command command, String error) {
		writeStringToLog(ERROR + "," + PLAYER + "=" + command.getPlayer() + ","
				+ OPERATION + "=" + command.getOperation() + "," + error);
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
	 * Formats and gets the current date, for the log file name
	 * 
	 * @return
	 */
	private static String getDate() {
		SimpleDateFormat formatter = new SimpleDateFormat();
		formatter.applyPattern("yyyy-MM-dd-HH-mm");
		return formatter.format(new Date());
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
	private void writeStringToLog(String s) {
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
