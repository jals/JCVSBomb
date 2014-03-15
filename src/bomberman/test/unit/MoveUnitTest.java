package bomberman.test.unit;

import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bomberman.client.Client;
import bomberman.common.Command.Operation;
import bomberman.common.model.Player;
import bomberman.server.BombFactory;
import bomberman.server.Server;
import bomberman.test.ClientThread;
import bomberman.test.ServerThread;

public class MoveUnitTest {
	
	static ServerThread serverThread;
	static ClientThread clientThread;
	static Client client;
	static Server server;
	
	private static String PLAYER_NAME = "test";

	@BeforeClass
	public static void setUp() throws Exception {
		Random rand = new Random();
		int  port = rand.nextInt(500) + 9500;
		
		serverThread = new ServerThread(port);
		clientThread = new ClientThread(PLAYER_NAME, port);
		
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
	public void testMove() {
		System.out.println("Testing move operations");
		
		// Test moving down
		Point initial = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.MOVE_DOWN);
		sleep(200);
		Point end = server.getPlayerLocation(PLAYER_NAME);
		assertTrue(initial.y == end.y);
		assertTrue(end.x == (initial.x+1));

		// Test moving up
		initial = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.MOVE_UP);
		sleep(200);
		end = server.getPlayerLocation(PLAYER_NAME);
		assertTrue(initial.y == end.y);
		assertTrue(end.x == (initial.x-1));

		// Test moving right
		initial = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		end = server.getPlayerLocation(PLAYER_NAME);
		assertTrue((initial.y+1) == end.y);
		assertTrue(end.x == initial.x);

		// Test moving left
		initial = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.MOVE_LEFT);
		sleep(200);
		end = server.getPlayerLocation(PLAYER_NAME);
		assertTrue((initial.y-1) == end.y);
		assertTrue(end.x == initial.x);
		
		// Test moving repeatedly up into the wall
		initial = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.MOVE_UP);
		client.processCommand(Operation.MOVE_UP);
		client.processCommand(Operation.MOVE_UP);
		sleep(200);
		end = server.getPlayerLocation(PLAYER_NAME);
		assertTrue(initial.y == end.y);
		assertTrue(end.x == initial.x);
	}

	private static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
