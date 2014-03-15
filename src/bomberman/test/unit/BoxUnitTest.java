package bomberman.test.unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import bomberman.client.Client;
import bomberman.common.Command.Operation;
import bomberman.common.model.Player;
import bomberman.common.model.Square;
import bomberman.server.BombFactory;
import bomberman.server.Server;
import bomberman.test.ClientThread;
import bomberman.test.ServerThread;

public class BoxUnitTest {
	
	static ServerThread serverThread;
	static ClientThread clientThread;
	static Client client;
	static Server server;
	
	private static String PLAYER_NAME = "test";

	@BeforeClass
	public static void setUp() throws Exception {
		Random rand = new Random();
		int  port = rand.nextInt(500) + 9500;
		
		serverThread = new ServerThread(port, false);
		clientThread = new ClientThread(PLAYER_NAME, port, false);
		
		serverThread.start();
		clientThread.start();
		
		client = clientThread.getClient();
		server = serverThread.getServer();
		
		// Give the threads some time to start
		sleep(500);
		
		assertTrue(server.isRunning());
		System.out.println("Server is running");
		assertTrue(client.isRunning());
		System.out.println("Client is running");
		
		// Join the game
		client.processCommand(Operation.JOIN_GAME);
		sleep(200);
		Player player = server.getPlayer(PLAYER_NAME);
		assertTrue(player != null);
		assertTrue(player.getName().equals(PLAYER_NAME));
		System.out.println("Client joined Game");
		
		// Start the game
		client.processCommand(Operation.START_GAME);
		sleep(200);
		assertTrue(server.isGameStarted());
		System.out.println("Game started");
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		System.out.println("Shutting down server");
		serverThread.shutdown();
		System.out.println("Shutting down client");
		clientThread.shutdown();
		serverThread.join();
		clientThread.join();
		System.out.println("Shut down successfully");
	}
	
	@Test
	public void testExplodeBoxGetHealth() {
		Player player = server.getPlayer(PLAYER_NAME);
		int startHealth = player.getHealth();
		
		BombFactory factory = server.getBombFactory();
		Square square = server.getSquare(3, 3);
		assertTrue(square.hasBox());
		
		System.out.println("Moving to the box");
		
		// Move down 2 times
		client.processCommand(Operation.MOVE_DOWN);
		sleep(200);
		client.processCommand(Operation.MOVE_DOWN);
		sleep(200);
		
		// Move right 1
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		
		System.out.println("Blowing up the box");
		
		// Drop a bomb
		client.processCommand(Operation.DROP_BOMB);
		sleep(200);
		
		Point bombLocation = server.getPlayerLocation(PLAYER_NAME);
		
		// Make sure the bomb is there
		assertTrue(factory.isBombAt(bombLocation));
		
		// Move out of the way
		client.processCommand(Operation.MOVE_LEFT);
		sleep(200);
		client.processCommand(Operation.MOVE_UP);
		sleep(200);
		
		// Wait for the bomb to explode
		System.out.println("Waiting for bomb to explode...");
		sleep(6200); // Bomb has max fuse of 6 seconds
		System.out.println("Done waiting.");
		
		// Box should have blown up leaving a power up
		assertFalse(square.hasBox());
		assertTrue(square.hasPowerUp());
		
		System.out.println("Moving to get the power up");
		
		// Move to get the power up
		client.processCommand(Operation.MOVE_DOWN);
		sleep(200);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		
		assertTrue(player.getHealth() == (startHealth + 1));
		assertFalse(square.hasPowerUp());
	}

	private static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
