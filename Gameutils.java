import java.util.Random;

public class Gameutils {
	   public static Random random = new Random();
	   private static final int GRID_SIZE = 10;
	   
	   private static final int[] SHIP_SIZES = {5, 4, 3, 3, 2}; // Carrier, Battleship, Cruiser, Submarine, Destroyer
	   
	   public static void initializeComputerGrid(Cell[][] computerGrid) {
	        // Initialize computer grid
	        for (int r = 0; r < GRID_SIZE; r++) {
	            for (int c = 0; c < GRID_SIZE; c++) {
	                computerGrid[r][c] = new Cell(r, c);
	            }
	        }
	        
	        // Place computer ships randomly
	        for (int shipType = 0; shipType < SHIP_SIZES.length; shipType++) {
	            boolean shipPlaced = false;
	            
	            while (!shipPlaced) {
	                // Randomize position and orientation
	                int row = random.nextInt(GRID_SIZE);
	                int col = random.nextInt(GRID_SIZE);
	                boolean horizontal = random.nextBoolean();
	                
	                // Check if valid position
	                if (isValidComputerShipPlacement(row, col, SHIP_SIZES[shipType], horizontal,computerGrid)) {
	                    // Place ship
	                    placeComputerShip(row, col, shipType, SHIP_SIZES[shipType], horizontal,computerGrid);
	                    shipPlaced = true;
	                }
	            }
	        }
	    }
	    
	   public static boolean isValidComputerShipPlacement(int row, int col, int size, boolean horizontal,Cell[][] grid) {
	        // Check if the ship fits on the board
	        if (horizontal) {
	            if (col + size > GRID_SIZE) {
	                return false;
	            }
	        } else {
	            if (row + size > GRID_SIZE) {
	                return false;
	            }
	        }
	        
	        // Check if any part of the ship would overlap with another ship
	        for (int i = 0; i < size; i++) {
	            int r = horizontal ? row : row + i;
	            int c = horizontal ? col + i : col;
	            
	            if (grid[r][c].hasShip() || 
	                cellAdjacentToShip(r, c, grid, true)) {
	                return false;
	            }
	        }
	        
	        return true;
	    }
	    
	   public static void placeComputerShip(int row, int col, int shipType, int size, boolean horizontal,Cell[][] computerGrid) {
	        for (int i = 0; i < size; i++) {
	            int r = horizontal ? row : row + i;
	            int c = horizontal ? col + i : col;
	            
	            computerGrid[r][c].setShip(true);
	            computerGrid[r][c].setShipType(shipType);
	            computerGrid[r][c].setShipPart(i);
	            computerGrid[r][c].setHorizontalPlacement(horizontal);
	        }
	    }
	   
	   public static boolean cellAdjacentToShip(int row, int col, Cell[][] grid, boolean includeDiagonals) {
	        // Check adjacent cells for ships (used to prevent ships from touching)
	        for (int r = Math.max(0, row - 1); r <= Math.min(GRID_SIZE - 1, row + 1); r++) {
	            for (int c = Math.max(0, col - 1); c <= Math.min(GRID_SIZE - 1, col + 1); c++) {
	                // Skip the current cell
	                if (r == row && c == col) continue;
	                
	                // Skip diagonals if not including them
	                if (!includeDiagonals && r != row && c != col) continue;
	                
	                // Check if this cell has a ship
	                if (grid[r][c].hasShip()) {
	                    return true;
	                }
	            }
	        }
	        return false;
	    }
}
