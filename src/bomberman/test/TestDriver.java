package bomberman.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import bomberman.Client;
import bomberman.Server;

public class TestDriver {

	private static void runTest(String filename, String player)
			throws Exception {
		BufferedReader reader = null;

		// Open up the text file
		try {
			reader = new BufferedReader(new FileReader(new File(filename)));

			Client c = new Client(player);
			String line = reader.readLine();
			while (line != null) {
				c.move(line);
				Thread.sleep(1000);
				// Read the next line
				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Test case file not found");
			return;
		} finally {
			reader.close();

		}

	}

	// /**
	// * Returns the Operation corresponding to the input string.
	// * Returns null if the string is not recognized
	// * @param string
	// * @return
	// */
	// private static Operation parseString(String string) {
	// switch (string.toUpperCase()) {
	// case "MOVE_LEFT":
	// return Operation.MOVE_LEFT;
	// case "MOVE_RIGHT":
	// return Operation.MOVE_RIGHT;
	// case "MOVE_UP":
	// return Operation.MOVE_UP;
	// case "MOVE_DOWN":
	// return Operation.MOVE_DOWN;
	// case "JOIN_GAME":
	// return Operation.JOIN_GAME;
	// case "LEAVE_GAME":
	// return Operation.LEAVE_GAME;
	// case "DROP_BOMB":
	// return Operation.DROP_BOMB;
	// default :
	// return null;
	// }
	// }

	/**
	 * Usage: TestDriver {file name} {player name}
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		new Thread() {
			public void run() {
				try {
					Server.main(null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		runTest(args[0], args[1]);
	}

}
