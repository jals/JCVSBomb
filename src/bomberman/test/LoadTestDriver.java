package bomberman.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import bomberman.common.Command;
import bomberman.common.Command.Operation;


/**
 * This class will run a test suite of load tests to see how much the system can handle.
 * This was based on the TestDriver class...
 * 
 * @author spbyron, csiewecke
 * 
 */
public class LoadTestDriver {

	// Commands executed by the TestDriver are stored in an array list, to be
	// compared with the list of commands received by the server later
	private static ArrayList<Command> commands = new ArrayList<Command>();
	//private int numberOfPlayers;

	/**
	 * Usage: TestDriver {test_directory} (optional)
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		System.out.print("Please input the desired number of players for this test (possible values range from 1 to 10, type 'exit' to quit): ");
		Scanner console = new Scanner(System.in);
		String input = console.nextLine();

		while (!input.equals("exit")) {

			int numEntered = -1;

			try {
				numEntered = Integer.parseInt(input);
			} catch (Exception e) {
				// Do nothing
			}

			if (numEntered < 11 && numEntered > 0) {
				executeTestCase(numEntered);
			} else {
				System.out.println("\nInvalid number entered. Please enter a number between 1 and 10");
			}

			System.out.print("Please input the desired number of players for this test (possible values range from 1 to 10, type 'exit' to quit): ");
			input = console.nextLine();
		}

		console.close();

	}

	/**
	 * Execute a test case
	 * 
	 * @param file
	 */
	private static void executeTestCase(int numberOfPlayers) {
		System.out.println("Executing test case with "+numberOfPlayers+" players.");
		runTest(numberOfPlayers);

	}

	/**
	 * Runs the given test case
	 * 
	 * @param testFile
	 */
	private static String runTest(int numberOfPlayers) {
		// Start up a server
		ServerThread server = new ServerThread(false);
		server.start();

		// Hashmap to keep track of the clients that have been spawned off
		HashMap<String, ClientThread> clients = new HashMap<String, ClientThread>();

		// Start the test case
		try {

			
			// Create new clients
			for (int i = 1; i < numberOfPlayers+1; i++) {
				System.out.println("Adding new player: Player "+i );

				ClientThread clientThread = new ClientThread("Player"+i);
				clients.put("Player"+i, clientThread);

				clientThread.start();

				// Add the command into the array list to track what has
				// been done
				commands.add(new Command("Player"+i, Operation.JOIN_GAME));
				
				// Sleep for a time
				Thread.sleep(2000);
			}
			
			commands.add(new Command("Player1", Operation.START_GAME));// once all Players have been added, start the game.
			System.out.println("Executing command: START_GAME (Player1)");
			clients.get("Player1").getClient().processCommand(Command.Operation.START_GAME);
			Thread.sleep(500);
			
			for(int j = 0; j<10; j++){
				for(int k = 1;k < numberOfPlayers+1;k++){
					commands.add(new Command("Player"+k, randomOperation()));
					
					System.out.println("Executing command: "+randomOperation()+" (Player"+k+")");
					ClientThread clientThread = clients.get("Player"+k);
					clientThread.getClient().processCommand(randomOperation());
					Thread.sleep(500);
					
				}
			}
			//everybody leaves the game? may not work since some could be gone already...
			for(int l = 1; l < numberOfPlayers+1;l++){
				commands.add(new Command("Player"+l, Command.Operation.LEAVE_GAME));
				System.out.println("Executing command: "+Command.Operation.LEAVE_GAME+" (Player"+l+")");
				ClientThread clientThread = clients.get("Player"+l);
				clientThread.getClient().processCommand(Command.Operation.LEAVE_GAME);
				Thread.sleep(1000);
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Shutdown all the clients
		Set<String> players = clients.keySet();
		for (String s : players) {
			
			// Compute the latencies
			System.out.println("Client " + s + " latencies: ");
			File log = new File(clients.get(s).getLogFile());
			ClientLatencyAnalyser.computeLatencies(log);
			
			// Shutdown the client
			clients.get(s).shutdown();

			// Wait for the client to shutdown
			try {
				clients.get(s).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		String log = server.getLogFile();
		server.shutdown();

		// Wait for the server to shutdown before continuing
		try {
			server.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return log;
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
}
