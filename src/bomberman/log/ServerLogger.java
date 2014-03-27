package bomberman.log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import bomberman.common.Command;
import bomberman.common.model.Model;
import bomberman.common.model.Player;
import bomberman.common.model.Square;

/**
 * Thread for logging Commands as they are processed by the server Also logs any
 * errors (e.g. trying to move into a wall), and logs the game state everytime
 * the server sends out a refresh of the board
 * 
 * @author spbyron
 * 
 */

public class ServerLogger extends Logger {

	/**
	 * Create a new logger object with the date and time as a filename
	 */
	public ServerLogger() {
		super("logs/bomberman-server-" + getDate() + ".log");
	}
	
	/**
	 * Formats and gets the current date, for the log file name
	 * 
	 * @return
	 */
	private static String getDate() {
		SimpleDateFormat formatter = new SimpleDateFormat();
		formatter.applyPattern("yyyy-MM-dd-HH-mm-ss");
		return formatter.format(new Date());
	}

	/**
	 * Log a command
	 * 
	 * @param command
	 */
	public void logCommand(Command command, int player) {
		writeStringToLog(COMMAND + "," + PLAYER + "=" + command.getPlayer() + "," + OPERATION + "=" + command.getOperation() + "," + ID + "=" + player);
	}

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
							if (i == 0 || i == Model.BOARD_SIZE - 1) {
								grid += "-";
							} else {
								grid += "|";
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
					if (i > 9) {
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
			// Do nothing
		}
	}

	/**
	 * Log an error
	 * 
	 * @param command
	 * @param error
	 */
	public void logError(Command command, String error) {
		writeStringToLog(ERROR + "," + PLAYER + "=" + command.getPlayer() + "," + OPERATION + "=" + command.getOperation() + "," + error);
	}
	
}
