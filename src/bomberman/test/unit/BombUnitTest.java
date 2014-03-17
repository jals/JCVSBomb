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
import bomberman.server.BombFactory;
import bomberman.server.Server;
import bomberman.test.ClientThread;
import bomberman.test.ServerThread;

public class BombUnitTest {
	
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
	public void testDropBomb() {
		System.out.println("Testing dropping bombs");
		BombFactory factory = server.getBombFactory();
		
		// Drop a bomb
		Point position = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.DROP_BOMB);
		sleep(200);
		assertTrue(factory.isBombAt(position));
		
		// Move out of the way so we dont get blown up
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		Player player = server.getPlayer(PLAYER_NAME);
		assertTrue(player.isAlive());
		
		// Drop another bomb, but dont move out of the way this time
		position = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.DROP_BOMB);
		sleep(200);
		assertTrue(factory.isBombAt(position));
		
		int startHealth = player.getHealth();
		
		// Wait for the bomb to explode
		System.out.println("Waiting for bomb to explode...");
		sleep(7000); // Bomb has max fuse of 6 seconds
		System.out.println("Done waiting.");
		
		// Make sure the bomb exploded
		assertFalse(factory.isBombAt(position));
		
		if (player != null) {
			assertTrue(player.getHealth() == (startHealth-1));
		}
	}

	private static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
