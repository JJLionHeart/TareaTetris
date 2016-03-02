

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Random;
import java.io.RandomAccessFile;
import javax.swing.JFrame;
import java.awt.Color;

/**
 * The {@code Tetris} class is responsible for handling much of the game logic and
 * reading user input.
 * @author Brendan Jones
 *
 */
public class Tetris extends JFrame {
	
	/**
	 * The Serial Version UID.
	 */
	private static final long iSerialVersionUID = -4722429764792514382L;

	/**
	 * The number of milliseconds per frame.
	 */
	private static final long iFRAME_TIME = 1000L / 50L;
	
	/**
	 * The number of pieces that exist.
	 */
	private static final int iTYPE_COUNT = TileType.values().length;
		
	/**
	 * The BoardPanel instance.
	 */
	private BoardPanel bpnBoard;
	
	/**
	 * The SidePanel instance.
	 */
	private SidePanel spnSide;
	
	/**
	 * Whether or not the game is paused.
	 */
	private boolean boolIsPaused;
	
	/**
	 * Whether or not we've played a game yet. This is set to true
	 * initially and then set to false when the game starts.
	 */
	private boolean boolIsNewGame;
	
	/**
	 * Whether or not the game is over.
	 */
	private boolean boolIsGameOver;
	
	/**
	 * The current level we're on.
	 */
	private int iLevel;
	
	/**
	 * The current score.
	 */
	private int iScore;
	
	/**
	 * The random number generator. This is used to
	 * spit out pieces randomly.
	 */
	private Random ranRandom;
	
	/**
	 * The clock that handles the update logic.
	 */
	private Clock clkLogicTimer;
				
	/**
	 * The current type of tile.
	 */
	private TileType tltCurrentType;
	
	/**
	 * The next type of tile.
	 */
	private TileType tltNextType;
	
	/**
	 * The current column of our tile.
	 */
	private int iCurrentCol;
	
	/**
	 * The current row of our tile.
	 */
	private int iCurrentRow;
	
	/**
	 * The current rotation of our tile.
	 */
	private int iCurrentRotation;
		
	/**
	 * Ensures that a certain amount of time passes after a piece is
	 * spawned before we can drop it.
	 */
	private int iDropCooldown;
	
	/**
	 * The speed of the game.
	 */
	private float fGameSpeed;
        
        private SoundClip SClipFondo;  // Objeto SoundClip de fondo
        private SoundClip SClipPieza;  // Objeto SoundClip de las piezas
        
        private Color colColorReal;   //Guardo el color real de la pieza;
        
        private boolean bIluminar;    //Decido si la pieza se debe iluminar o no
		
