package bomberman.test;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import bomberman.common.Command;
import bomberman.common.Command.Operation;
import bomberman.log.ServerLogger;

/**
 * Simulates client/server interaction by executing test cases.
 * 
 * @author spbyron
 * 
 */
public class TestDriver {

	/**
	 * Main method which launches the interactive test driver
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Scanner console = new Scanner(System.in);
		
		System.out.println("Welcome to the Bomberman test driver! Select one of the following options:");
		System.out.println("0\tRun functional tests");
		System.out.println("1\tRun load tester");
		
		String selection = console.nextLine();
		if (selection.equals("0")) {
			runFunctionalTests();
		} else if (selection.equals("1")) {
			runLoadTesting();
		} else {
			System.out.println("ERROR: Invalid selection");
		}
		
		console.close();
	}
	
	private static void runLoadTesting() throws InterruptedException {
		/*
		 * Get the required parameters from the user
		 */
		Scanner console = new Scanner(System.in);

		System.out.println("Enter the number of clients for this test (integer greater than 0): ");
		String input = console.nextLine();

		int numberOfPlayers;
		try {
			numberOfPlayers = Integer.parseInt(input);
		} catch (Exception e) {
			// Catch any number formatting exceptions
			System.out.println("ERROR: Please enter a valid number of clients (greater than 0)");
			console.close();
			return;
		}
		
		// Make sure a non-zero integer was entered
		if (numberOfPlayers <= 0) {
			System.out.println("ERROR: Please enter a valid number of clients (greater than 0)");
			console.close();
			return;
		}
		
		boolean startServer;
		System.out.println("Start a server? (y to start a server locally/n if the server is running on a separate machine) : ");
		input = console.nextLine();
		
		if (input.toLowerCase().equals("y")) {
			startServer = true;
		} else if (input.toLowerCase().equals("n")) {
			startServer = false;
		} else {
			System.out.println("ERROR: Please enter y or n");
			console.close();
			return;
		}
		
		int port;
		System.out.println("Enter a port number to communicate over: ");
		input = console.nextLine();
		try {
			port = Integer.parseInt(input);
		} catch (Exception e) {
			// Catch any number formatting exceptions
			System.out.println("ERROR: Please enter a valid port");
			console.close();
			return;
		}
		
		// Make sure a non-zero port was entered
		if (port <= 0) {
			System.out.println("ERROR: Please enter a valid port");
			console.close();
			return;
		}
		
		System.out.println("Enter a host (IP address/localhost) the clients should connect to:");
		String host = console.nextLine();
		
		boolean autoStart;
		System.out.println("Auto start game? (y/n) : ");
		input = console.nextLine();
		
		if (input.toLowerCase().equals("y")) {
			autoStart = true;
		} else if (input.toLowerCase().equals("n")) {
			autoStart = false;
		} else {
			System.out.println("ERROR: Please enter y or n");
			console.close();
			return;
		}
		
		/*
		 * Start testing
		 */
		
		ServerThread server = null;
		if (startServer) {
			// Start up a server
			server = new ServerThread(port, false, true); // No test mode, with enemies
			server.start();
			System.out.println("Server started");
		}
		
		// ArrayList to keep track of the clients that have been spawned off
		ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
		
		// Create new clients
		for (int i = 0; i < numberOfPlayers; i++) {
			System.out.println("Adding new player: Player "+i );

			ClientThread clientThread = new ClientThread("Player"+i, host, port, true, false);
			clients.add(clientThread);

			clientThread.start();
			
			// Sleep for a time
			Thread.sleep(2000);
		}
		
		if (!autoStart) {
			System.out.println("When ready press enter to start testing");
			console.nextLine();
		}

		System.out.println("Executing command: START_GAME (Player1)");
		clients.get(0).getClient().processCommand(Command.Operation.START_GAME);
		Thread.sleep(500);
		
		//Run a series of random commands
		for(int j = 0; j<10; j++){
			for(int k = 0;k < numberOfPlayers;k++){
				System.out.println("Executing command: "+randomOperation()+" (Player"+k+")");
				ClientThread clientThread = clients.get(k);
				clientThread.getClient().processCommand(randomOperation());
				Thread.sleep(500);
				
			}
		}
		
		// Leave the game
		for(int l = 0; l < numberOfPlayers;l++){
			System.out.println("Executing command: "+Command.Operation.LEAVE_GAME+" (Player"+l+")");
			ClientThread clientThread = clients.get(l);
			clientThread.getClient().processCommand(Command.Operation.LEAVE_GAME);
			Thread.sleep(1000);
		}
		
		// Compute latencies, and shutdown all the clients
		shutdownClients(clients);
		
		// Shutdown the server
		shutdownServer(server);
		
