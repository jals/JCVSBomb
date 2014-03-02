package bomberman;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Square implements Serializable {

	private static final long serialVersionUID = -1121507013822267342L;
	List<Object> objects;

	public Square() {
		objects = new ArrayList<Object>();
	}

	public List<Object> getObjects() {
		return objects;
	}

	public void addObject(Object o) {
		objects.add(o);
	}

	public void removePlayers() {
		List<Object> newObjects = new ArrayList<Object>();
		for (int x = 0; x < objects.size(); x++) {
			if (!(objects.get(x) instanceof Player)) {
				newObjects.add(objects.get(x));
			}
		}
		objects = newObjects;
	}

	public boolean hasMultiplePlayers() {
		return numPlayers() > 1;
	}

	public String toString() {
		for (Object o : objects) {
			if (o instanceof Door) {
				if (((Door) o).isVisible()) {
					return o.toString() + "";
				} else {
					return "D"; // TODO change to empty string to fully hide
								// door
				}
			} else if (o instanceof Player) {
				return ((Player) o).getName();
			}
		}

		return "";
	}

	public boolean hasWall() {
		return objects.contains(1);
	}

	public int numPlayers() {
		int numPlayers = 0;
		for (int x = 0; x < objects.size(); x++) {
			if (objects.get(x) instanceof Player) {
				numPlayers++;
			}
		}
		return numPlayers;

	}

	public void removeLast() {
		objects.remove(objects.size() - 1);
	}

}
