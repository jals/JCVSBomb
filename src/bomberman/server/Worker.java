package bomberman.server;

import java.awt.Point;
import java.net.DatagramSocket;

import bomberman.common.Command;
import bomberman.common.Utility;
import bomberman.common.model.Player;

/**
 * There is exactly one instance of Worker class for each player that requests
 * to join. Each instance has two threads, one for receiving commands, and one
 * for sending refreshes. For spectator classes, the first thread for receiving
 * messages is not started.
 * 
 * @author vinayakbansal
 * 
 */
class Worker extends Thread {
	Server server;
	DatagramSocket socket;
	Player p;

	public Worker(final Server server, final Player p,
			final DatagramSocket socket) {
		this.p = p;
		this.server = server;
		this.socket = socket;
		// just an ack saying you have joined the game
		Utility.sendMessage(socket, "joined", p.getAddress(), p.getPort());
		// Creating a separate thread to wait for a notify on the refresh
		// object,
		// and then send a new grid to the client.
		new Thread() {
			public void run() {
				while (server.isRunning()) {
					synchronized (server.getLock().readLock()) {
						Utility.sendMessage(socket, server.getGrid(),
								p.getAddress(), p.getPort());
					}
					if (!p.getIsAlive()) {
						done();
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
				socket.close();
				return;
			}
		}.start();
	}

	/**
	 * This method receives new messages from the client, processes them, and
	 * then asks the server to update itself.
	 */
	public void run() {
		// just an ack saying that the game has started.
		Utility.sendMessage(socket, Command.Operation.START_GAME,
				p.getAddress(), p.getPort());

		while (server.isRunning()) {
			Object o = Utility.receiveMessage(socket);
			Command c = (Command) o;

			if (c == null) {
				return;
			}

			// Log the command
			server.getLogger().logCommand(c);

			if (c.getOperation().isMove()) {
				Point location = p.getLocation();
				Point newLocation = Utility.getLocation(c.getOperation(),
						location);
				if (server.canGo(newLocation.x, newLocation.y)) {
					p.setLocation(newLocation);
				} else {
					server.getLogger().logError(c, "Cannot move here");
				}
			} else if (c.getOperation() == Command.Operation.LEAVE_GAME) {
				done();
				break;
			} else if (c.getOperation() == Command.Operation.DROP_BOMB) {
				server.addBomb(p.getLocation().x, p.getLocation().y);
			}
			server.refreshGrid();
		}
		socket.close();
		return;
	}

	/**
	 * The game is over. Tell the client that.
	 */
	private void done() {
		server.removePlayer(p);
		server.refreshGrid();
		Utility.sendMessage(socket, Command.Operation.LEAVE_GAME,
				p.getAddress(), p.getPort());

	}

}