package bomberman.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;

import bomberman.Door;
import bomberman.Model;
import bomberman.Square;

public class BoardDisplay  extends JComponent {
	private static final long serialVersionUID = -83694624035879827L;
    private static final int CELL_PIXELS = 75;  // Size of each cell.
    private static final int PUZZLE_SIZE = 12;   // Number of rows/cols
    private static final int BOARD_PIXELS = CELL_PIXELS * PUZZLE_SIZE;
    private static final int X_OFFSET = 18;  // Fine tuning placement of text.
    private static final int Y_OFFSET = 28;  // Fine tuning placement of text.
    private static final Font TEXT_FONT  = new Font("Sansserif", Font.BOLD, 24);
    
    private Model model;
    
    public BoardDisplay(Model model) {
        setPreferredSize(new Dimension(BOARD_PIXELS + 1, BOARD_PIXELS + 1));
        setBackground(Color.WHITE);
        this.model = model;
    }
    
    @Override 
    public void paintComponent(Graphics g) {
    	super.paintComponent(g);
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        fillOuterWalls(g);
        fillInnerWalls(g);
        g.setColor(Color.BLACK);
        drawGridLines(g);
        drawCellValues(g);
    }
    
    private void fillOuterWalls(Graphics g){
    	g.setColor(Color.DARK_GRAY);
    	g.fillRect(0, 0, CELL_PIXELS * PUZZLE_SIZE, CELL_PIXELS); //top border
    	g.fillRect(0, 0, CELL_PIXELS, CELL_PIXELS * PUZZLE_SIZE); //left border
    	g.fillRect(0, CELL_PIXELS * PUZZLE_SIZE - CELL_PIXELS, CELL_PIXELS * PUZZLE_SIZE, CELL_PIXELS * PUZZLE_SIZE); //right border
    	g.fillRect(CELL_PIXELS * PUZZLE_SIZE - CELL_PIXELS, 0, CELL_PIXELS * PUZZLE_SIZE, CELL_PIXELS * PUZZLE_SIZE); //bottom border
    }
    
    private void fillInnerWalls(Graphics g){
    	Square[][] b = model.getBoard();
    	g.setColor(Color.GRAY);
    	for(int i=1; i< PUZZLE_SIZE - 1; i++){
    		for(int j=1; j< PUZZLE_SIZE - 1; j++){
				if(!(b[i][j].getObjects().get(0) instanceof Door)){
					if((Integer)b[i][j].getObjects().get(0) == 1){ //fill all rectangles that have a value of 1
    					g.fillRect(j * CELL_PIXELS, i * CELL_PIXELS, 1 * CELL_PIXELS, 1 * CELL_PIXELS);
    				}
				}
    		}
    	}
    }
    
    private void drawGridLines(Graphics g) {
        for (int i = 1; i <= PUZZLE_SIZE - 1; i++) {
            int grid = i * CELL_PIXELS;
            g.drawLine(grid, CELL_PIXELS, grid, BOARD_PIXELS - CELL_PIXELS);
            g.drawLine(CELL_PIXELS, grid, BOARD_PIXELS - CELL_PIXELS, grid);
        }
    }
    
    private void drawCellValues(Graphics g) {
        g.setFont(TEXT_FONT);
        for (int i = 0; i < PUZZLE_SIZE; i++) {
            int yDisplacement = (i+1) * CELL_PIXELS - Y_OFFSET;
            for (int j = 0; j < PUZZLE_SIZE; j++) {
                if (model.getVal(i, j) != null) {
                    int xDisplacement = j * CELL_PIXELS + X_OFFSET;
                    String value = model.getVal(i, j).toString();
                    if(value.length() > 4){
                    	value = model.getVal(i, j).toString().substring(0, 4);
                    }
                    g.drawString(value, xDisplacement, yDisplacement);
                }
            }
        }
    }
    
}