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
import bomberman.server.Server;
import bomberman.test.ClientThread;
import bomberman.test.ServerThread;

public class DoorUnitTest {
	
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
	public void testFindDoor() {
		assertFalse(server.getDoor().isVisible());
		
		// Move right 4 times to the door
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		
		assertTrue(server.getDoor().isVisible());
		
		Point location = server.getPlayerLocation(PLAYER_NAME);
		assertTrue(server.getDoor().getLocation().equals(location));
	}

	private static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
