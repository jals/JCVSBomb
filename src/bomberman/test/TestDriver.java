package bomberman.test;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import bomberman.common.Command;
import bomberman.common.Command.Operation;
import bomberman.server.Logger;

/**
 * Simulates client/server interaction by executing test cases.
 * 
 * @author spbyron
 *
 */
public class TestDriver {
	
	// Commands executed by the TestDriver are stored in an array list, to be
	// compared with the list of commands recieved by the server later
	private static ArrayList<Command> commands = new ArrayList<Command>();
	
	/**
	 * Usage: TestDriver {test_directory} (optional)
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String location = new String();
		
		if (args.length < 1) {
			location = "tests/";
		} else {
			location = args[0];
		}
		
		File testDirectory = new File(location);
		
		if (!testDirectory.exists()) {
			System.out.println("ERROR: Directory specified does not exist");
			return;
		}
		
		if (!testDirectory.isDirectory()) {
			System.out.println("ERROR: Location specified is not a directory");
			return;
		}
		
		File[] files = testDirectory.listFiles();
		
		for (int i=0; i<files.length; i++) {
			System.out.println(i + "\t" + files[i].getName());
		}
		
		System.out.print("Type a test number and press enter to run it (all to run all tests, exit to quit): ");
		Scanner console = new Scanner(System.in);
		String input = console.nextLine();
		
		while (!input.equals("exit")) {
			
			if (input.equals("all")) {
				runAll(files);
			} else {
				int numEntered = Integer.parseInt(input);
				if (numEntered < files.length) {
					executeTestCase(files[numEntered]);
				} else {
					System.out.println("Invalid number entered. Please enter a number between 0 and " + (files.length-1));
				}
				
				for (int i=0; i<files.length; i++) {
					System.out.println(i + "\t" + files[i].getName());
				}
			}
			
			System.out.print("Type a test number and press enter to run it (all to run all tests, exit to quit): ");
			input = console.nextLine();
		}
		
		console.close();
		
	}
	
	/**
	 * Executes each of the files in the array
	 * @param files
	 */
	private static void runAll(File[] files) {
		for (int i=0; i<files.length; i++) {
			executeTestCase(files[i]);
		}
	}
	
