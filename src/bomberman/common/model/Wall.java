package bomberman.common.model;

import java.awt.Point;
import java.io.Serializable;

/**
 * 
 * @author Jarred Linthorne
 *
 */
public class Wall implements Serializable {

	private static final long serialVersionUID = -3005295060876171995L;
	private Point location;

	/**
	 * @param location : The location that the door is located at
	 */
	public Wall(Point location) {
		this.location = location;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public String toString() {
		return "Wall";
	}
}