	/**
	 * Creates a new Tetris instance. Sets up the window's properties,
	 * and adds a controller listener.
	 */
	private Tetris() {
		/*
		 * Set the basic properties of the window.
		 */
		super("Tetris");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		
		/*
		 * Initialize the BoardPanel and SidePanel instances.
		 */
		this.bpnBoard = new BoardPanel(this);
		this.spnSide = new SidePanel(this);
		
		/*
		 * Add the BoardPanel and SidePanel instances to the window.
		 */
		add(bpnBoard, BorderLayout.CENTER);
		add(spnSide, BorderLayout.EAST);
                
                /*
		 * Add the background sound. 
		 */
                SClipFondo = new SoundClip("Fondo.wav");
                SClipFondo.setLooping(true);
                SClipFondo.play();
		
                /*
		 * Add the sound for the pieces. 
		 */
                SClipPieza = new SoundClip("Pieza.wav");
                
                /*
                 * Inicializo el booleano que determina el brillo
                */
                bIluminar = false;
		/*
		 * Adds a custom anonymous KeyListener to the frame.
		 */
		addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent keyE) {
								
				switch(keyE.getKeyCode()) {
				
				/*
				 * Drop - When pressed, we check to see that the game is not
				 * paused and that there is no drop cooldown, then set the
				 * logic timer to run at a speed of 25 cycles per second.
				 */
				case KeyEvent.VK_S:
					if(!boolIsPaused && iDropCooldown == 0) {
						clkLogicTimer.setCyclesPerSecond(25.0f);
					}
					break;
					
				/*
				 * Move Left - When pressed, we check to see that the game is
				 * not paused and that the position to the left of the current
				 * position is valid. If so, we decrement the current column by 1.
				 */
				case KeyEvent.VK_A:
					if(!boolIsPaused && 
                                                bpnBoard.isValidAndEmpty(tltCurrentType, iCurrentCol - 1, iCurrentRow, iCurrentRotation)) {
						iCurrentCol--;
					}
					break;
					
				/*
				 * Move Right - When pressed, we check to see that the game is
				 * not paused and that the position to the right of the current
				 * position is valid. If so, we increment the current column by 1.
				 */
				case KeyEvent.VK_D:
					if(!boolIsPaused 
                                                && bpnBoard.isValidAndEmpty(
                                                        tltCurrentType, 
                                                        iCurrentCol + 1, 
                                                        iCurrentRow, 
                                                        iCurrentRotation)) {
						iCurrentCol++;
					}
					break;
					
				/*
				 * Rotate Anticlockwise - When pressed, check to see that the game is not paused
				 * and then attempt to rotate the piece anticlockwise. Because of the size and
				 * complexity of the rotation code, as well as it's similarity to clockwise
				 * rotation, the code for rotating the piece is handled in another method.
				 */
				case KeyEvent.VK_Q:
					if(!boolIsPaused) {
						rotatePiece(
                                                        (iCurrentRotation == 0) 
                                                                ? 3 :
                                                                iCurrentRotation 
                                                                        - 1);
					}
					break;
				
				/*
			     * Rotate Clockwise - When pressed, check to see that the game is not paused
				 * and then attempt to rotate the piece clockwise. Because of the size and
				 * complexity of the rotation code, as well as it's similarity to anticlockwise
				 * rotation, the code for rotating the piece is handled in another method.
				 */
				case KeyEvent.VK_E:
					if(!boolIsPaused) {
						rotatePiece((iCurrentRotation == 3) ? 0 : iCurrentRotation + 1);
					}
					break;
					
				/*
				 * Pause Game - When pressed, check to see that we're currently playing a game.
				 * If so, toggle the pause variable and update the logic timer to reflect this
				 * change, otherwise the game will execute a huge number of updates and essentially
				 * cause an instant game over when we unpause if we stay paused for more than a
				 * minute or so.
				 */
				case KeyEvent.VK_P:
					if(!boolIsGameOver && !boolIsNewGame) {
						boolIsPaused = !boolIsPaused;
						clkLogicTimer.setPaused(boolIsPaused);
					}
					break;
				
				/*
				 * Start Game - When pressed, check to see that we're in either a game over or new
				 * game state. If so, reset the game.
				 */
				case KeyEvent.VK_ENTER:
					if(boolIsGameOver || boolIsNewGame) {
						resetGame();
					}
					break;
                                case KeyEvent.VK_C:
                                    try{
                                    Cargar();//cargar el juego
                                    }catch(Exception e){
                                        System.out.println(e);
                                    }
                                    break;
				case KeyEvent.VK_G:
                                try{
                                    Guardar();//guardar el juego actual
                                    }catch(Exception e){
                                        System.out.println(e);
                                    }
                                    break;
				}
                                
                                    
			}
			