	/**
	 * Execute a test case
	 * @param file
	 */
	private static void executeTestCase(File file) {
		String logFile = runTest(file);
		
		if (!verifyServerLog(logFile)) {
			System.out.println("Server log file does not match the list of commands "
					+ "sent by the test case.\n"
					+ "Test case DID NOT execute successfully.\n");
		} else {
			System.out.println("Test case completed successfully\n");
		}
	}
	
	
	/**
	 * Runs the given test case
	 * @param testFile
	 */
	private static String runTest(File testFile) {
		// Start up a server
		ServerThread server = new ServerThread();
		server.start();

		// Hashmap to keep track of the clients that have been spawned off
		HashMap<String, ClientThread> clients = new HashMap<String, ClientThread>();
		
		// Open the file for reading
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(testFile));
		} catch (FileNotFoundException e1) {
			System.out.println("ERROR: File not found");
			return server.getLogFile();
		}

		// Start processing the test case
		try {
			String line = reader.readLine();
			
			while (line != null) {
				String[] split = line.split(",");
			
				// Create a new client
				if (split[0].equals(Logger.PLAYER)) {
					System.out.println("Adding new player: " + split[1]);
					
					ClientThread clientThread = new ClientThread(split[1]);
					clients.put(split[1], clientThread);
					
					clientThread.start();
					
					// Add the command into the array list to track what has been done
					commands.add(new Command(split[1], Operation.JOIN_GAME));
					
					// Sleep for a time
					Thread.sleep(500);
				}
				
				// Process the command
				if (split[0].equals(Logger.COMMAND)) {
					System.out.println("Executing command: " + split[2] + " (" + split[1] + ")");
					
					// Add the command into the array list to track what has been done
					Command.Operation operation = Command.Operation.valueOf(split[2].toUpperCase());
					commands.add(new Command(split[1], operation));
					
					// Send the command to the correct client
					ClientThread clientThread = clients.get(split[1]);
					clientThread.getClient().processCommand(split[2]);
					
					// Sleep for a time
					Thread.sleep(500);
				}
				
				line = reader.readLine();
			}

		} catch (IOException e) {
			System.out.println("ERROR: Error reading file");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Shutdown all the clients
		Set<String> players = clients.keySet();
		for (String s : players) {
			clients.get(s).shutdown();
			
			// Wait for the client to shutdown
			try {
				clients.get(s).join(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		String log = server.getLogFile();
		server.shutdown();
		
		// Wait for the server to shutdown before continuing
		try {
			server.join(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return log;
	}
	
	/**
	 * Compares the list of commands executed by the TestDriver to the log file 
	 * generated by the server.
	 * If the two match, the test case completed successfully
	 * @return
	 */
	private static boolean verifyServerLog(String logFile) {
		BufferedReader reader = null;
		boolean ret = true;
		
		ArrayList<Command> commandsFromLog = new ArrayList<Command>();
		String[][] prevBoardState = null;
		
		try {
			reader = new BufferedReader(new FileReader(new File(logFile)));
			String line = reader.readLine();
			
			while (line!=null) {
				String[] split = line.split(",");
				
				if (split[0].equals(Logger.COMMAND)) {
					Command command = parseCommand(split);
					commandsFromLog.add(command);
					
					if (commands.contains(command)) {
						commands.remove(command);
					} else {
						return false;
					}
				} else if (split[0].equals(Logger.BOARD_STATE)) {
					String[][] state = new String[12][12];
					state[0] = split;
					
					for (int i=1; i<12; i++) {
						line = reader.readLine();
						state[i] = line.split(",");
					}
					
					state = parseBoardState(state);
					
					if (prevBoardState == null) {
						prevBoardState = state;
					} else {
						boolean check = checkMoveCommands(commandsFromLog, prevBoardState, state);
						if (!check) {
							return false;
						}
						prevBoardState = state;
						commandsFromLog = new ArrayList<Command>();
					}
				}
				
				line = reader.readLine();
			}
			
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ret = false;
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
		
	}
	
	/**
	 * Simulates movement on the board, and ensures the player was moved to the correct location by the server
	 * @param commands
	 * @param prevState
	 * @param currState
	 * @return
	 */
	private static boolean checkMoveCommands(ArrayList<Command> commands, String[][] prevState, String[][] currState) {
		// Get the total number of players
		int numPlayers = countNumPlayers(commands);
		boolean ret = true;
		
		// For each player
		for (int i=1; i<= numPlayers; i++) {
			// Get the commands for this player
			ArrayList<Command> playerCommands = getPlayerCommands(commands, i);
			
			if (playerCommands.size() > 0) {
				// Get the players initial position
				Point playerPosition = getPlayerPosition(i, prevState);
				
				// If the player was found on the board
				if (playerPosition != null) {
					// For each of the players command
					for (int j=0; j<playerCommands.size(); j++) {
						Command command = playerCommands.get(j);
						Operation operation = command.getOperation();
						
						// If the operation is a move
						if (operation.isMove()) {
							if (operation.equals(Operation.MOVE_DOWN)) {
								if (!isWallAt(playerPosition.x+1, playerPosition.y, prevState)) {
									playerPosition.x = playerPosition.x+1;
								}
							} else if (operation.equals(Operation.MOVE_UP)) {
								if (!isWallAt(playerPosition.x-1, playerPosition.y, prevState)) {
									playerPosition.x = playerPosition.x-1;
								}
							} else if (operation.equals(Operation.MOVE_LEFT)) {
								if (!isWallAt(playerPosition.x, playerPosition.y-1, prevState)) {
									playerPosition.y = playerPosition.y-1;
								}
							} else if (operation.equals(Operation.MOVE_RIGHT)) {
								if (!isWallAt(playerPosition.x, playerPosition.y+1, prevState)) {
									playerPosition.y = playerPosition.y+1;
								}
							}
						}
					}
				}
				
				// Get the players final position from the current board state
				Point finalPosition = getPlayerPosition(i, currState);
				
				// If the player was found on the board
				if (finalPosition != null) {
					// Make sure the two positions are the same
					if (!finalPosition.equals(playerPosition)) {
						ret = false;
					}
				}
			}
		}
		
		return ret;
	}
		
	/**
	 * Parses a string array from a log into a Command object
	 * @param split
	 * @return
	 */
	private static Command parseCommand(String[] split) {
		if (split[0].equals(Logger.COMMAND)) {
			int id = Integer.parseInt(split[3].split("=")[1]);
			String player = split[1].split("=")[1];
			Command.Operation operation = Command.Operation.valueOf(split[2].split("=")[1]);
			return new Command(player, operation, id);
		}
		return null;
	}
	
	/**
	 * Parses an array of board state log lines into an array representing the board state
	 * 
	 * @param boardState
	 * @return
	 */
	private static String[][] parseBoardState(String[][] boardState) {
		String[][] board = new String[12][12];
		
		for (int i=0; i<12; i++) {
			String line = boardState[i][2];
			
			for (int j=0; j<12; j++) {
				board[i][j] = String.valueOf(line.charAt(j));
			}
		}
		
		return board;
	}
	
	/**
	 * Checks board to see if there is a wall at x,y
	 * @param x
	 * @param y
	 * @param board
	 * @return
	 */
	private static boolean isWallAt(int x, int y, String[][] board) {
		if (board[x][y].equals("-")) {
			return true;
		}
		
		if (board[x][y].equals("|")) {
			return true;
		}
		
		if (board[x][y].equals("w")) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns a Point representing where the given player is on the board
	 * @param id
	 * @param board
	 * @return
	 */
	private static Point getPlayerPosition(int id, String[][] board) {
		for (int i=0; i<12; i++) {
			for (int j=0; j<12; j++) {
				if (board[i][j].equals(String.valueOf(id))) {
					return new Point(i, j);
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the Commands in commands belonging to player id
	 * @param commands
	 * @param id
	 * @return
	 */
	private static ArrayList<Command> getPlayerCommands(ArrayList<Command> commands, int id) {
		ArrayList<Command> playerCommands = new ArrayList<Command>();
		
		for (int i=0; i<commands.size(); i++) {
			if (commands.get(i).getIdentifier() == id) {
				playerCommands.add(commands.get(i));
			}
		}
		
		return playerCommands;
	}
	
	/**
	 * Counts the number of unique players that there are Commands in the list for
	 * @param commands
	 * @return
	 */
	private static int countNumPlayers(ArrayList<Command> commands) {
		// Find the highest player number there is in the list
		// This represents the number of players in the game
		int numPlayers = 0;
		
		for (int i=0; i<commands.size(); i++) {
			int id = commands.get(i).getIdentifier();
			if (id > numPlayers) {
				numPlayers = id;
			}
		}
		
		return numPlayers;
	}

}
