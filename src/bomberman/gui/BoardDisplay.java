package bomberman.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import bomberman.common.model.Bomb;
import bomberman.common.model.Box;
import bomberman.common.model.Door;
import bomberman.common.model.Explosion;
import bomberman.common.model.Model;
import bomberman.common.model.Player;
import bomberman.common.model.PowerUp;
import bomberman.common.model.PowerUp.Powers;
import bomberman.common.model.Wall;

/**
 * This class is used by the GUI to create the visuals. The size of the
 * graphical units are specified here, as well as the font used. This class
 * creates the illusion of walls by painting the outer and inner walls of the
 * grid as different colours than the walkable area.
 * 
 * @author Jarred Linthorne
 * 
 */
public class BoardDisplay extends JComponent {
	private static final long serialVersionUID = -83694624035879827L;
	private static final int CELL_PIXELS = 75; // Size of each cell.
	private static final int PUZZLE_SIZE = 12; // Number of rows/cols
	private static final int X_BOARD_PIXELS = CELL_PIXELS * PUZZLE_SIZE;
	private static final int Y_BOARD_PIXELS = CELL_PIXELS * (PUZZLE_SIZE + 5);
	private static final int X_OFFSET = 18; // Fine tuning placement of text.
	private static final int Y_OFFSET = 28; // Fine tuning placement of text.
	private static final int X_PICTURE_OFFSET = 6; // Fine tuning placement of text.
	private static final int Y__PICTURE_OFFSET = 35; // Fine tuning placement of text.
	private static final Font TEXT_FONT = new Font("Sansserif", Font.BOLD, 24);

	private Model model;

	/**
	 * The constructor sets the size of the component and the default background
	 * colour
	 * 
	 * @param model
	 */
	public BoardDisplay(Model model) {
		setPreferredSize(new Dimension(Y_BOARD_PIXELS + 1, X_BOARD_PIXELS + 1));
		setBackground(Color.WHITE);
		this.model = model;
	}

