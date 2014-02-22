package bomberman;

import java.awt.Point;

public class Player {

	private String playerName;
	private Point location;
	private Boolean isAlive;
	
	public Player(String playerName){
		this.playerName = playerName;
	}
	
	public String getName() {
		return playerName;
	}
	
	public void setName(String name) {
		this.playerName = name;
	}
	
	public Point getLocation() {
		return location;
	}
	
	public void setLocation(Point location) {
		this.location = location;
	}
	
	public Boolean getIsAlive() {
		return isAlive;
	}
	
	public void setIsAlive(Boolean isAlive) {
		this.isAlive = isAlive;
	}
	
	
}
