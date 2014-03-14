package bomberman.test.unit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Point;

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

public class BombUnitTests {
	
	static ServerThread serverThread;
	static ClientThread clientThread;
	static Client client;
	static Server server;
	
	private static String PLAYER_NAME = "test";

	@BeforeClass
	public static void setUp() throws Exception {
		serverThread = new ServerThread();
		clientThread = new ClientThread(PLAYER_NAME);
		
		serverThread.start();
		clientThread.start();
		
		client = clientThread.getClient();
		server = serverThread.getServer();
		
		// Give the threads some time to start
		sleep(500);
		
		assertTrue(server.isRunning());
		assertTrue(client.isRunning());
		
		// Join the game
		System.out.println("Joining game...");
		client.processCommand(Operation.JOIN_GAME);
		Player player = server.getPlayer(PLAYER_NAME);
		assertTrue(player != null);
		assertTrue(player.getName().equals(PLAYER_NAME));
		
		// Start the game
		System.out.println("Starting game...");
		client.processCommand(Operation.START_GAME);
		sleep(100);
		assertTrue(server.isRunning());
		assertTrue(server.isGameStarted());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		serverThread.shutdown();
		clientThread.shutdown();
		// Give the threads some time to stop
		sleep(500);
	}
	
	@Test
	public void testDropBomb() {
		BombFactory factory = server.getBombFactory();
		Point position = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.DROP_BOMB);
		sleep(100);
		assertTrue(factory.isBombAt(position));
		
		// Move out of the way so we dont get blown up
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(100);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(100);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(100);
		Player player = server.getPlayer(PLAYER_NAME);
		assertTrue(player.isAlive());
		
		// Drop another bomb, but dont move out of the way this time
		position = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.DROP_BOMB);
		sleep(100);
		assertTrue(factory.isBombAt(position));
		
		// Wait for the bomb to explode
		System.out.println("Waiting for bomb to explode...");
		sleep(6000);
		System.out.println("Done waiting.");
		
		player = server.getPlayer(PLAYER_NAME);
		assertTrue(player == null);
		
	}

	private static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
