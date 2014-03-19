package bomberman.common.model;

import java.awt.Point;
import java.io.Serializable;

/**
 * 
 * @author Jarred Linthorne
 * 
 */
public class PowerUp implements Serializable {
	
	public enum Powers {
		HEALTH_UP, BOMB_INCREASED_RADIUS;
	}

	private static final long serialVersionUID = 5359130147473094881L;
	private Point location;
	private Powers power;

	/**
	 * @param location
	 *            The location that the door is located at
	 */
	public PowerUp(Point location, Powers power) {
		this.location = location;
		this.power = power;
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

	public Powers getPower() {
		return power;
	}

	public void setPower(Powers power) {
		this.power = power;
	}
}
