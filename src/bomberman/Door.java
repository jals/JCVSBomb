package bomberman;

import java.awt.Point;
import java.io.Serializable;

public class Door implements Serializable {

	private static final long serialVersionUID = 8330869684136249381L;
	private Point location;
	private boolean isVisible;

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
