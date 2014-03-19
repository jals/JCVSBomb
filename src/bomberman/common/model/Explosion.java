package bomberman.common.model;

import java.awt.Point;
import java.io.Serializable;

public class Explosion implements Serializable {

	private static final long serialVersionUID = 4938349151126093263L;
	private boolean exploded;
	private Point location;
	private Bomb bomb;

	public Explosion(boolean exploded, Point location, Bomb bomb) {
		this.exploded = exploded;
		this.location = location;
		this.bomb = bomb;
	}

	public boolean isExploded() {
		return exploded;
	}

	public void setExploded(boolean exploded) {
		this.exploded = exploded;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public Bomb getBomb() {
		return bomb;
	}

	public void setBomb(Bomb bomb) {
		this.bomb = bomb;
	}
}
