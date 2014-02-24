package bomberman;

public class Client {
	private String ip;
	private String playerName;
	private Object[][] grid;
	
	public Client(){
		joinGame();	//Code to make the methods not have warnings
		move("up");
		leaveGame();
	}
	
	public static void main(String[] args){
		
	}
	
	private void move(String direction){
		
	}
	
	private void joinGame(){
		
	}
	
	private void leaveGame(){
		
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public Object[][] getGrid() {
		return grid;
	}

	public void setGrid(Object[][] grid) {
		this.grid = grid;
	}
}
