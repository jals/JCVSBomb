package bomberman;

import java.awt.Point;
import java.io.Serializable;

/**
 * 
 * @author Jarred Linthorne
 *
 */
public class Box implements Serializable {

	private static final long serialVersionUID = 2644898360748483411L;
	private Point location;

	/**
	 * @param location : The location that the door is located at
	 */
	public Box(Point location) {
		this.location = location;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public String toString() {
		return "Bomb";
	}
}
