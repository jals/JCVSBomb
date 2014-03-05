package bomberman;

import java.awt.Point;
import java.io.Serializable;

/**
 * This is the class representation of the door/the exit to the level
 * 
 * @author Jarred Linthorne
 *
 */
public class Bomb implements Serializable {

	private static final long serialVersionUID = -5488979320523687344L;
	private Point location;
	private int fuseTime;

	/**
	 * @param location : The location that the door is located at
	 * @param isVisible : Whether or not the door is visible to the client
	 */
	public Bomb(Point location, int fuseTime) {
		this.location = location;
		this.setFuseTime(fuseTime);
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public int getFuseTime() {
		return fuseTime;
	}

	public void setFuseTime(int fuseTime) {
		this.fuseTime = fuseTime;
	}

	public String toString() {
		return "Bomb";
	}
}