			@Override
			public void keyReleased(KeyEvent keyE) {
				
				switch(keyE.getKeyCode()) {
				
				/*
				 * Drop - When released, we set the speed of the logic timer
				 * back to whatever the current game speed is and clear out
				 * any cycles that might still be elapsed.
				 */
				case KeyEvent.VK_S:
					clkLogicTimer.setCyclesPerSecond(fGameSpeed);
					clkLogicTimer.reset();
					break;
				}
				
			}
			
		});
		
		/*
		 * Here we resize the frame to hold the BoardPanel and SidePanel instances,
		 * center the window on the screen, and show it to the user.
		 */
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/**
	 * Starts the game running. Initializes everything and enters the game loop.
	 */
	private void startGame() {
		/*
		 * Initialize our random number generator, logic timer, and new game variables.
		 */
		this.ranRandom = new Random();
		this.boolIsNewGame = true;
		this.fGameSpeed = 1.0f;
		
		/*
		 * Setup the timer to keep the game from running before the user presses enter
		 * to start it.
		 */
		this.clkLogicTimer = new Clock(fGameSpeed);
		clkLogicTimer.setPaused(true);
		
		while(true) {
			//Get the time that the frame started.
			long lnStart = System.nanoTime();
			
			//Update the logic timer.
			clkLogicTimer.update();
			
			/*
			 * If a cycle has elapsed on the timer, we can update the game and
			 * move our current piece down.
			 */
			if(clkLogicTimer.hasElapsedCycle()) {
				updateGame();
			}
		
			//Decrement the drop cool down if necessary.
			if(iDropCooldown > 0) {
				iDropCooldown--;
			}
			
			//Display the window to the user.
			renderGame();
			
			/*
			 * Sleep to cap the framerate.
			 */
			long lnDelta = (System.nanoTime() - lnStart) / 1000000L;
			if(lnDelta < iFRAME_TIME) {
				try {
					Thread.sleep(iFRAME_TIME - lnDelta);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Updates the game and handles the bulk of it's logic.
	 */
	private void updateGame() {
		/*
		 * Check to see if the piece's position can move down to the next row.
		 */
		if(bpnBoard.isValidAndEmpty(tltCurrentType, iCurrentCol, iCurrentRow + 1, iCurrentRotation)) {
			//Increment the current row if it's safe to do so.
			iCurrentRow++;
                        /*
                        //decido si se debe iluminar.
                        if(bIluminar)
                        {
                            tltCurrentType.Iluminar(colColorReal);
                            bIluminar = false;
                        }
                        else
                        {
                            tltCurrentType.setColor(colColorReal);
                            bIluminar = true;
                        }
                        */
		} else {
			/*
			 * We've either reached the bottom of the board, or landed on another piece, so
			 * we need to add the piece to the board.
			 */
			bpnBoard.addPiece(tltCurrentType, iCurrentCol, iCurrentRow, iCurrentRotation);
			
			/*
			 * Check to see if adding the new piece resulted in any cleared lines. If so,
			 * increase the player's score. (Up to 4 lines can be cleared in a single go;
			 * [1 = 100pts, 2 = 200pts, 3 = 400pts, 4 = 800pts]).
			 */
			int iCleared = bpnBoard.checkLines();
			if(iCleared > 0) {
				iScore += 50 << iCleared;
			}
                        
			/*
			 * Increase the speed slightly for the next piece and update the game's timer
			 * to reflect the increase.
			 */
			fGameSpeed += 0.035f;
			clkLogicTimer.setCyclesPerSecond(fGameSpeed);
			clkLogicTimer.reset();
			
			/*
			 * Set the drop cooldown so the next piece doesn't automatically come flying
			 * in from the heavens immediately after this piece hits if we've not reacted
			 * yet. (~0.5 second buffer).
			 */
			iDropCooldown = 25;
			
			/*
			 * Update the difficulty level. This has no effect on the game, and is only
			 * used in the "Level" string in the SidePanel.
			 */
			iLevel = (int)(fGameSpeed * 1.70f);
			
			/*
			 * Spawn a new piece to control.
			 */
			spawnPiece();
                        
			/*
			 * Plays the sound when it hits anothe piece or the bottom.
			 */
                        SClipPieza.play();
		}		
	}
	
	/**
	 * Forces the BoardPanel and SidePanel to repaint.
	 */
	private void renderGame() {
		bpnBoard.repaint();
		spnSide.repaint();
	}
	
	/**
	 * Resets the game variables to their default values at the start
	 * of a new game.
	 */
	private void resetGame() {
		this.iLevel = 1;
		this.iScore = 0;
		this.fGameSpeed = 1.0f;
		this.tltNextType = TileType.values()
                        [ranRandom.nextInt(iTYPE_COUNT)];
		this.boolIsNewGame = false;
		this.boolIsGameOver = false;		
		bpnBoard.clear();
		clkLogicTimer.reset();
		clkLogicTimer.setCyclesPerSecond(fGameSpeed);
		spawnPiece();
	}
		
	/**
	 * Spawns a new piece and resets our piece's variables to their default
	 * values.
	 */
	private void spawnPiece() {
		/*
		 * Poll the last piece and reset our position and rotation to
		 * their default variables, then pick the next piece to use.
		 */
		this.tltCurrentType = tltNextType;
		this.iCurrentCol = tltCurrentType.getSpawnColumn();
		this.iCurrentRow = tltCurrentType.getSpawnRow();
		this.iCurrentRotation = 0;
		this.tltNextType = TileType.values()
                        [ranRandom.nextInt(iTYPE_COUNT)];
                
                //guardo el color real de la pieza.
                colColorReal = tltCurrentType.getBaseColor();
		
		/*
		 * If the spawn point is invalid, we need to pause the game and flag that we've lost
		 * because it means that the pieces on the bpnBoard have gotten too high.
		 */
		if(!bpnBoard.isValidAndEmpty(tltCurrentType, iCurrentCol
                        , iCurrentRow, iCurrentRotation)) {
			this.boolIsGameOver = true;
			clkLogicTimer.setPaused(true);
		}		
	}

	/**
	 * Attempts to set the rotation of the current piece to newRotation.
	 * @param newRotation The rotation of the new peice.
	 */
	private void rotatePiece(int iNewRotation) {
		/*
		 * Sometimes pieces will need to be moved when rotated to avoid clipping
		 * out of the board (the I piece is a good example of this). Here we store
		 * a temporary row and column in case we need to move the tile as well.
		 */
		int iNewColumn = iCurrentCol;
		int iNewRow = iCurrentRow;
		
		/*
		 * Get the insets for each of the sides. These are used to determine how
		 * many empty rows or columns there are on a given side.
		 */
		int iLeft = tltCurrentType.getLeftInset(iNewRotation);
		int iRight = tltCurrentType.getRightInset(iNewRotation);
		int iTop = tltCurrentType.getTopInset(iNewRotation);
		int iBottom = tltCurrentType.getBottomInset(iNewRotation);
		
		/*
		 * If the current piece is too far to the left or right, move the piece away from the edges
		 * so that the piece doesn't clip out of the map and automatically become invalid.
		 */
		if(iCurrentCol < -iLeft) {
			iNewColumn -= iCurrentCol - iLeft;
		} else if(iCurrentCol + tltCurrentType.getDimension() 
                        - iRight >= BoardPanel.iCOL_COUNT) {
			iNewColumn -= (iCurrentCol + 
                                tltCurrentType.getDimension() - iRight) 
                                - BoardPanel.iCOL_COUNT + 1;
		}
		
		/*
		 * If the current piece is too far to the top or bottom, move the piece away from the edges
		 * so that the piece doesn't clip out of the map and automatically become invalid.
		 */
		if(iCurrentRow < -iTop) {
			iNewRow -= iCurrentRow - iTop;
		} else if(iCurrentRow + tltCurrentType.getDimension() - iBottom >= BoardPanel.iROW_COUNT) {
			iNewRow -= (iCurrentRow + tltCurrentType.getDimension() - iBottom) - BoardPanel.iROW_COUNT + 1;
		}
		
		/*
		 * Check to see if the new position is acceptable. If it is, update the rotation and
		 * position of the piece.
		 */
		if(bpnBoard.isValidAndEmpty(tltCurrentType, iNewColumn, iNewRow, iNewRotation)) {
			iCurrentRotation = iNewRotation;
			iCurrentRow = iNewRow;
			iCurrentCol = iNewColumn;
		}
	}
	
	/**
	 * Checks to see whether or not the game is paused.
	 * @return Whether or not the game is paused.
	 */
	public boolean isPaused() {
		return boolIsPaused;
	}
	
	/**
	 * Checks to see whether or not the game is over.
	 * @return Whether or not the game is over.
	 */
	public boolean isGameOver() {
		return boolIsGameOver;
	}
	
	/**
	 * Checks to see whether or not we're on a new game.
	 * @return Whether or not this is a new game.
	 */
	public boolean isNewGame() {
		return boolIsNewGame;
	}
	
	/**
	 * Gets the current score.
	 * @return The score.
	 */
	public int getScore() {
		return iScore;
	}
	
	/**
	 * Gets the current level.
	 * @return The level.
	 */
	public int getLevel() {
		return iLevel;
	}
	
	/**
	 * Gets the current type of piece we're using.
	 * @return The piece type.
	 */
	public TileType getPieceType() {
		return tltCurrentType;
	}
	
	/**
	 * Gets the next type of piece we're using.
	 * @return The next piece.
	 */
	public TileType getNextPieceType() {
		return tltNextType;
	}
	
	/**
	 * Gets the column of the current piece.
	 * @return The column.
	 */
	public int getPieceCol() {
		return iCurrentCol;
	}
	
	/**
	 * Gets the row of the current piece.
	 * @return The row.
	 */
	public int getPieceRow() {
		return iCurrentRow;
	}
	
	/**
	 * Gets the rotation of the current piece.
	 * @return The rotation.
	 */
	public int getPieceRotation() {
		return iCurrentRotation;
	}
        /**
         * funcion de guardar, toma el estado actual del juego
         * y lo transfiere a un archivo de acceso aleatorio
         * @throws IOException 
         */
        public void Guardar()throws IOException{
            RandomAccessFile rafSalida;
            rafSalida = new RandomAccessFile("guardado.dat","rw");
            rafSalida.writeInt(this.iLevel);
            rafSalida.writeInt(this.iScore);
            rafSalida.writeFloat(this.fGameSpeed);
            rafSalida.writeInt(this.tltNextType.getType());
            rafSalida.writeInt(this.tltCurrentType.getType());
            rafSalida.writeBoolean(this.boolIsNewGame);
            rafSalida.writeBoolean(this.boolIsGameOver);
            int matDatos[][] = bpnBoard.getTablero();
            
            rafSalida.writeInt(matDatos.length);
            rafSalida.writeInt(matDatos[0].length);
            for(int iC=0;iC < matDatos.length;iC++){
                for(int iJ = 0;iJ<matDatos[0].length;iJ++){
                    rafSalida.writeInt(matDatos[iC][iJ]);
                }
            }
        }
        /**
         * Funcion de Cargar el juego, toma el estado guardado en el archivo
         * de acceso aleatorio y lo sustituye en el juego actual
         * @throws IOException 
         */
        public void Cargar()throws IOException{
            RandomAccessFile rafEntrada;
            rafEntrada = new RandomAccessFile("guardado.dat","rw");
            this.iLevel = rafEntrada.readInt();
            this.iScore = rafEntrada.readInt();
            this.fGameSpeed = rafEntrada.readFloat();
            this.tltNextType = TileType.values()[rafEntrada.readInt()];
            this.tltCurrentType = TileType.values()[rafEntrada.readInt()];
            this.boolIsNewGame = rafEntrada.readBoolean();
            this.boolIsGameOver = rafEntrada.readBoolean();
            
            clkLogicTimer.reset();
	    clkLogicTimer.setCyclesPerSecond(fGameSpeed);
            int iRows = rafEntrada.readInt();
            int iCols = rafEntrada.readInt();
            int matTablero[][] = new int[iRows][iCols];
            for(int iC = 0;iC<iRows;iC++){
                for(int iJ = 0;iJ<iCols;iJ++){
                    matTablero[iC][iJ] = rafEntrada.readInt();
                }
            }
            bpnBoard.clear();
            bpnBoard.setTablero(matTablero);
        }
	/**
	 * Entry-point of the game. Responsible for creating and starting a new
	 * game instance.
	 * @param args Unused.
	 */
	public static void main(String[] args) {
		Tetris tetTetris = new Tetris();
		tetTetris.startGame();
	}
        

}
