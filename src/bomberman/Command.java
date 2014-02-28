package bomberman;

import java.io.Serializable;

public class Command implements Serializable{
	
	public enum Operation {
		MOVE_LEFT,
		MOVE_RIGHT,
		MOVE_UP,
		MOVE_DOWN, 
		JOIN_GAME, 
		LEAVE_GAME,
		DROP_BOMB;
		
		public boolean isMove() {
			return (this.ordinal() <= MOVE_DOWN.ordinal());
		}
	}
	
	private static final long serialVersionUID = 7342838579737714576L;

	private String player;
	private Operation operation;
	
	public Command(String player, Operation operation) {
		this.player = player;
		this.operation = operation;
	}
	
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