	/**
	 * This method paints the background, and calls the methods to draw the
	 * walls and grid lines
	 * 
	 * @param g
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		try {
			fillOuterWalls(g);
		} catch (IOException e1) {
			System.err.println("Outer walls could not be created.");
		}
		g.setColor(Color.BLACK);
		drawGridLines(g);
		try {
			drawCellValues(g);
		} catch (IOException e) {
			System.err.println("Error reading image file.");
		}
	}

	/**
	 * This method is used to fill in the outer walls to show a boundary for the
	 * grid
	 * 
	 * @param g
	 * @throws IOException 
	 */
	private void fillOuterWalls(Graphics g) throws IOException {
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, CELL_PIXELS * PUZZLE_SIZE, CELL_PIXELS); // top border
		g.fillRect(0, 0, CELL_PIXELS, CELL_PIXELS * PUZZLE_SIZE); // left border
		g.fillRect(0, CELL_PIXELS * PUZZLE_SIZE - CELL_PIXELS, CELL_PIXELS
				* PUZZLE_SIZE, CELL_PIXELS * PUZZLE_SIZE); // bottom border
		g.fillRect(CELL_PIXELS * PUZZLE_SIZE - CELL_PIXELS, 0, CELL_PIXELS
				* PUZZLE_SIZE, CELL_PIXELS * PUZZLE_SIZE + 1); // right border
		g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Title.png")), 225, 0, null);
	}

	/**
	 * This method is used to draw each of the inner lines that segment the
	 * squares
	 * 
	 * @param g
	 */
	private void drawGridLines(Graphics g) {
		for (int i = 1; i <= PUZZLE_SIZE - 1; i++) {
			int grid = i * CELL_PIXELS;
			g.drawLine(grid, CELL_PIXELS, grid, X_BOARD_PIXELS - CELL_PIXELS);
			g.drawLine(CELL_PIXELS, grid, X_BOARD_PIXELS - CELL_PIXELS, grid);
		}
	}

	/**
	 * This method is used to draw the string values into the square to display
	 * names, etc.
	 * 
	 * @param g
	 * @throws IOException 
	 */
	private void drawCellValues(Graphics g) throws IOException {
		g.setFont(TEXT_FONT);
		ImageIcon imageIcon = null;
		
		ArrayList<Player> playerList = model.getPlayerList();
		Player[] players = new Player[playerList.size()]; //Create the array to avoid concurrent modification exceptions
		players = playerList.toArray(players);
		if(playerList != null){
			for(int k = 0; k < players.length; k++){
				String hasWon = "";
				if (players[k].hasWon()){
					hasWon = "Won";
				}
				g.drawString(hasWon, 11 * CELL_PIXELS + X_OFFSET - X_PICTURE_OFFSET, (3 + players[k].getIdentifier()) * CELL_PIXELS - Y_OFFSET);
				imageIcon = new ImageIcon(this.getClass().getResource("images/Downward" + players[k].getIdentifier() + ".png"));
				g.drawImage(imageIcon.getImage(), 12 * CELL_PIXELS + X_OFFSET - X_PICTURE_OFFSET, (3 + players[k].getIdentifier()) * CELL_PIXELS - Y_OFFSET - Y__PICTURE_OFFSET, null);
				g.drawString(players[k].getName(), 13 * CELL_PIXELS + X_OFFSET - X_PICTURE_OFFSET, (3 + players[k].getIdentifier()) * CELL_PIXELS - Y_OFFSET);
				int health = players[k].getHealth(); 
				int heartSpacer = 0;
				while(health > 0){
					imageIcon = new ImageIcon(this.getClass().getResource("images/Heart.png"));
					g.drawImage(imageIcon.getImage(), (15 + heartSpacer) * CELL_PIXELS - 20, (3 + players[k].getIdentifier()) * CELL_PIXELS - Y_OFFSET - Y__PICTURE_OFFSET, null);
					health--;
					heartSpacer++;
				}
			}
			
		}
		
		for (int i = 0; i < PUZZLE_SIZE; i++) {
			int yDisplacement = (i + 1) * CELL_PIXELS - Y_OFFSET;
			for (int j = 0; j < PUZZLE_SIZE; j++) {
				if (model.getVal(i, j) != null) {
					int xDisplacement = j * CELL_PIXELS + X_OFFSET;
					String value = model.getVal(i, j).toString();
					if (value.length() > 4) {
						value = model.getVal(i, j).toString().substring(0, 4);
					}
					Player p = model.getBoard()[i][j].getPlayer();
					Door d = model.getBoard()[i][j].getDoor();
					Bomb b = model.getBoard()[i][j].getBomb();
					Explosion ex = model.getBoard()[i][j].getExplosion();
					PowerUp pu = model.getBoard()[i][j].getPowerUp();
					Box box = model.getBoard()[i][j].getBox();
					Wall w = model.getBoard()[i][j].getWall();
					
					if(w != null){
						if((i!=0) || j!=1){ //don't draw this, used for getting the correct floor
							imageIcon = new ImageIcon(this.getClass().getResource("images/wall" + model.getBoard()[0][1].getWall().getFloor() + ".png"));
							g.drawImage(imageIcon.getImage(), xDisplacement - X_PICTURE_OFFSET - 12, yDisplacement - Y__PICTURE_OFFSET - 10, null);
						}
					} else if (ex != null) {
						 imageIcon = new ImageIcon(this.getClass().getResource("images/Explosion.gif"));
						 g.drawImage(imageIcon.getImage(), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
						 if(model.getBoard()[i][j+1] != null){ //don't go out of bounds
							 if(!model.getBoard()[i][j+1].hasWall()){ //don't blow up wall
								 g.drawImage(imageIcon.getImage(), (j+1) * CELL_PIXELS + X_OFFSET - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
							 }
						 }
						 if(model.getBoard()[i][j-1] != null){ //don't go out of bounds
							 if(!model.getBoard()[i][j-1].hasWall()){ //don't blow up wall
								 g.drawImage(imageIcon.getImage(), (j-1) * CELL_PIXELS + X_OFFSET - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
							 }
						 }
						 if(model.getBoard()[i+1][j] != null){ //don't go out of bounds
							 if(!model.getBoard()[i+1][j].hasWall()){ //don't blow up wall
								 g.drawImage(imageIcon.getImage(), xDisplacement - X_PICTURE_OFFSET, (i + 2) * CELL_PIXELS - Y_OFFSET - Y__PICTURE_OFFSET, null);
							 }
						 }
						 if(model.getBoard()[i-1][j] != null){ //don't go out of bounds
							 if(!model.getBoard()[i-1][j].hasWall()){ //don't blow up wall
								 g.drawImage(imageIcon.getImage(), xDisplacement - X_PICTURE_OFFSET, i * CELL_PIXELS - Y_OFFSET - Y__PICTURE_OFFSET, null);
							 }
						 }
						 if(ex.getBomb().getRadius() == 2){ //Explosions for superbomb
							 if(j+2 < Model.BOARD_SIZE && model.getBoard()[i][j+2] != null){ //don't go out of bounds
								 if(!model.getBoard()[i][j+2].hasWall()){ //don't blow up wall
									 g.drawImage(imageIcon.getImage(), (j+2) * CELL_PIXELS + X_OFFSET - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
								 }
							 }
							 if(j-2 > 0 && model.getBoard()[i][j-2] != null){ //don't go out of bounds
								 if(!model.getBoard()[i][j-2].hasWall()){ //don't blow up wall
									 g.drawImage(imageIcon.getImage(), (j-2) * CELL_PIXELS + X_OFFSET - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
								 }
							 }
							 if(i+2 < Model.BOARD_SIZE && model.getBoard()[i+2][j] != null){ //don't go out of bounds
								 if(!model.getBoard()[i+2][j].hasWall()){ //don't blow up wall
									 g.drawImage(imageIcon.getImage(), xDisplacement - X_PICTURE_OFFSET, (i + 3) * CELL_PIXELS - Y_OFFSET - Y__PICTURE_OFFSET, null);
								 }
							 }
							 if(i-2 > 0 && model.getBoard()[i-2][j] != null){ //don't go out of bounds
								 if(!model.getBoard()[i-2][j].hasWall()){ //don't blow up wall
									 g.drawImage(imageIcon.getImage(), xDisplacement - X_PICTURE_OFFSET, (i - 1) * CELL_PIXELS - Y_OFFSET - Y__PICTURE_OFFSET, null);
								 }
							 }
							 if(model.getBoard()[i+1][j+1] != null){ //don't go out of bounds
								 if(!model.getBoard()[i+1][j+1].hasWall()){ //don't blow up wall
									 g.drawImage(imageIcon.getImage(), (j+1) * CELL_PIXELS + X_OFFSET - X_PICTURE_OFFSET, (i + 2) * CELL_PIXELS - Y_OFFSET - Y__PICTURE_OFFSET, null);
								 }
							 }
							 if(model.getBoard()[i-1][j-1] != null){ //don't go out of bounds
								 if(!model.getBoard()[i-1][j-1].hasWall()){ //don't blow up wall
									 if((i-1) != 0 && (j-1) != 0){	//handle 0, 0 case
										 g.drawImage(imageIcon.getImage(), (j-1) * CELL_PIXELS + X_OFFSET - X_PICTURE_OFFSET, i * CELL_PIXELS - Y_OFFSET - Y__PICTURE_OFFSET, null);
									 }
								 }
							 }
							 if(model.getBoard()[i+1][j-1] != null){ //don't go out of bounds
								 if(!model.getBoard()[i+1][j-1].hasWall()){ //don't blow up wall
									 g.drawImage(imageIcon.getImage(), (j-1) * CELL_PIXELS + X_OFFSET - X_PICTURE_OFFSET, (i + 2) * CELL_PIXELS - Y_OFFSET - Y__PICTURE_OFFSET, null);
								 }
							 }
							 if(model.getBoard()[i-1][j+1] != null){ //don't go out of bounds
								 if(!model.getBoard()[i-1][j+1].hasWall()){ //don't blow up wall
									 g.drawImage(imageIcon.getImage(), (j+1) * CELL_PIXELS + X_OFFSET - X_PICTURE_OFFSET, i * CELL_PIXELS - Y_OFFSET - Y__PICTURE_OFFSET, null);
								 }
							 }
						 }
					} else if(p != null){
						if(!p.hasWon()){
							if(p.getName().equals("Enemy1")){
								g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Enemy1.png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
							} else if(p.getName().equals("Enemy2")){
								g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Enemy2.png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
							} else if(p.getLastDirection() == Model.LEFT){
								g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Leftward" + p.getIdentifier() + ".png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
							} else if(p.getLastDirection() == Model.RIGHT){
								g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Rightward" + p.getIdentifier() + ".png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
							} else if(p.getLastDirection() == Model.UP){
								g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Upward" + p.getIdentifier() + ".png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
							} else {
								g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Downward" + p.getIdentifier() + ".png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
							}
						}
					} else if (d != null){
						if (d.isVisible()){
							g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/DoorHole.png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
						} else {
							g.drawString(value, xDisplacement, yDisplacement);
						}
					} else if (b != null) {
						if(b.getRadius() == 2){
							imageIcon = new ImageIcon(this.getClass().getResource("images/SuperBomb.gif"));
						} else {
							imageIcon = new ImageIcon(this.getClass().getResource("images/Bomb.gif"));
						}
						g.drawImage(imageIcon.getImage(), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
					} else if (pu != null){
						if(pu.getPower().equals(Powers.HEALTH_UP)){
							imageIcon = new ImageIcon(this.getClass().getResource("images/Heart.gif"));
							g.drawImage(imageIcon.getImage(), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
						} else if(pu.getPower().equals(Powers.INVINCIBILITY)){
							imageIcon = new ImageIcon(this.getClass().getResource("images/star.gif"));
							g.drawImage(imageIcon.getImage(), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
						} else if(pu.getPower().equals(Powers.BOMB_INCREASED_RADIUS)){
							imageIcon = new ImageIcon(this.getClass().getResource("images/SuperBomb.png"));
							g.drawImage(imageIcon.getImage(), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
						}
					} else if (box != null){
						g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Box.png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
					} else {
						g.drawString(value, xDisplacement, yDisplacement);
					}
				}
			}
		}
	}

}