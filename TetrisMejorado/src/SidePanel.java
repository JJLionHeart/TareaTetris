import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * The {@code SidePanel} class is responsible for displaying various information
 * on the game such as the next piece, the score and current level, and controls.
 * @author Brendan Jones
 *
 */
public class SidePanel extends JPanel {
	
	/**
	 * Serial Version UID.
	 */
	private static final long lnSerialVersionUID = 2181495598854992747L;

	/**
	 * The dimensions of each tile on the next piece preview.
	 */
	private static final int iTILE_SIZE = BoardPanel.iTILE_SIZE >> 1;
	
	/**
	 * The width of the shading on each tile on the next piece preview.
	 */
	private static final int iSHADE_WIDTH = BoardPanel.iSHADE_WIDTH >> 1;
	
	/**
	 * The number of rows and columns in the preview window. Set to
	 * 5 because we can show any piece with some sort of padding.
	 */
	private static final int iTILE_COUNT = 5;
	
	/**
	 * The center x of the next piece preview box.
	 */
	private static final int iSQUARE_CENTER_X = 130;
	
	/**
	 * The center y of the next piece preview box.
	 */
	private static final int iSQUARE_CENTER_Y = 65;
	
	/**
	 * The size of the next piece preview box.
	 */
	private static final int iSQUARE_SIZE = (iTILE_SIZE * iTILE_COUNT >> 1);
	
	/**
	 * The number of pixels used on a small insets (generally used for categories).
	 */
	private static final int iSMALL_INSET = 20;
	
	/**
	 * The number of pixels used on a large insets.
	 */
	private static final int iLARGE_INSET = 40;
	
	/**
	 * The y coordinate of the stats category.
	 */
	private static final int iSTATS_INSET = 150;
	
	/**
	 * The y coordinate of the controls category.
	 */
	private static final int iCONTROLS_INSET = 275;
	
	/**
	 * The number of pixels to offset between each string.
	 */
	private static final int iTEXT_STRIDE = 25;
	
	/**
	 * The small font.
	 */
	private static final Font fonSMALL_FONT = new Font("Tahoma", Font.BOLD
                , 11);
	
	/**
	 * The large font.
	 */
	private static final Font fonLARGE_FONT = new Font("Tahoma", Font.BOLD
                , 13);
	
	/**
	 * The color to draw the text and preview box in.
	 */
	private static final Color colDRAW_COLOR = new Color(128, 192, 128);
	
	/**
	 * The Tetris instance.
	 */
	private Tetris tetTetris;
	
	/**
	 * Creates a new SidePanel and sets it's display properties.
	 * @param tetris The Tetris instance to use.
	 */
	public SidePanel(Tetris tetTetris) {
		this.tetTetris = tetTetris;
		
		setPreferredSize(new Dimension(200, BoardPanel.iPANEL_HEIGHT));
		setBackground(Color.BLACK);
	}
	
