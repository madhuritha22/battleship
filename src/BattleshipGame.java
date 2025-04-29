// BattleshipGame.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class BattleshipGame extends JFrame {
    private static final int GRID_SIZE = 10;
    private static final int CELL_SIZE = 40;
    private static final Color WATER_COLOR = new Color(100, 149, 237);
    private static final Color SHIP_COLOR = Color.DARK_GRAY;
    private static final Color HIT_COLOR = Color.RED;
    private static final Color MISS_COLOR = Color.WHITE;
    
    private Cell[][] playerGrid = new Cell[GRID_SIZE][GRID_SIZE];
    private Cell[][] computerGrid = new Cell[GRID_SIZE][GRID_SIZE];
    
    private JPanel playerPanel;
    private JPanel computerPanel;
    private JLabel statusLabel;
    
    private boolean gameOver = false;
    private boolean playerTurn = true;
    private Random random = new Random();
    
    // Ship sizes
    private static final int[] SHIP_SIZES = {5, 4, 3, 3, 2}; // Carrier, Battleship, Cruiser, Submarine, Destroyer
    private static final String[] SHIP_NAMES = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};
    
    public BattleshipGame() {
        setTitle("Battleship Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Initialize game panels
        JPanel gamePanel = new JPanel(new GridLayout(1, 2, 20, 0));
        
        // Player's grid
        playerPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        playerPanel.setBorder(BorderFactory.createTitledBorder("Your Fleet"));
        initializeGrid(playerPanel, playerGrid, false);
        
        // Computer's grid
        computerPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        computerPanel.setBorder(BorderFactory.createTitledBorder("Enemy Waters"));
        initializeGrid(computerPanel, computerGrid, true);
        
        gamePanel.add(playerPanel);
        gamePanel.add(computerPanel);
        
        // Status panel
        JPanel statusPanel = new JPanel();
        statusLabel = new JLabel("Place your ships!");
        statusPanel.add(statusLabel);
        
        // Add the panels to the frame
        add(gamePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        // Place computer ships
        placeComputerShips();
        
        // Place player ships (after initialization for visualization)
        SwingUtilities.invokeLater(this::placePlayerShips);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initializeGrid(JPanel panel, Cell[][] grid, boolean isComputer) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                final int row = i;
                final int col = j;
                
                grid[i][j] = new Cell();
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                button.setBackground(WATER_COLOR);
                button.setOpaque(true);
                button.setBorderPainted(true);
                
                if (isComputer) {
                    button.addActionListener(e -> {
                        if (playerTurn && !gameOver && !grid[row][col].isShot()) {
                            fireShot(row, col, grid, button);
                            playerTurn = false;
                            
                            // Check if game is over
                            if (!gameOver) {
                                statusLabel.setText("Computer's turn...");
                                
                                // Let computer take its turn after a short delay
                                Timer timer = new Timer(1000, event -> {
                                    computerTurn();
                                    playerTurn = true;
                                    
                                    if (!gameOver) {
                                        statusLabel.setText("Your turn! Select a target.");
                                    }
                                });
                                timer.setRepeats(false);
                                timer.start();
                            }
                        }
                    });
                }
                
                grid[i][j].setButton(button);
                panel.add(button);
            }
        }
    }
    
    private void placePlayerShips() {
        for (int i = 0; i < SHIP_SIZES.length; i++) {
            placeShipRandomly(playerGrid, SHIP_SIZES[i], true);
        }
        updateGridDisplay();
        statusLabel.setText("Your turn! Select a target.");
    }
    
    private void placeComputerShips() {
        for (int i = 0; i < SHIP_SIZES.length; i++) {
            placeShipRandomly(computerGrid, SHIP_SIZES[i], false);
        }
    }
    
    private boolean placeShipRandomly(Cell[][] grid, int shipSize, boolean isVisible) {
        boolean isHorizontal = random.nextBoolean();
        int maxRow = isHorizontal ? GRID_SIZE : GRID_SIZE - shipSize;
        int maxCol = isHorizontal ? GRID_SIZE - shipSize : GRID_SIZE;
        
        // Try 100 times to find a valid placement
        for (int attempt = 0; attempt < 100; attempt++) {
            int row = random.nextInt(maxRow);
            int col = random.nextInt(maxCol);
            
            if (isValidPlacement(grid, row, col, shipSize, isHorizontal)) {
                // Place the ship
                for (int i = 0; i < shipSize; i++) {
                    int r = isHorizontal ? row : row + i;
                    int c = isHorizontal ? col + i : col;
                    grid[r][c].setShip(true);
                    
                    if (isVisible) {
                        grid[r][c].getButton().setBackground(SHIP_COLOR);
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    private boolean isValidPlacement(Cell[][] grid, int startRow, int startCol, int shipSize, boolean isHorizontal) {
        // Check if ship fits
        for (int i = 0; i < shipSize; i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;
            
            // Check if out of bounds
            if (row >= GRID_SIZE || col >= GRID_SIZE) {
                return false;
            }
            
            // Check if cell is already occupied
            if (grid[row][col].hasShip()) {
                return false;
            }
            
            // Check adjacent cells (diagonals included)
            for (int r = Math.max(0, row - 1); r <= Math.min(GRID_SIZE - 1, row + 1); r++) {
                for (int c = Math.max(0, col - 1); c <= Math.min(GRID_SIZE - 1, col + 1); c++) {
                    if (grid[r][c].hasShip()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private void updateGridDisplay() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                // Update player grid
                if (playerGrid[i][j].isShot()) {
                    if (playerGrid[i][j].hasShip()) {
                        playerGrid[i][j].getButton().setBackground(HIT_COLOR);
                    } else {
                        playerGrid[i][j].getButton().setBackground(MISS_COLOR);
                    }
                } else if (playerGrid[i][j].hasShip()) {
                    playerGrid[i][j].getButton().setBackground(SHIP_COLOR);
                } else {
                    playerGrid[i][j].getButton().setBackground(WATER_COLOR);
                }
                
                // Update computer grid - only show hits and misses
                if (computerGrid[i][j].isShot()) {
                    if (computerGrid[i][j].hasShip()) {
                        computerGrid[i][j].getButton().setBackground(HIT_COLOR);
                    } else {
                        computerGrid[i][j].getButton().setBackground(MISS_COLOR);
                    }
                } else {
                    computerGrid[i][j].getButton().setBackground(WATER_COLOR);
                }
            }
        }
    }
    
    private void fireShot(int row, int col, Cell[][] grid, JButton button) {
        grid[row][col].setShot(true);
        
        if (grid[row][col].hasShip()) {
            button.setBackground(HIT_COLOR);
            statusLabel.setText("HIT!");
        } else {
            button.setBackground(MISS_COLOR);
            statusLabel.setText("MISS!");
        }
        
        checkGameOver();
    }
    
    private void computerTurn() {
        if (gameOver) return;
        
        int row, col;
        
        // Try to hit adjacent to a previous hit
        boolean foundTarget = false;
        for (int i = 0; i < GRID_SIZE && !foundTarget; i++) {
            for (int j = 0; j < GRID_SIZE && !foundTarget; j++) {
                if (playerGrid[i][j].isShot() && playerGrid[i][j].hasShip()) {
                    // Check adjacent cells
                    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                    for (int[] dir : directions) {
                        int newRow = i + dir[0];
                        int newCol = j + dir[1];
                        
                        if (newRow >= 0 && newRow < GRID_SIZE && newCol >= 0 && newCol < GRID_SIZE && 
                            !playerGrid[newRow][newCol].isShot()) {
                            row = newRow;
                            col = newCol;
                            fireComputerShot(row, col);
                            foundTarget = true;
                            break;
                        }
                    }
                }
            }
        }
        
        // If no target found, shoot randomly
        if (!foundTarget) {
            do {
                row = random.nextInt(GRID_SIZE);
                col = random.nextInt(GRID_SIZE);
            } while (playerGrid[row][col].isShot());
            
            fireComputerShot(row, col);
        }
    }
    
    private void fireComputerShot(int row, int col) {
        playerGrid[row][col].setShot(true);
        
        if (playerGrid[row][col].hasShip()) {
            statusLabel.setText("Computer HIT your ship at " + (char)('A' + col) + (row + 1) + "!");
        } else {
            statusLabel.setText("Computer MISSED at " + (char)('A' + col) + (row + 1) + ".");
        }
        
        updateGridDisplay();
        checkGameOver();
    }
    
    private void checkGameOver() {
        boolean playerLost = true;
        boolean computerLost = true;
        
        // Check if player has any ships left
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (playerGrid[i][j].hasShip() && !playerGrid[i][j].isShot()) {
                    playerLost = false;
                }
                if (computerGrid[i][j].hasShip() && !computerGrid[i][j].isShot()) {
                    computerLost = false;
                }
            }
        }
        
        if (playerLost) {
            gameOver = true;
            statusLabel.setText("Game Over! Computer won!");
            revealComputerShips();
        } else if (computerLost) {
            gameOver = true;
            statusLabel.setText("Game Over! You won!");
            revealComputerShips();
        }
    }
    
    private void revealComputerShips() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (computerGrid[i][j].hasShip() && !computerGrid[i][j].isShot()) {
                    computerGrid[i][j].getButton().setBackground(new Color(50, 50, 50, 150)); // Gray transparent
                }
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new BattleshipGame());
    }
    
    // Cell class to represent grid cells
    private static class Cell {
        private boolean hasShip = false;
        private boolean isShot = false;
        private JButton button;
        
        public boolean hasShip() {
            return hasShip;
        }
        
        public void setShip(boolean hasShip) {
            this.hasShip = hasShip;
        }
        
        public boolean isShot() {
            return isShot;
        }
        
        public void setShot(boolean isShot) {
            this.isShot = isShot;
        }
        
        public JButton getButton() {
            return button;
        }
        
        public void setButton(JButton button) {
            this.button = button;
        }
    }
}