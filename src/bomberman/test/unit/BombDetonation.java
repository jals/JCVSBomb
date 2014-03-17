package bomberman.test.unit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Point;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import bomberman.client.Client;
import bomberman.common.Command.Operation;
import bomberman.common.model.Bomb;
import bomberman.common.model.Player;
import bomberman.common.model.Square;
import bomberman.server.Server;
import bomberman.test.ClientThread;
import bomberman.test.ServerThread;

public class BombDetonation {
	
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
	public void testDropBomb() {
		System.out.println("Testing dropping bombs");
		
		// Drop a bomb
		Point position = server.getPlayerLocation(PLAYER_NAME);
		client.processCommand(Operation.DROP_BOMB);
		sleep(200);
		client.processCommand(Operation.MOVE_RIGHT);
		sleep(200);
		client.processCommand(Operation.MOVE_RIGHT);
		
		Square s = server.getSquare(position.x, position.y);
		Bomb b = s.getBomb();
		
		long test = System.currentTimeMillis() + b.getFuseTime();

		while(System.currentTimeMillis() < test){
			//waiting
			if(s.getBomb() == null){
				fail();
			}
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
