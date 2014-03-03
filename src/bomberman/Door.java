package bomberman;

import java.awt.Point;
import java.io.Serializable;

/**
 * This is the class representation of the door/the exit to the level
 * 
 * @author Jarred Linthorne
 *
 */
public class Door implements Serializable {

	private static final long serialVersionUID = 8330869684136249381L;
	private Point location;
	private boolean isVisible;

	/**
	 * @param location : The location that the door is located at
	 * @param isVisible : Whether or not the door is visible to the client
	 */
	public Door(Point location, boolean isVisible) {
		this.location = location;
		this.isVisible = isVisible;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public String toString() {
		return "Exit";
	}
}
