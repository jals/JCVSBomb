package bomberman.common.model;

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
	private Door door;
	private PowerUp powerUp;

	/**
	 * @param location
	 *            The location that the door is located at
	 */
	public Box(Point location, Door door, PowerUp powerUp) {
		this.location = location;
		this.door = door;
		this.powerUp = powerUp;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public String toString() {
		return "Box";
	}

	public Door getDoor() {
		return door;
	}

	public void setDoor(Door door) {
		this.door = door;
	}

	public PowerUp getPowerUp() {
		return powerUp;
	}

	public void setPowerUp(PowerUp powerUp) {
		this.powerUp = powerUp;
	}
}
