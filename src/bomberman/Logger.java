package bomberman;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger extends Thread {

	private static final String PLAYER = "PLAYER";
	private static final String COMMAND = "COMMAND";
	private static final String OPERATION = "OPERATION";
	private static final String BOARD_STATE = "BOARD_STATE";
	private static final String REFRESH = "REFRESH";
	private static final String ERROR = "ERROR";

	private static BufferedWriter log;
	private String fileName;

	private boolean run = true;

	/**
	 * Create a new logger object Write the log to the given file
	 * 
	 * @param fileName
	 */
	public Logger(String fileName) {
		this.fileName = fileName;

		try {
			File directory = new File("logs");
			directory.mkdir();
			log = new BufferedWriter(new FileWriter(new File(fileName)));
		} catch (IOException e) {
			System.out.println("ERROR: Could not create log file: " + fileName);
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
	 * Loop while run is true (haven't shut down)
	 */
	public void run() {
		while (run) {
		}

		try {
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Log a command
	 * 
	 * @param command
	 * @throws IOException
	 */
	public void logCommand(Command command) {
		writeStringToLog(COMMAND + "," + PLAYER + "=" + command.getPlayer()
				+ "," + OPERATION + "=" + command.getOperation());
	}

	/**
	 * Log a grid refresh
	 * 
	 * @throws IOException
	 */
	public void logRefresh() {
		writeStringToLog(REFRESH);
	}

	/**
	 * Log the state of the grid
	 * 
	 * @param model
	 * @throws IOException
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
						} else if (board[i][j].hasWall()) {
							// Wall
							grid += "w";
						} else if (board[i][j].numPlayers() > 0) {
							// Player
							grid += "P";
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
		return fileName;
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
