package bomberman.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import bomberman.common.model.Bomb;
import bomberman.common.model.Door;
import bomberman.common.model.Explosion;
import bomberman.common.model.Model;
import bomberman.common.model.Player;
import bomberman.common.model.Square;

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
	private static final int BOARD_PIXELS = CELL_PIXELS * PUZZLE_SIZE;
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
		setPreferredSize(new Dimension(BOARD_PIXELS + 1, BOARD_PIXELS + 1));
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
		fillInnerWalls(g);
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
				* PUZZLE_SIZE, CELL_PIXELS * PUZZLE_SIZE); // right border
		g.fillRect(CELL_PIXELS * PUZZLE_SIZE - CELL_PIXELS, 0, CELL_PIXELS
				* PUZZLE_SIZE, CELL_PIXELS * PUZZLE_SIZE); // bottom border
		g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Title.png")), 225, 0, null);
	}

	/**
	 * This method is used to fill in the inner walls to show the squares that
	 * aren't able to be walked on
	 * 
	 * @param g
	 */
	private void fillInnerWalls(Graphics g) {
		Square[][] b = model.getBoard();
		g.setColor(Color.GRAY);
		for (int i = 1; i < PUZZLE_SIZE - 1; i++) {
			for (int j = 1; j < PUZZLE_SIZE - 1; j++) {
				if (!(b[i][j].getObjects().get(0) instanceof Door)) {
					// fill all rectangles that have a value of 1
					if (b[i][j].hasWall()) {
						g.fillRect(j * CELL_PIXELS, i * CELL_PIXELS,
								1 * CELL_PIXELS, 1 * CELL_PIXELS);
					}
				}
			}
		}
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
			g.drawLine(grid, CELL_PIXELS, grid, BOARD_PIXELS - CELL_PIXELS);
			g.drawLine(CELL_PIXELS, grid, BOARD_PIXELS - CELL_PIXELS, grid);
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
					if(p != null){
						if(p.getLastDirection() == Model.LEFT){
							g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Leftward" + p.getIdentifier() + ".png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
						} else if(p.getLastDirection() == Model.RIGHT){
							g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Rightward" + p.getIdentifier() + ".png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
						} else if(p.getLastDirection() == Model.UP){
							g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Upward" + p.getIdentifier() + ".png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
						} else {
							g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/Downward" + p.getIdentifier() + ".png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
						}
					} else if (d != null){
						if (d.isVisible()){
							g.drawImage(ImageIO.read(new File("src/bomberman/gui/images/DoorHole.png")), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
						} else {
							g.drawString(value, xDisplacement, yDisplacement);
						}
					} else if (ex != null) {
						 ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("images/Explosion.gif"));
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
					} else if (b != null) {
					    ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("images/Bomb.gif"));
					    g.drawImage(imageIcon.getImage(), xDisplacement - X_PICTURE_OFFSET, yDisplacement - Y__PICTURE_OFFSET, null);
					} else {
						g.drawString(value, xDisplacement, yDisplacement);
					}
				}
			}
		}
	}

}