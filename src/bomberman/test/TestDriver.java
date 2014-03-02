package bomberman.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import bomberman.Client;
import bomberman.Command;
import bomberman.Command.Operation;

public class TestDriver {

	private static String PLAYER = "PLAYER";
	private static String COMMAND = "COMMAND";
	private static ArrayList<Command> commands = new ArrayList<Command>();
	private static ServerThread server;

	/**
	 * Runs the given test case
	 * @param filename
	 */
	private static void runTest(String filename) {
		// Start up a server
		server = new ServerThread();
		server.start();

		// Hashmap to keep track of the clients that have been spawned off
		HashMap<String, Client> clients = new HashMap<String, Client>();
		
		// Open the file for reading
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(new File(filename)));
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
				if (split[0].equals(PLAYER)) {
					System.out.println("Adding new player: " + split[1]);
					
					ClientThread clientThread = new ClientThread(split[1]);
					
					Client client = clientThread.getClient();
					clients.put(split[1], client);
					
					clientThread.start();
					
					// Add the command into the array list to track what has been done
					commands.add(new Command(split[1], Operation.JOIN_GAME));
				}
				
				// Process the command
				if (split[0].equals(COMMAND)) {
					System.out.println("Executing command: " + split[2] + " (" + split[1] + ")");
					
					// Add the command into the array list to track what has been done
					Command.Operation operation = Command.Operation.valueOf(split[2].toUpperCase());
					commands.add(new Command(split[1], operation));
					
					// Send the command to the correct client
					Client client = clients.get(split[1]);
					client.move(split[2]);
					
					// Sleep for a time
					Thread.sleep(1000);
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
	}
	
	/**
	 * Compares the list of executed commands to the log file generated by the server.
	 * If the two match, the test case completed successfully
	 * @return
	 */
	private static boolean verifyServerLog() {
		BufferedReader reader = null;
		boolean ret = true;
		
		try {
			reader = new BufferedReader(new FileReader(new File(server.getLogFile())));
			String line = reader.readLine();
			int i=0;
			
			while (line!=null) {
				String[] split = line.split(",");
				
				if (split[0].equals(COMMAND)) {
					String player = split[1].split("=")[1];
					Command.Operation operation = Command.Operation.valueOf(split[2].split("=")[1]);
					
					Command command = commands.get(i);

					if (!command.getPlayer().equals(player)) {
						return false;
					}
					
					if (!command.getOperation().equals(operation)) {
						return false;
					}
					
					i++;
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
	 * Usage: TestDriver {file name}
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("Error: Please specify a path to a test file to be run");
			return;
		}
		
		runTest(args[0]);
		
		if (!verifyServerLog()) {
			System.out.println("Server log file does not match the list of commands "
					+ "sent by the test case.\n"
					+ "Test case DID NOT execute successfully.");
		} else {
			System.out.println("Test case completed successfully");
		}
	}

}
