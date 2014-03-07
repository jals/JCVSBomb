package bomberman.common.model;

import java.awt.Point;
import java.io.Serializable;

public class Explosion implements Serializable{

	private static final long serialVersionUID = 4938349151126093263L;
	private boolean exploded;
	private Point location;
	
	public Explosion(boolean exploded, Point location){
		this.exploded = exploded;
		this.location = location;
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
}
