package bomberman.test.unit;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import bomberman.client.Client;
import bomberman.common.Command.Operation;
import bomberman.common.model.Player;
import bomberman.server.Server;
import bomberman.test.ClientThread;
import bomberman.test.ServerThread;

public class TwoPlayersTouchUnitTest {

	static ServerThread serverThread;
	static ClientThread clientThread1;
	static ClientThread clientThread2;
	static Client client1;
	static Client client2;
	static Server server;

	private static String PLAYER1_NAME = "test1";
	private static String PLAYER2_NAME = "test2";

	@BeforeClass
	public static void setUp() throws Exception {
		Random rand = new Random();
		int port = rand.nextInt(500) + 9500;

		serverThread = new ServerThread(port, false);
		clientThread1 = new ClientThread(PLAYER1_NAME, port, false);
		clientThread2 = new ClientThread(PLAYER2_NAME, port, false);

		serverThread.start();
		clientThread1.start();
		clientThread2.start();

		client1 = clientThread1.getClient();
		client2 = clientThread2.getClient();
		server = serverThread.getServer();

		// Give the threads some time to start
		sleep(500);

		assertTrue(server.isRunning());
		System.out.println("Server is running");
		assertTrue(client1.isRunning());
		assertTrue(client2.isRunning());
		System.out.println("Client is running");

		// Join the game
		Player player1 = server.getPlayer(PLAYER1_NAME);
		Player player2 = server.getPlayer(PLAYER2_NAME);
		
		assertTrue(player1 != null);
		assertTrue(player1.getName().equals(PLAYER1_NAME));
		assertTrue(player2 != null);
		assertTrue(player2.getName().equals(PLAYER2_NAME));
		System.out.println("Client joined Game");

		// Start the game
		client1.processCommand(Operation.START_GAME);
		
		sleep(200);
		assertTrue(server.isGameStarted());
		System.out.println("Game started");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		System.out.println("Shutting down server");
		serverThread.shutdown();
		System.out.println("Shutting down client");
		clientThread1.shutdown();
		clientThread2.shutdown();
		serverThread.join();
		clientThread1.join();
		clientThread2.join();
		System.out.println("Shut down successfully");
	}

	@Test
	public void testMove() {
		// Move player 1 down 9 times and player 2 left 9 times
		for (int i=0; i<9; i++) {
			client2.processCommand(Operation.MOVE_LEFT);
			sleep(200);
			client1.processCommand(Operation.MOVE_DOWN);
			sleep(200);
			client2.processCommand(Operation.MOVE_DOWN);
			sleep(200);
			client1.processCommand(Operation.MOVE_LEFT);
			sleep(200);
		}
		
		sleep(500);
		
		Player player1 = server.getPlayer(PLAYER1_NAME);
		Player player2 = server.getPlayer(PLAYER2_NAME);
		
		assertTrue(player1 == null || player2 == null);	//Ensure one of the players has died
	}

	private static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
