
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.JPanel;

/**
 * The {@code BoardPanel} class is responsible for displaying the game grid and
 * handling things related to the game board.
 *
 * @author Brendan Jones
 *
 */
public class BoardPanel extends JPanel {

    /**
     * Serial Version UID.
     */
    private static final long lSerialVersionUID = 5055679736784226108L;

    /**
     * Minimum color component values for tiles. This is required if we want to
     * show both light and dark shading on our tiles.
     */
    public static final int iCOLOR_MIN = 35;

    /**
     * Maximum color component values for tiles. This is required if we want to
     * show both light and dark shading on our tiles.
     */
    public static final int iCOLOR_MAX = 255 - iCOLOR_MIN;

    /**
     * The width of the border around the game board.
     */
    private static final int iBORDER_WIDTH = 5;

    /**
     * The number of columns on the board.
     */
    public static final int iCOL_COUNT = 10;

    /**
     * The number of visible rows on the board.
     */
    private static final int iVISIBLE_ROW_COUNT = 20;

    /**
     * The number of rows that are hidden from view.
     */
    private static final int iHIDDEN_ROW_COUNT = 2;

    /**
     * The total number of rows that the board contains.
     */
    public static final int iROW_COUNT = iVISIBLE_ROW_COUNT
            + iHIDDEN_ROW_COUNT;

    /**
     * The number of pixels that a tile takes up.
     */
    public static final int iTILE_SIZE = 24;

    /**
     * The width of the shading on the tiles.
     */
    public static final int iSHADE_WIDTH = 4;

    /**
     * The central x coordinate on the game board.
     */
    private static final int iCENTER_X = iCOL_COUNT * iTILE_SIZE / 2;

    /**
     * The central y coordinate on the game board.
     */
    private static final int iCENTER_Y = iVISIBLE_ROW_COUNT * iTILE_SIZE / 2;

    /**
     * The total width of the panel.
     */
    public static final int iPANEL_WIDTH = iCOL_COUNT * iTILE_SIZE
            + iBORDER_WIDTH * 2;

    /**
     * The total height of the panel.
     */
    public static final int iPANEL_HEIGHT = iVISIBLE_ROW_COUNT
            * iTILE_SIZE + iBORDER_WIDTH * 2;

    /**
     * The larger font to display.
     */
    private static final Font fntLARGE_FONT = new Font("Tahoma", Font.BOLD, 16);

    /**
     * The smaller font to display.
     */
    private static final Font fntSMALL_FONT = new Font("Tahoma", Font.BOLD, 12);

    /**
     * The Tetris instance.
     */
    private Tetris tetTetris;

    /**
     * The tiles that make up the board.
     */
    private TileType[][] tltTiles;

    /**
     * Variable para la Iluminacion.
     */
    private boolean bIluminar;
    private int iContador;

    /**
     * Sonidopara cuando se complete exitosamente la linea
     */
    private SoundClip sClipSuccess;

    /**
     * Crates a new GameBoard instance.
     *
     * @param tetris The Tetris instance to use.
     */
    public BoardPanel(Tetris tetris) {
        this.tetTetris = tetris;
        this.tltTiles = new TileType[iROW_COUNT][iCOL_COUNT];
        iContador = 50;
        bIluminar = false;
        setPreferredSize(new Dimension(iPANEL_WIDTH, iPANEL_HEIGHT));
        
        sClipSuccess = new SoundClip("success.wav");
    }

    /**
     * Resets the board and clears away any tiles.
     */
    public void clear() {
        /*
		 * Loop through every tile index and set it's value
		 * to null to clear the board.
         */
        for (int i = 0; i < iROW_COUNT; i++) {
            for (int j = 0; j < iCOL_COUNT; j++) {
                tltTiles[i][j] = null;
            }
        }
    }

