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
	 * Create a new logger object
	 * Write the log to the given file
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
	
	public Logger() {
		this("logs/bomberman-" + getDate() + ".log");
	}
	
	public void run() {
		while(run) {
		}
		
		try {
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Log a command
	 * @param command
	 * @throws IOException
	 */
	public void logCommand(Command command) {
		writeStringToLog(COMMAND + "," + PLAYER + "=" + command.getPlayer() + "," + OPERATION + "=" + command.getOperation());
	}
	
	/**
	 * Log a grid refresh
	 * @throws IOException
	 */
	public void logRefresh() {
		writeStringToLog(REFRESH);
	}
	
	/**
	 * Log the state of the grid
	 * @param model
	 * @throws IOException
	 */
	public void logGrid(Model model) {
		try {
			String grid = new String();
			Square[][] board = model.getBoard();
			
			synchronized (log) {
				for (int i=0; i<Model.BOARD_SIZE; i++) {
					for (int j=0; j<Model.BOARD_SIZE; j++) {
						if (board[i][j] == null) {
							grid+=0;
						} else {
							grid += board[i][j];
						}
					}
					log.write(BOARD_STATE + ",Row " + i + "," + grid);
					log.newLine();
					grid = new String();
				}
				log.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void logError(Command command, String error) {
		writeStringToLog(ERROR + "," + PLAYER + "=" + command.getPlayer() + "," + OPERATION + "=" + command.getOperation() + "," + error);
	}
	
	/**
	 * Close the log file
	 * @throws IOException
	 */
	private void close() throws IOException {
		log.close();
	}
	
	private static String getDate() {
		SimpleDateFormat formatter = new SimpleDateFormat();
		formatter.applyPattern("yyyy-MM-dd-HH-mm");
		return formatter.format(new Date());
	}
	
	public String getLogFile() {
		return fileName;
	}
	
	public void shutdown() {
		run = false;
	}
	
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
