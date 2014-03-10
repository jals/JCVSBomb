/**
 * Author Vinayak Bansal
 * 
 */

package bomberman.server;

import java.awt.Point;
import java.util.Random;

import bomberman.common.Command.Operation;
import bomberman.common.Utility;
import bomberman.common.model.Player;

public class Enemy implements Runnable {
	Player p;
	Server s;
	
	public Enemy(Server s, Player p) {
		this.s = s;
		this.p = p;
	}

	@Override
	public void run() {
		Random rand = new Random();
		while (true) {
			Operation o = null;
			int value = rand.nextInt() % 4;
			switch (value) {
			case 0:
				o = Operation.MOVE_DOWN;
				break;
			case 1:
				o = Operation.MOVE_LEFT;
				break;
			case 2:
				o = Operation.MOVE_RIGHT;
				break;
			case 3:
				o = Operation.MOVE_UP;
				break;
			}
			Point location = p.getLocation();
			Point newLocation = Utility.getLocation(o, location);
			if (s.canGo(newLocation.x, newLocation.y)) {
				p.setLocation(newLocation);
				s.refreshGrid();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
