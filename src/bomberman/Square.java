/**
 * Author: Vinayak Bansal
 * Dated: Mar 3, 2014
 * 
 * This represents one square on the grid. It is nothing
 * fancier than a grid of objects. This is required, as we can have
 * multiple objects at a square. It forms the third dimension for a 
 * 2-D array.
 * TODO In the future, have an enum, instead of generic objects.
 */
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

	/**
	 * Removes any players that are in this square.
	 * Makes it easy when refreshing the grid.
	 */
	public void removePlayers() {
		List<Object> newObjects = new ArrayList<Object>();
		for (int x = 0; x < objects.size(); x++) {
			if (!(objects.get(x) instanceof Player)) {
				newObjects.add(objects.get(x));
			}
		}
		objects = newObjects;
	}
	
	
	public PowerUp removePowerUp() {
		PowerUp toReturn = null;
		List<Object> newObjects = new ArrayList<Object>();
		for (int x = 0; x < objects.size(); x++) {
			if (!(objects.get(x) instanceof PowerUp)) {
				newObjects.add(objects.get(x));
			} else {
				toReturn = (PowerUp) objects.get(x);
			}
		}
		objects = newObjects;
		return toReturn;
	}
	
	public Player getPlayer(){
		for (int x = 0; x < objects.size(); x++) {
			if ((objects.get(x) instanceof Player)) {
				return (Player) objects.get(x);
			}
		}
		return null;
	}

	public boolean hasMultiplePlayers() {
		return numPlayers() > 1;
	}

	/**
	 * String representation for what to print on the GUI
	 */
	public String toString() {
		for (Object o : objects) {
			if (o instanceof Door) {
				if (((Door) o).isVisible()) {
					return o.toString() + "";
				} else {
					// TODO change to empty string to fully hide door
					return "D"; 
				}
			} else if (o instanceof Player) {
				return ((Player) o).getName();
			}
		}

		return "";
	}

	/**
	 * Handy method to see if the player can come to this square
	 * @return true if this square is a wall. False, otherwise.
	 */
	public boolean canGo() {
		for(Object object: objects) {
			if ((object instanceof Box) || (object instanceof Wall)){
				return false;
			}
		}
		return true;
	}
	
	public boolean hasPowerUp() {
		for(Object object: objects) {
			if (object instanceof PowerUp){
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the number of players in this square.
	 */
	public int numPlayers() {
		int numPlayers = 0;
		for (int x = 0; x < objects.size(); x++) {
			if (objects.get(x) instanceof Player) {
				numPlayers++;
			}
		}
		return numPlayers;

	}

	public boolean hasDoor() {
		for (int x = 0; x < objects.size(); x++) {
			if (objects.get(x) instanceof Door) {
				return true;
			}
		}
		return false;
	}
	
	public Door getDoor(){
		for (int x = 0; x < objects.size(); x++) {
			if (objects.get(x) instanceof Door) {
				return (Door) objects.get(x);
			}
		}
		return null;
	}
	/**
	 * This is to undo the addObject method
	 * This is required with fixing the grid.
	 */
	public void removeLast() {
		objects.remove(objects.size() - 1);
	}

}
