package bomberman.server;

import java.awt.Point;
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

	public void addBomb(Bomb b) {
		synchronized (bombs) {
			bombs.add(b);
		}
	}
	
	public void removeBomb(Bomb b) {
		synchronized (bombs) {
			bombs.remove(b);
		}
	}

	public void run() {
		while (server.isRunning()) {
			synchronized (bombs) {
				for (Bomb b : bombs) {
					b.decrementFuseTime(100);
				}
				List<Bomb> newBombs = new ArrayList<Bomb>(bombs.size());
				for (Bomb b : bombs) {
					if (b.getFuseTime() < 0) {
						server.bombExploded(b.getLocation().x,
								b.getLocation().y);
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

	public boolean isBombAt(Point point) {
		synchronized(bombs) {
			for (Bomb bomb : bombs) {
				if (bomb.getLocation().equals(point)) {
					return true;
				}
			}
			return false;
		}
	}

}