		console.close();
		return;
	}
	
	private static void runFunctionalTests() {
		System.out.println("\nAvailable tests:");
		Scanner console = new Scanner(System.in);
		File testDirectory = new File("tests/");
		
		if (!testDirectory.exists()) {
			System.out.println("ERROR: Tests directory does not exist!");
			console.close();
			return;
		}
		
		if (!testDirectory.isDirectory()) {
			System.out.println("ERROR: Location is not a directory!");
			console.close();
			return;
		}
		
		File[] files = testDirectory.listFiles();
		
		for (int i = 0; i < files.length; i++) {
			System.out.println(i + "\t" + files[i].getName());
		}
		
		System.out.print("Type a test number and press enter to run it (all to run all tests, exit to quit): ");
		String input = console.nextLine();
		
		while (!input.equals("exit")) {
			if (input.equals("all")) {
				for (int i = 0; i < files.length; i++) {
					executeFunctionalTestCase(files[i]);
				}
			} else if (input.equals("exit")) {
				break;
			} else {
				int numEntered = -1;
				
				try {
					numEntered = Integer.parseInt(input);
				} catch (Exception e) {
					// Do nothing
				}
				
				if (numEntered < files.length && numEntered > -1) {
					executeFunctionalTestCase(files[numEntered]);
				} else {
					System.out.println("\nInvalid number entered. Please enter a number between 0 and " + (files.length - 1));
				}
				
				for (int i = 0; i < files.length; i++) {
					System.out.println(i + "\t" + files[i].getName());
				}
			}
			
			System.out.print("Type a test number and press enter to run it (all to run all tests, exit to quit): ");
			input = console.nextLine();
		}
		
		console.close();
		return;
	}

	/**
	 * Execute a test case
	 * 
	 * @param file
	 */
	private static void executeFunctionalTestCase(File file) {
		ArrayList<Command> commands = new ArrayList<Command>();
		
		System.out.println("Executing test case " + file.getName());
		
		// Start up a server
		ServerThread server = new ServerThread();
		server.start();

		// Hashmap to keep track of the clients that have been spawned off
		HashMap<String, ClientThread> clients = new HashMap<String, ClientThread>();

		// Open the file for reading
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			System.out.println("ERROR: File not found");
			return;
		}

		// Start processing the test case
		try {
			String line = reader.readLine();

			while (line != null) {
				String[] split = line.split(",");

				// Create a new client
				if (split[0].equals(ServerLogger.PLAYER)) {
					System.out.println("Adding new player: " + split[1]);

					ClientThread clientThread = new ClientThread(split[1]);
					clients.put(split[1], clientThread);

					clientThread.start();

					// Add the command into the array list to track what has
					// been done
					commands.add(new Command(split[1], Operation.JOIN_GAME));

					// Sleep for a time
					Thread.sleep(2000);
				}

				// Process the command
				if (split[0].equals(ServerLogger.COMMAND)) {
					System.out.println("Executing command: " + split[2] + " (" + split[1] + ")");

					// Add the command into the array list to track what has
					// been done
					Command.Operation operation = Command.Operation.valueOf(split[2].toUpperCase());
					commands.add(new Command(split[1], operation));

					// Send the command to the correct client
					ClientThread clientThread = clients.get(split[1]);
					clientThread.getClient().processCommand(Command.Operation.valueOf((split[2]).toUpperCase()));

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
		shutdownClients(clients.values());

		String logFile = server.getLogFile();
				
		// Shutdown the server
		shutdownServer(server);

		// Verify the commands were received by the server
		if (!verifyServerLog(logFile, commands)) {
			System.out.println("Server log file does not match the list of commands " + "sent by the test case.\n" + "Test case DID NOT execute successfully.\n");
		} else {
			System.out.println("Test case completed successfully\n");
		}
	}

	/**
	 * Compares the list of commands executed by the TestDriver to the log file
	 * generated by the server. If the two match, the test case completed
	 * successfully
	 * @param commands 
	 * 
	 * @return
	 */
	private static boolean verifyServerLog(String logFile, ArrayList<Command> commands) {
		BufferedReader reader = null;
		boolean ret = true;

		ArrayList<Command> commandsFromLog = new ArrayList<Command>();
		String[][] prevBoardState = null;

		try {
			reader = new BufferedReader(new FileReader(new File(logFile)));
			String line = reader.readLine();

			while (line != null) {
				String[] split = line.split(",");

				if (split[0].equals(ServerLogger.COMMAND)) {
					Command command = parseCommand(split);
					commandsFromLog.add(command);

					if (commands.contains(command)) {
						commands.remove(command);
					} else {
						System.out.println("ERROR: Command not received by the server: " + command);
						ret = false;
					}
				} else if (split[0].equals(ServerLogger.BOARD_STATE)) {
					String[][] state = new String[12][12];
					state[0] = split;

					for (int i = 1; i < 12; i++) {
						line = reader.readLine();
						state[i] = line.split(",");
					}

					state = parseBoardState(state);

					if (prevBoardState == null) {
						prevBoardState = state;
					} else {
						boolean check = checkMoveCommands(commandsFromLog, prevBoardState, state);
						if (!check) {
							System.out.println("ERROR: The following commands were not interpreted properly: " + commandsFromLog);
							ret = false;
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
	 * Simulates movement on the board, and ensures the player was moved to the
	 * correct location by the server
	 * 
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
		for (int i = 1; i <= numPlayers; i++) {
			// Get the commands for this player
			ArrayList<Command> playerCommands = getPlayerCommands(commands, i);

			if (playerCommands.size() > 0) {
				// Get the players initial position
				Point playerPosition = getPlayerPosition(i, prevState);

				// If the player was found on the board
				if (playerPosition != null) {
					// For each of the players command
					for (int j = 0; j < playerCommands.size(); j++) {
						Command command = playerCommands.get(j);
						Operation operation = command.getOperation();

						// If the operation is a move
						if (operation.isMove()) {
							if (operation.equals(Operation.MOVE_DOWN)) {
								if (!isWallAt(playerPosition.x + 1, playerPosition.y, prevState)) {
									playerPosition.x = playerPosition.x + 1;
								}
							} else if (operation.equals(Operation.MOVE_UP)) {
								if (!isWallAt(playerPosition.x - 1, playerPosition.y, prevState)) {
									playerPosition.x = playerPosition.x - 1;
								}
							} else if (operation.equals(Operation.MOVE_LEFT)) {
								if (!isWallAt(playerPosition.x, playerPosition.y - 1, prevState)) {
									playerPosition.y = playerPosition.y - 1;
								}
							} else if (operation.equals(Operation.MOVE_RIGHT)) {
								if (!isWallAt(playerPosition.x, playerPosition.y + 1, prevState)) {
									playerPosition.y = playerPosition.y + 1;
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
						System.out.println("ERROR: Player not in the expected position");
						ret = false;
					}
				}
			}
		}

		return ret;
	}

	/**
	 * Parses a string array from a log into a Command object
	 * 
	 * @param split
	 * @return
	 */
	private static Command parseCommand(String[] split) {
		if (split[0].equals(ServerLogger.COMMAND)) {
			int id = Integer.parseInt(split[3].split("=")[1]);
			String player = split[1].split("=")[1];
			Command.Operation operation = Command.Operation.valueOf(split[2].split("=")[1]);
			return new Command(player, operation, id);
		}
		return null;
	}

	/**
	 * Parses an array of board state log lines into an array representing the
	 * board state
	 * 
	 * @param boardState
	 * @return
	 */
	private static String[][] parseBoardState(String[][] boardState) {
		String[][] board = new String[12][12];

		for (int i = 0; i < 12; i++) {
			String line = boardState[i][2];

			for (int j = 0; j < 12; j++) {
				board[i][j] = String.valueOf(line.charAt(j));
			}
		}

		return board;
	}

	/**
	 * Checks board to see if there is a wall at x,y
	 * 
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
	 * 
	 * @param id
	 * @param board
	 * @return
	 */
	private static Point getPlayerPosition(int id, String[][] board) {
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 12; j++) {
				if (board[i][j].equals(String.valueOf(id))) {
					return new Point(i, j);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the Commands in commands belonging to player id
	 * 
	 * @param commands
	 * @param id
	 * @return
	 */
	private static ArrayList<Command> getPlayerCommands(ArrayList<Command> commands, int id) {
		ArrayList<Command> playerCommands = new ArrayList<Command>();

		for (int i = 0; i < commands.size(); i++) {
			if (commands.get(i).getIdentifier() == id) {
				playerCommands.add(commands.get(i));
			}
		}

		return playerCommands;
	}

	/**
	 * Counts the number of unique players that there are Commands in the list
	 * for
	 * 
	 * @param commands
	 * @return
	 */
	private static int countNumPlayers(ArrayList<Command> commands) {
		// Find the highest player number there is in the list
		// This represents the number of players in the game
		int numPlayers = 0;

		for (int i = 0; i < commands.size(); i++) {
			int id = commands.get(i).getIdentifier();
			if (id > numPlayers) {
				numPlayers = id;
			}
		}

		return numPlayers;
	}
	
	/**
	 * Creates a random movement Operation.
	 * @return
	 */
	private static Operation randomOperation(){
		Random rand = new Random();
		Operation o = null;
		int value = rand.nextInt(4);
		switch (value) {
		case 0:
			o = Operation.MOVE_DOWN;
			break;
		case 1:
			o = Operation.MOVE_LEFT;
			break;
		case 2:
			o = Operation.MOVE_RIGHT;
			break;
		case 3:
			o = Operation.MOVE_UP;
			break;
		}
		
		return o;
	}
	
	/**
	 * Shut down all the clients in the list
	 * @param clients
	 */
	private static void shutdownClients(Collection<ClientThread> clients) {
		for (ClientThread client : clients) {
			
			// Compute the latencies
			System.out.println("Client " + client + " latencies: ");
			File log = new File(client.getLogFile());
			ClientLatencyAnalyser.computeLatencies(log);
			
			// Shutdown the client
			client.shutdown();

			// Wait for the client to shutdown
			try {
				client.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Shuts down the specified server
	 * @param server
	 */
	private static void shutdownServer(ServerThread server) {
		server.shutdown();

		// Wait for the server to shutdown before continuing
		try {
			server.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
