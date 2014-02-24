package bomberman;

import java.io.Serializable;

public class Command implements Serializable{

	private static final long serialVersionUID = 7342838579737714576L;

	private String player;
	private Operation operation;
	
	public String getPlayer() {
		return player;
	}
	
	public void setPlayer(String player) {
		this.player = player;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
}
