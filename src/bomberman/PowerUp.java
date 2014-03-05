package bomberman;

import java.awt.Point;
import java.io.Serializable;

/**
 * This is the class representation of the door/the exit to the level
 * 
 * @author Jarred Linthorne
 *
 */
public class PowerUp implements Serializable {

	private static final long serialVersionUID = 5359130147473094881L;
	private Point location;

	/**
	 * @param location : The location that the door is located at
	 * @param isVisible : Whether or not the door is visible to the client
	 */
	public PowerUp(Point location) {
		this.location = location;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public String toString() {
		return "PowerUp";
	}
}