    /**
     * Determines whether or not a piece can be placed at the coordinates.
     *
     * @param type THe type of piece to use.
     * @param iX The x coordinate of the piece.
     * @param iY The y coordinate of the piece.
     * @param iRotation The rotation of the piece.
     * @return Whether or not the position is valid.
     */
    public boolean isValidAndEmpty(TileType type, int iX, int iY, int iRotation) {

        //Ensure the piece is in a valid column.
        if (iX < -type.getLeftInset(iRotation) || iX + type.getDimension()
                - type.getRightInset(iRotation) >= iCOL_COUNT) {
            return false;
        }

        //Ensure the piece is in a valid row.
        if (iY < -type.getTopInset(iRotation) || iY + type.getDimension()
                - type.getBottomInset(iRotation) >= iROW_COUNT) {
            return false;
        }

        /*
		 * Loop through every tile in the piece and see if it 
                 * conflicts with an existing tile.
		 * 
		 * Note: It's fine to do this even though it allows for 
                 * wrapping because we've already
		 * checked to make sure the piece is in a valid location.
         */
        for (int iCol = 0; iCol < type.getDimension(); iCol++) {
            for (int iRow = 0; iRow < type.getDimension(); iRow++) {
                if (type.isTile(iCol, iRow, iRotation)
                        && isOccupied(iX + iCol, iY + iRow)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Adds a piece to the game board. Note: Doesn't check for existing pieces,
     * and will overwrite them if they exist.
     *
     * @param type The type of piece to place.
     * @param iX The x coordinate of the piece.
     * @param iY The y coordinate of the piece.
     * @param iRotation The rotation of the piece.
     */
    public void addPiece(TileType type, int iX, int iY, int iRotation) {
        /*
		 * Loop through every tile within the piece and add it
		 * to the board only if the boolean that represents that
		 * tile is set to true.
         */
        for (int iCol = 0; iCol < type.getDimension(); iCol++) {
            for (int iRow = 0; iRow < type.getDimension(); iRow++) {
                if (type.isTile(iCol, iRow, iRotation)) {
                    setTile(iCol + iX, iRow + iY, type);
                }
            }
        }
    }

    /**
     * Checks the board to see if any lines have been cleared, and removes them
     * from the game.
     *
     * @return The number of lines that were cleared.
     */
    public int checkLines() {
        int iCompletedLines = 0;

        /*
		 * Here we loop through every line and check it to see if
		 * it's been cleared or not. If it has, we increment the
		 * number of completed lines and check the next row.
		 * 
		 * The checkLine function handles clearing the line and
		 * shifting the rest of the board down for us.
         */
        for (int iRow = 0; iRow < iROW_COUNT; iRow++) {
            if (checkLine(iRow)) {
                iCompletedLines++;
                sClipSuccess.play();
            }
        }
        return iCompletedLines;
    }

    /**
     * Checks whether or not {@code row} is full.
     *
     * @param iLine The row to check.
     * @return Whether or not this row is full.
     */
    private boolean checkLine(int iLine) {
        /*
		 * Iterate through every column in this row. If any of them are
		 * empty, then the row is not full.
         */
        for (int iCol = 0; iCol < iCOL_COUNT; iCol++) {
            if (!isOccupied(iCol, iLine)) {
                return false;
            }
        }

        /*
		 * Since the line is filled, we need to 'remove' it from the game.
		 * To do this, we simply shift every row above it down by one.
         */
        for (int iRow = iLine - 1; iRow >= 0; iRow--) {
            for (int iCol = 0; iCol < iCOL_COUNT; iCol++) {
                setTile(iCol, iRow + 1, getTile(iCol, iRow));
            }
        }
        return true;
    }

    /**
     * Checks to see if the tile is already occupied.
     *
     * @param iX The x coordinate to check.
     * @param iY The y coordinate to check.
     * @return Whether or not the tile is occupied.
     */
    private boolean isOccupied(int iX, int iY) {
        return tltTiles[iY][iX] != null;
    }

    /**
     * Sets a tile located at the desired column and row.
     *
     * @param iX The column.
     * @param iY The row.
     * @param type The value to set to the tile to.
     */
    private void setTile(int iX, int iY, TileType type) {
        tltTiles[iY][iX] = type;
    }

    /**
     * Gets a tile by it's column and row.
     *
     * @param iX The column.
     * @param iY The row.
     * @return The tile.
     */
    private TileType getTile(int iX, int iY) {
        return tltTiles[iY][iX];
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //This helps simplify the positioning of things.
        g.translate(iBORDER_WIDTH, iBORDER_WIDTH);
        
        //Inicializo la variable para cambiar el brillo.
        /*
		 * Draw the board differently depending on the current game state.
         */
        if (tetTetris.isPaused()) {
            g.setFont(fntLARGE_FONT);
            g.setColor(Color.WHITE);
            String sMsg = "PAUSED";
            g.drawString(sMsg, iCENTER_X
                    - g.getFontMetrics().stringWidth(sMsg) / 2,
                    iCENTER_Y);
        } else if (tetTetris.isNewGame() || tetTetris.isGameOver()) {
           setBackground(Color.black);
            g.setFont(fntLARGE_FONT);
            g.setColor(Color.WHITE);

            /*
			 * Because both the game over and new game screens are 
                         * nearly identical, we can handle them together and 
                         * just use a ternary operator to change
			 * the messages that are displayed.
             */
            String sMsg = tetTetris.isNewGame() ? "TETRIS" : "GAME OVER";
            g.drawString(sMsg, iCENTER_X
                    - g.getFontMetrics().stringWidth(sMsg) / 2, 150);
            g.setFont(fntSMALL_FONT);
            sMsg = "Press Enter to Play"
                    + (tetTetris.isNewGame() ? "" : " Again");
            g.drawString(sMsg, iCENTER_X
                    - g.getFontMetrics().stringWidth(sMsg) / 2, 300);
        } else {
                URL urlFondo = this.getClass().getResource("bg.gif");
            Image imFondo = Toolkit.getDefaultToolkit().getImage(urlFondo);
            g.drawImage(imFondo, -5, -5, getWidth()+5, getHeight()+5, this);
            /*
			 * Draw the tiles onto the board.
             */
            for (int iX = 0; iX < iCOL_COUNT; iX++) {
                for (int iY = iHIDDEN_ROW_COUNT; iY < iROW_COUNT; iY++) {
                    TileType tile = getTile(iX, iY);
                    if (tile != null) {
                        drawTile(tile, iX
                                * iTILE_SIZE,
                                (iY - iHIDDEN_ROW_COUNT)
                                * iTILE_SIZE, g);
                    }
                }
            }

            /*
			 * Draw the current piece. This cannot be drawn like the rest of the
			 * pieces because it's still not part of the game board. If it were
			 * part of the board, it would need to be removed every frame which
			 * would just be slow and confusing.
             */
            TileType tltType = tetTetris.getPieceType();
            int iPieceCol = tetTetris.getPieceCol();
            int iPieceRow = tetTetris.getPieceRow();
            int iRotation = tetTetris.getPieceRotation();

            //Draw the piece onto the board.
            for (int iCol = 0; iCol < tltType.getDimension(); iCol++) {
                for (int iRow = 0; iRow < tltType.getDimension(); iRow++) {
                    if (iPieceRow + iRow >= 2 && tltType.isTile(iCol, iRow, iRotation)) {
                        if (bIluminar) {
                            drawTile(tltType, (iPieceCol + iCol) * iTILE_SIZE,
                                    (iPieceRow + iRow - iHIDDEN_ROW_COUNT)
                                    * iTILE_SIZE, g);
                        } else {
                            drawTile2(tltType, (iPieceCol + iCol) * iTILE_SIZE,
                                    (iPieceRow + iRow - iHIDDEN_ROW_COUNT)
                                    * iTILE_SIZE, g);
                        }
                    }
                }
            }

            //Vario la variable para el brillo
            if (iContador == 0) {
                iContador = 50;
                bIluminar = !bIluminar;
            }
            iContador--;
            /*
			 * Draw the ghost (semi-transparent piece that shows where the current piece will land). I couldn't think of
			 * a better way to implement this so it'll have to do for now. We simply take the current position and move
			 * down until we hit a row that would cause a collision.
             */
            Color colBase = tltType.getBaseColor();
            colBase = new Color(colBase.getRed(), colBase.getGreen(),
                    colBase.getBlue(), 20);
            for (int iLowest = iPieceRow; iLowest < iROW_COUNT; iLowest++) {
                //If no collision is detected, try the next row.
                if (isValidAndEmpty(tltType, iPieceCol, iLowest, iRotation)) {
                    continue;
                }

                //Draw the ghost one row higher than the one the collision took place at.
                iLowest--;

                //Draw the ghost piece.
                for (int iCol = 0; iCol < tltType.getDimension(); iCol++) {
                    for (int iRow = 0; iRow < tltType.getDimension(); iRow++) {
                        if (iLowest + iRow >= 2 && tltType.isTile(iCol, iRow, iRotation)) {
                            drawTile(colBase, colBase.brighter(), colBase.darker(), (iPieceCol + iCol) * iTILE_SIZE, (iLowest + iRow - iHIDDEN_ROW_COUNT) * iTILE_SIZE, g);
                        }
                    }
                }

                break;
            }

            /*
			 * Draw the background grid above the pieces (serves as a useful visual
			 * for players, and makes the pieces look nicer by breaking them up.
             */
            g.setColor(Color.DARK_GRAY);
            for (int iX = 0; iX < iCOL_COUNT; iX++) {
                for (int iY = 0; iY < iVISIBLE_ROW_COUNT; iY++) {
                    g.drawLine(0, iY * iTILE_SIZE, iCOL_COUNT * iTILE_SIZE, iY * iTILE_SIZE);
                    g.drawLine(iX * iTILE_SIZE, 0, iX * iTILE_SIZE, iVISIBLE_ROW_COUNT * iTILE_SIZE);
                }
            }
        }

        /*
		 * Draw the outline.
         */
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, iTILE_SIZE * iCOL_COUNT, iTILE_SIZE * iVISIBLE_ROW_COUNT);
    }

    /**
     * Draws a tile onto the board.
     *
     * @param type The type of tile to draw.
     * @param iX The column.
     * @param iY The row.
     * @param g The graphics object.
     */
    private void drawTile(TileType type, int iX, int iY, Graphics g) {
        drawTile(type.getBaseColor(), type.getLightColor(), type.getDarkColor(), iX, iY, g);
    }

    /**
     * Draws a tile onto the board.
     *
     * @param type The type of tile to draw.
     * @param iX The column.
     * @param iY The row.
     * @param g The graphics object.
     */
    private void drawTile2(TileType type, int iX, int iY, Graphics g) {
        drawTile(type.getBaseColor().brighter(), type.getLightColor().brighter(), type.getDarkColor().brighter(), iX, iY, g);
    }

    /**
     * Draws a tile onto the board.
     *
     * @param colBase The base color of tile.
     * @param colLight The light color of the tile.
     * @param colDark The dark color of the tile.
     * @param iX The column.
     * @param iY The row.
     * @param g The graphics object.
     */
    private void drawTile(Color colBase, Color colLight, Color colDark, int iX, int iY, Graphics g) {

        /*
		 * Fill the entire tile with the base color.
         */
        g.setColor(colBase);
        g.fillRect(iX, iY, iTILE_SIZE, iTILE_SIZE);

        /*
		 * Fill the bottom and right edges of the tile with the dark shading color.
         */
        g.setColor(colDark);
        g.fillRect(iX, iY + iTILE_SIZE - iSHADE_WIDTH, iTILE_SIZE, iSHADE_WIDTH);
        g.fillRect(iX + iTILE_SIZE - iSHADE_WIDTH, iY, iSHADE_WIDTH, iTILE_SIZE);

        /*
		 * Fill the top and left edges with the light shading. We draw a single line
		 * for each row or column rather than a rectangle so that we can draw a nice
		 * looking diagonal where the light and dark shading meet.
         */
        g.setColor(colLight);
        for (int i = 0; i < iSHADE_WIDTH; i++) {
            g.drawLine(iX, iY + i, iX + iTILE_SIZE - i - 1, iY + i);
            g.drawLine(iX + i, iY, iX + i, iY + iTILE_SIZE - i - 1);
        }
    }

    public int[][] getTablero() {
        int[][] iarrSalida = new int[tltTiles.length][tltTiles[0].length];
        for (int iC = 0; iC < tltTiles.length; iC++) {
            for (int iJ = 0; iJ < tltTiles[0].length; iJ++) {
                if (tltTiles[iC][iJ] != null) {
                    iarrSalida[iC][iJ] = tltTiles[iC][iJ].getType();
                } else {
                    iarrSalida[iC][iJ] = -1;
                }
            }
        }
        return iarrSalida;
    }

    public void setTablero(int[][] entrada) {

        tltTiles = new TileType[entrada.length][entrada[0].length];

        for (int iC = 0; iC < entrada.length; iC++) {
            for (int iJ = 0; iJ < entrada[0].length; iJ++) {
                if (entrada[iC][iJ] != -1) {
                    tltTiles[iC][iJ] = TileType.values()[entrada[iC][iJ]];
                } else {
                    tltTiles[iC][iJ] = null;
                }
            }
        }
    }
}
