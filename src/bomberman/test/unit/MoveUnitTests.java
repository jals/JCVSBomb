package bomberman.test.unit;

import static org.junit.Assert.assertTrue;

import java.awt.Point;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import bomberman.client.Client;
import bomberman.common.Command.Operation;
import bomberman.common.model.Player;
import bomberman.server.Server;
import bomberman.test.ClientThread;
import bomberman.test.ServerThread;

public class MoveUnitTests {
	
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
	}

	@AfterClass
	public static void tearDown() throws Exception {
		serverThread.shutdown();
		clientThread.shutdown();
	}
	
	@Test
	public void testStarted() {
		assertTrue(server.isRunning());
		assertTrue(client.isRunning());
	}

	@Test
	public void testJoinGame() {
		client.processCommand(Operation.JOIN_GAME);
		Player player = server.getPlayer(PLAYER_NAME);
		assertTrue(player != null);
		assertTrue(player.getName().equals(PLAYER_NAME));
	}
	
	@Test
	public void testStartGame() throws InterruptedException {
		client.processCommand(Operation.START_GAME);
		sleep(100);
		assertTrue(server.isRunning());
		assertTrue(server.isGameStarted());
	}
	
	@Test
	public void testMove() {
		// Test moving down
		Point initial = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.MOVE_DOWN);
		sleep(100);
		Point end = server.getPlayerLocation(PLAYER_NAME);
		assertTrue(initial.y == end.y);
		assertTrue(end.x == (initial.x+1));

		// Test moving up
		initial = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.MOVE_UP);
		sleep(100);
		end = server.getPlayerLocation(PLAYER_NAME);
		assertTrue(initial.y == end.y);
		assertTrue(end.x == (initial.x-1));

		// Test moving right
		initial = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(100);
		end = server.getPlayerLocation(PLAYER_NAME);
		assertTrue((initial.y+1) == end.y);
		assertTrue(end.x == initial.x);

		// Test moving left
		initial = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.MOVE_LEFT);
		sleep(100);
		end = server.getPlayerLocation(PLAYER_NAME);
		assertTrue((initial.y-1) == end.y);
		assertTrue(end.x == initial.x);
	}
	
	@Test
	public void testMoveIntoWall() {
		Point initial = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.MOVE_UP);
		client.processCommand(Operation.MOVE_UP);
		client.processCommand(Operation.MOVE_UP);
		sleep(100);
		Point end = server.getPlayerLocation(PLAYER_NAME);
		assertTrue(initial.y == end.y);
		assertTrue(end.x == initial.x);
	}

	private static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
		}
	}
	
}
