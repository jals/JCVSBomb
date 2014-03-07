package bomberman.common.model;

import java.awt.Point;
import java.io.Serializable;

/**
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
	 * @param fuseTime : The length of time until the bomb should explode
	 */
	public Bomb(Point location, int fuseTime) {
		this.location = location;
		this.fuseTime = fuseTime;
	}

	public Point getLocation() {
		return location;
	}

	public int getFuseTime() {
		return fuseTime;
	}

	public void decrementFuseTime(int time) {
		fuseTime -= time;
	}

	public String toString() {
		return "Bomb";
	}
}