	@Override
	public void paintComponent(Graphics graGraphics) {
		super.paintComponent(graGraphics);
		
		//Set the color for drawing.
		graGraphics.setColor(colDRAW_COLOR);
		
		/*
		 * This variable stores the current y coordinate of the string.
		 * This way we can re-order, add, or remove new strings if necessary
		 * without needing to change the other strings.
		 */
		int iOffset;
		
		/*
		 * Draw the "Stats" category.
		 */
		graGraphics.setFont(fonLARGE_FONT);
		graGraphics.drawString("Stats", iSMALL_INSET, iOffset = iSTATS_INSET);
		graGraphics.setFont(fonSMALL_FONT);
		graGraphics.drawString("Level: " + tetTetris.getLevel()
                        , iLARGE_INSET, iOffset += iTEXT_STRIDE);
		graGraphics.drawString("Score: " + tetTetris.getScore()
                        , iLARGE_INSET, iOffset += iTEXT_STRIDE);
		
		/*
		 * Draw the "Controls" category.
		 */
		graGraphics.setFont(fonLARGE_FONT);
		graGraphics.drawString("Controls", iSMALL_INSET
                        , iOffset = iCONTROLS_INSET-30);
		graGraphics.setFont(fonSMALL_FONT);
		graGraphics.drawString("A - Move Left", iLARGE_INSET
                        , iOffset += iTEXT_STRIDE);
		graGraphics.drawString("D - Move Right", iLARGE_INSET
                        , iOffset += iTEXT_STRIDE);
		graGraphics.drawString("Q - Rotate Anticlockwise"
                        , iLARGE_INSET, iOffset += iTEXT_STRIDE);
		graGraphics.drawString("E - Rotate Clockwise", iLARGE_INSET
                        , iOffset += iTEXT_STRIDE);
		graGraphics.drawString("S - Drop", iLARGE_INSET
                        , iOffset += iTEXT_STRIDE);
		graGraphics.drawString("P - Pause Game", iLARGE_INSET
                        , iOffset += iTEXT_STRIDE);
		graGraphics.drawString("G - Save Game", iLARGE_INSET
                        , iOffset += iTEXT_STRIDE);
                graGraphics.drawString("C - Load Game", iLARGE_INSET
                        , iOffset += iTEXT_STRIDE);
                graGraphics.drawString("M - Mute Sound", iLARGE_INSET
                        , iOffset += iTEXT_STRIDE);
		/*
		 * Draw the next piece preview box.
		 */
		graGraphics.setFont(fonLARGE_FONT);
		graGraphics.drawString("Next Piece:", iSMALL_INSET, 70);
		graGraphics.drawRect(iSQUARE_CENTER_X - iSQUARE_SIZE
                        , iSQUARE_CENTER_Y - iSQUARE_SIZE, iSQUARE_SIZE * 2
                        , iSQUARE_SIZE * 2);
		
		/*
		 * Draw a preview of the next piece that will be spawned. The code is pretty much
		 * identical to the drawing code on the board, just smaller and centered, rather
		 * than constrained to a grid.
		 */
		TileType tltType = tetTetris.getNextPieceType();
		if(!tetTetris.isGameOver() && tltType != null) {
			/*
			 * Get the size properties of the current piece.
			 */
			int iCols = tltType.getCols();
			int iRows = tltType.getRows();
			int iDimension = tltType.getDimension();
		
			/*
			 * Calculate the top left corner (origin) of the piece.
			 */
			int iStartX = (iSQUARE_CENTER_X 
                                - (iCols * iTILE_SIZE / 2));
			int iStartY = (iSQUARE_CENTER_Y 
                                - (iRows * iTILE_SIZE / 2));
		
			/*
			 * Get the insets for the preview. The default
			 * rotation is used for the preview, so we just use 0.
			 */
			int iTop = tltType.getTopInset(0);
			int iLeft = tltType.getLeftInset(0);
		
			/*
			 * Loop through the piece and draw it's tiles onto the preview.
			 */
			for(int iRow = 0; iRow < iDimension; iRow++) {
				for(int col = 0; col < iDimension; col++) {
					if(tltType.isTile(col, iRow, 0)) {
						drawTile(tltType
                                                        , iStartX + 
                                                        ((col - iLeft) 
                                                                * iTILE_SIZE), 
                                                        iStartY + 
                                                                ((iRow - iTop) 
                                                                   * iTILE_SIZE)
                                                        , graGraphics);
					}
				}
			}
		}
	}
	
	/**
	 * Draws a tile onto the preview window.
	 * @param tltType The type of tile to draw.
	 * @param x The x coordinate of the tile.
	 * @param y The y coordinate of the tile.
	 * @param g The graphics object.
	 */
	private void drawTile(TileType tltType, int iX, int iY
                , Graphics graGraphics) {
		/*
		 * Fill the entire tile with the base color.
		 */
		graGraphics.setColor(tltType.getBaseColor());
		graGraphics.fillRect(iX, iY, iTILE_SIZE, iTILE_SIZE);
		
		/*
		 * Fill the bottom and right edges of the tile with the dark shading color.
		 */
		graGraphics.setColor(tltType.getDarkColor());
		graGraphics.fillRect(iX, iY + iTILE_SIZE - iSHADE_WIDTH
                        , iTILE_SIZE, iSHADE_WIDTH);
		graGraphics.fillRect(iX + iTILE_SIZE - iSHADE_WIDTH, iY
                        , iSHADE_WIDTH, iTILE_SIZE);
		
		/*
		 * Fill the top and left edges with the light shading. We draw a single line
		 * for each row or column rather than a rectangle so that we can draw a nice
		 * looking diagonal where the light and dark shading meet.
		 */
		graGraphics.setColor(tltType.getLightColor());
		for(int i = 0; i < iSHADE_WIDTH; i++) {
			graGraphics.drawLine(iX, iY + i, iX + iTILE_SIZE - i - 1
                                , iY + i);
			graGraphics.drawLine(iX + i, iY, iX + i, iY + iTILE_SIZE
                                - i - 1);
		}
	}
	
}
