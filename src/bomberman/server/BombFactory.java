package bomberman.server;

import java.util.ArrayList;
import java.util.List;

import bomberman.common.model.Bomb;

public class BombFactory extends Thread {

	private Server server;
	private List<Bomb> bombs;

	public BombFactory(Server server) {
		this.server = server;
		bombs = new ArrayList<Bomb>();
	}

	public synchronized void addBomb(Bomb b) {
		bombs.add(b);
	}

	public void run() {
		while (server.isRunning()) {
			for (Bomb b : bombs) {
				b.decrementFuseTime(100);
			}
			List<Bomb> newBombs = new ArrayList<Bomb>(bombs.size());
			for (Bomb b : bombs) {
				if (b.getFuseTime() < 0) {
					server.bombExploded(b.getLocation().x, b.getLocation().y);
				} else {
					newBombs.add(b);
				}
			}
			bombs = newBombs;

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
