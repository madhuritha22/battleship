import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.BiConsumer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class GridManager {
	
	   private static final int GRID_SIZE = 10;
	   private static final int CELL_SIZE = 40;
	   
	   private static final String[] COLUMN_LABELS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
	   private static final String[] ROW_LABELS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
	   
	   public static Image[] explosionFrames;
	   public static Image[] splashFrames;
	    
	   
	   public int[] shipSizes;
	   Image[] shipImages;
	   Image[] shipImagesVertical;
	   private static final Color WATER_COLOR = new Color(65, 105, 225);
	   private static final Color SHIP_COLOR = new Color(70, 70, 70);
	   private static final Color HIT_COLOR = Color.RED;
	   private static final Color MISS_COLOR = Color.WHITE;
	   private static final Color PLACEMENT_PREVIEW_COLOR = new Color(50, 205, 50, 150); // Green with transparency
	   private static final Color INVALID_PLACEMENT_COLOR = new Color(255, 0, 0, 150); // Red with transparency
	   
	   public static int waveOffset = 0;
	   
	   private static final Color[] SHIP_COLORS = {
		        new Color(40, 40, 40),  // Carrier - darker gray
		        new Color(50, 50, 50),  // Battleship - dark gray
		        new Color(60, 60, 60),  // Cruiser - medium gray
		        new Color(70, 70, 70),  // Submarine - light gray
		        new Color(80, 80, 80)   // Destroyer - lightest gray
		    };
		    
	   public static void initializePlayerGrid(JPanel playerPlacementPanel,
			    Cell[][] playerGrid,
			    int gridSize,
			    int[] shipSizes,
			    int currentShipIndex,
			    boolean isHorizontal,
			    Image[] shipImages,
			    Image[] shipImagesVertical,
			    MouseListener listener,
			    Runnable updatePlacementStatusCallback,
			    BiConsumer<Integer, Integer> previewShipPlacementCallback,
			    BiConsumer<Integer, Integer> placeShipCallback) {
		  
	        // Create cells for player's grid
	        playerPlacementPanel.removeAll();
	        
	        for (int row = 0; row < gridSize; row++) {
	            for (int col = 0; col < gridSize; col++) {
	                playerGrid[row][col] = new Cell(row, col);
	                
	                JPanel cellPanel = createCellPanel( playerGrid[row][col], true,
	                	    shipSizes, shipImages, shipImagesVertical, explosionFrames, splashFrames);
	                
	                // Add mouse listener for ship placement
	                final int finalRow = row;
	                final int finalCol = col;
	                
	                cellPanel.addMouseListener(new MouseAdapter() {
	                    @Override
	                    public void mouseEntered(MouseEvent e) {
	                        if (currentShipIndex < shipSizes.length) {
	                            previewShipPlacementCallback.accept(finalRow, finalCol);
	                        }
	                    }
	                    
	                    @Override
	                    public void mouseExited(MouseEvent e) {
	                        GridManager.clearPlacementPreview(playerGrid, playerPlacementPanel, GRID_SIZE);
	                    }
	                    
	                    @Override
	                    public void mouseClicked(MouseEvent e) {
	                        if (currentShipIndex < shipSizes.length) {
	                            placeShipCallback.accept(finalRow, finalCol);
	                        }
	                    }
	                });
	                
	                playerPlacementPanel.add(cellPanel);
	            }
	        }
	        
	        playerPlacementPanel.revalidate();
	        playerPlacementPanel.repaint();
	        
	        // Update status
	        updatePlacementStatusCallback.run();
	    }
	   
	   public static JPanel createCellPanel( Cell cell,
			    boolean isPlayer,
			    int[] shipSizes,
			    Image[] shipImages,
			    Image[] shipImagesVertical,
			    Image[] explosionFrames,
			    Image[] splashFrames) {
		   
	        final Cell finalCell = cell;
	        
	        JPanel cellPanel = new JPanel() {
	            @Override
	            protected void paintComponent(Graphics g) {
	                super.paintComponent(g);
	                
	                Graphics2D g2d = (Graphics2D) g;
	                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	                
	                // Draw cell background based on state
	                if (finalCell.isHit() && finalCell.hasShip()) {
	                    // Show hit ship
	                    g2d.setColor(HIT_COLOR);
	                    g2d.fillRect(0, 0, getWidth(), getHeight());
	                } else if (finalCell.isHit() && !finalCell.hasShip()) {
	                    // Show miss
	                    g2d.setColor(MISS_COLOR);
	                    g2d.fillOval(getWidth()/4, getHeight()/4, getWidth()/2, getHeight()/2);
	                } else if (isPlayer && finalCell.hasShip()) {
	                    // Show player's ship
	                    if (finalCell.getShipType() >= 0 && finalCell.getShipType() < SHIP_COLORS.length) {
	                        g2d.setColor(SHIP_COLORS[finalCell.getShipType()]);
	                    } else {
	                        g2d.setColor(SHIP_COLOR);
	                    }
	                    g2d.fillRect(0, 0, getWidth(), getHeight());
	                    
	                    // Draw ship texture/image if available
	                    if (finalCell.getShipType() >= 0 && 
	                        finalCell.getShipType() < shipImages.length && 
	                        finalCell.getShipPart() >= 0) {
	                        
	                        Image shipImage;
	                        if (finalCell.isHorizontalPlacement()) {
	                            shipImage = shipImages[finalCell.getShipType()];
	                        } else {
	                            shipImage = shipImagesVertical[finalCell.getShipType()];
	                        }
	                        
	                        // Only draw if we have the image
	                        if (shipImage != null) {
	                            int shipSize = shipSizes[finalCell.getShipType()];
	                            
	                            if (finalCell.isHorizontalPlacement()) {
	                                // Calculate source rectangle to draw just this part of the ship
	                                int partWidth = shipImage.getWidth(null) / shipSize;
	                                int sx1 = finalCell.getShipPart() * partWidth;
	                                int sy1 = 0;
	                                int sx2 = sx1 + partWidth;
	                                int sy2 = shipImage.getHeight(null);
	                                
	                                g2d.drawImage(shipImage, 
	                                              0, 0, getWidth(), getHeight(),
	                                              sx1, sy1, sx2, sy2, null);
	                            } else {
	                                // For vertical ship
	                                int partHeight = shipImage.getHeight(null) / shipSize;
	                                int sx1 = 0;
	                                int sy1 = finalCell.getShipPart() * partHeight;
	                                int sx2 = shipImage.getWidth(null);
	                                int sy2 = sy1 + partHeight;
	                                
	                                g2d.drawImage(shipImage, 
	                                              0, 0, getWidth(), getHeight(),
	                                              sx1, sy1, sx2, sy2, null);
	                            }
	                        }
	                    }
	                } else if (finalCell.isPreview()) {
	                    // Show placement preview
	                    g2d.setColor(finalCell.isValidPlacement() ? 
	                                 PLACEMENT_PREVIEW_COLOR : INVALID_PLACEMENT_COLOR);
	                    g2d.fillRect(0, 0, getWidth(), getHeight());
	                } else {
	                    // Water
	                    g2d.setColor(WATER_COLOR);
	                    g2d.fillRect(0, 0, getWidth(), getHeight());
	                    
	                    // Add water texture/waves effect
	                    int wavePhase = (finalCell.getRow() + finalCell.getCol() + waveOffset) % 10;
	                    g2d.setColor(new Color(100, 140, 255, 50 + wavePhase * 5));
	                    g2d.fillOval(-10 + wavePhase, -10 + wavePhase, 
	                                getWidth() + 20 - wavePhase * 2, 
	                                getHeight() + 20 - wavePhase * 2);
	                }
	                
	                // Draw cell border
	                g2d.setColor(new Color(0, 0, 50));
	                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	                
	                // Draw hit/miss animation
	                if (finalCell.isAnimating()) {
	                    if (finalCell.hasShip() && finalCell.isHit()) {
	                        // Draw explosion animation
	                        if (explosionFrames != null && explosionFrames.length > 0) {
	                            int frameIndex = finalCell.getAnimationFrame() % explosionFrames.length;
	                            g2d.drawImage(explosionFrames[frameIndex], 0, 0, getWidth(), getHeight(), null);
	                        }
	                    } else if (!finalCell.hasShip() && finalCell.isHit()) {
	                        // Draw splash animation
	                        if (splashFrames != null && splashFrames.length > 0) {
	                            int frameIndex = finalCell.getAnimationFrame() % splashFrames.length;
	                            g2d.drawImage(splashFrames[frameIndex], 0, 0, getWidth(), getHeight(), null);
	                        }
	                    }
	                }
	                
	                // Draw coordinates on the edge cells (first row and column)
	                if (isPlayer) {
	                    g2d.setColor(Color.WHITE);
	                    g2d.setFont(new Font("Arial", Font.BOLD, 10));
	                    
	                    // Draw column letters (A, B, C, etc.)
	                    if (finalCell.getRow() == 0) {
	                        g2d.drawString(COLUMN_LABELS[finalCell.getCol()], getWidth()/2 - 4, 12);
	                    }
	                    
	                    // Draw row numbers (1, 2, 3, etc.)
	                    if (finalCell.getCol() == 0) {
	                        g2d.drawString(ROW_LABELS[finalCell.getRow()], 4, getHeight()/2 + 4);
	                    }
	                }
	            }
	        };
	        
	        cellPanel.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
	        cellPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        
	        return cellPanel;
	    }
	    
	   public static void clearPlacementPreview(Cell[][] playerGrid, JPanel panel, int gridSize) {
	        for (int r = 0; r < GRID_SIZE; r++) {
	            for (int c = 0; c < GRID_SIZE; c++) {
	                if (playerGrid[r][c] != null) {
	                    playerGrid[r][c].setPreview(false);
	                }
	            }
	        }
	        panel.repaint();
	    }
}
