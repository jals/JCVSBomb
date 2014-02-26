package bomberman.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import bomberman.Command;
import bomberman.Command.Operation;

public class TestDriver {

	private static void runTest(String filename, String player) {
		BufferedReader reader = null;

		// Open up the text file
		try {
			reader = new BufferedReader(new FileReader(new File(filename)));
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Test case file not found");
			return;
		}

		try {
			String line = reader.readLine();
			while (line != null) {
				Operation operation;
				try {
					operation = Operation.valueOf(line.toUpperCase());
				} catch (IllegalArgumentException e) {
					System.out.println("ERROR: Unrecognized command");
					return;
				}
				
				
				// Create a new command with the operation
				Command command = new Command(player, operation);
				
				// Do something with the command
				System.out.println(command.getPlayer() + " " + command.getOperation()); 
			
				// Read the next line
				line = reader.readLine();
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	/**
//	 * Returns the Operation corresponding to the input string. 
//	 * Returns null if the string is not recognized
//	 * @param string
//	 * @return
//	 */
//	private static Operation parseString(String string) {
//		switch (string.toUpperCase()) {
//			case "MOVE_LEFT":
//				return Operation.MOVE_LEFT;
//			case "MOVE_RIGHT":
//				return Operation.MOVE_RIGHT;
//			case "MOVE_UP":
//				return Operation.MOVE_UP;
//			case "MOVE_DOWN":
//				return Operation.MOVE_DOWN;
//			case "JOIN_GAME":
//				return Operation.JOIN_GAME;
//			case "LEAVE_GAME":
//				return Operation.LEAVE_GAME;
//			case "DROP_BOMB":
//				return Operation.DROP_BOMB;
//			default :
//				return null;
//		}
//	}

	/**
	 * Usage: TestDriver {file name} {player name}
	 * @param args
	 */
	public static void main(String[] args) {
		runTest(args[0], args[1]);
	}

}
