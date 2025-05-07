// BattleshipGame.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BattleshipGame extends JFrame {
    private static final int GRID_SIZE = 10;
    private static final int CELL_SIZE = 40;
 
    // Animation-related constants
    private static final int ANIMATION_FRAMES = 10;
    private static final int ANIMATION_DELAY = 50;
    
    // Game screens
    private static final String START_SCREEN = "startScreen";
    private static final String PLACEMENT_SCREEN = "placementScreen";
    private static final String BATTLE_SCREEN = "battleScreen";
    private static final String GAME_OVER_SCREEN = "gameOverScreen";
    
    // Game state
    private Cell[][] playerGrid = new Cell[GRID_SIZE][GRID_SIZE];
    private Cell[][] computerGrid = new Cell[GRID_SIZE][GRID_SIZE];
    private boolean gameInProgress = false;
    private boolean placementPhase = true;
    private boolean playerTurn = true;
    private boolean isHorizontal = true;
    private boolean playerWon = false;
    public static Random random = new Random();
   
    
    // Main layout components
    private CardLayout mainLayout;
    private JPanel mainPanel;
    private JPanel startPanel;
    private JPanel placementPanel;
    private JPanel battlePanel;
    private JPanel gameOverPanel;
    
    // Start screen components
    private JLabel titleLabel;
    private JButton newGameButton;
    private JButton exitButton;
    private JPanel logoPanel;
    
    // Placement screen components
    private JPanel playerPlacementPanel;
    private JPanel placementControlPanel;
    private JLabel placementStatusLabel;
    private JButton rotatePlacementButton;
    private JButton resetPlacementButton;
    private JButton startBattleButton;
    
    // Battle screen components
    private JPanel playerBattlePanel;
    private JPanel computerPanel;
    private JLabel battleStatusLabel;
    private JButton surrenderButton;
    
    // Game over screen components
    private JLabel gameResultLabel;
    private JButton playAgainButton;
    private JButton exitToMenuButton;
    
    // Ship placement state
    private int currentShipIndex = 0;
    
    // Ship sizes
    private static final int[] SHIP_SIZES = {5, 4, 3, 3, 2}; // Carrier, Battleship, Cruiser, Submarine, Destroyer
    private static final String[] SHIP_NAMES = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};
    private static final Color[] SHIP_COLORS = {
        new Color(40, 40, 40),  // Carrier - darker gray
        new Color(50, 50, 50),  // Battleship - dark gray
        new Color(60, 60, 60),  // Cruiser - medium gray
        new Color(70, 70, 70),  // Submarine - light gray
        new Color(80, 80, 80)   // Destroyer - lightest gray
    };
    
    // Ocean wave animation
   
    private Timer waveTimer;
    
    private int waveOffset = 0;
    
    // Map coordinates for grid overlay
    private static final String[] COLUMN_LABELS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    private static final String[] ROW_LABELS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    
    // Images for visual enhancements
    private Image oceanBackground;
    private Image[] shipImages = new Image[SHIP_SIZES.length];
    private Image[] shipImagesVertical = new Image[SHIP_SIZES.length];
    private Image explosionImage;
    private Image splashImage;
    private Image[] explosionFrames;
    private Image[] splashFrames;
    
    public BattleshipGame() {
        setTitle("Battleship Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(900, 600));
        
        // Load images
        loadImages();
        
        // Initialize main layout
        mainLayout = new CardLayout();
        mainPanel = new JPanel(mainLayout);
        
        // Create all game screens
        createStartScreen();
        createPlacementScreen();
        createBattleScreen();
        createGameOverScreen();
        
        // Add screens to main panel
        mainPanel.add(startPanel, START_SCREEN);
        mainPanel.add(placementPanel, PLACEMENT_SCREEN);
        mainPanel.add(battlePanel, BATTLE_SCREEN);
        mainPanel.add(gameOverPanel, GAME_OVER_SCREEN);
        
        // Show the start screen initially
        mainLayout.show(mainPanel, START_SCREEN);
        
        // Start ocean wave animation
        startWaveAnimation();
        
        // Add main panel to frame
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void loadImages() {
        try {
            // Load ocean background
            oceanBackground = loadResourceImage("ocean_background.png");
            
            // Load ship images
            for (int i = 0; i < SHIP_SIZES.length; i++) {
                String shipFileName = "ship_" + (i + 1) + ".png";
                shipImages[i] = loadResourceImage(shipFileName);
                
                // Create vertical versions by rotating the ship images
                shipImagesVertical[i] = createRotatedImage(shipImages[i], 90);
            }
            
            // Load explosion and splash images
            explosionImage = loadResourceImage("explosion.png");
            splashImage = loadResourceImage("splash.png");
            
            // Create explosion animation frames
            explosionFrames = createAnimationFrames(explosionImage, 4, 3);
            
            // Create splash animation frames
            splashFrames = createAnimationFrames(splashImage, 3, 2);
            
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Image loadResourceImage(String fileName) {
        try {
            // First try to load from resources
            InputStream is = getClass().getResourceAsStream("/images/" + fileName);
            if (is != null) {
                return ImageIO.read(is);
            }
            
            // If not found in resources, try to load from file
            File file = new File("images/" + fileName);
            if (file.exists()) {
                return ImageIO.read(file);
            }
            
            // Create a placeholder image if the file is not found
            System.out.println("Image not found: " + fileName + ", using placeholder");
            BufferedImage placeholder = createPlaceholderImage(fileName);
            return placeholder;
            
        } catch (IOException e) {
            System.err.println("Failed to load image: " + fileName);
            e.printStackTrace();
            return createPlaceholderImage(fileName);
        }
    }
    
    private BufferedImage createPlaceholderImage(String name) {
        // Create a placeholder image with text
        BufferedImage img = new BufferedImage(100, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, 100, 40);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, 99, 39);
        g2d.setColor(Color.WHITE);
        g2d.drawString(name, 5, 20);
        g2d.dispose();
        return img;
    }
    
    private Image createRotatedImage(Image originalImage, double degrees) {
        if (originalImage == null) return null;
        
        int width = originalImage.getWidth(null);
        int height = originalImage.getHeight(null);
        
        BufferedImage rotatedImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotatedImage.createGraphics();
        
        g2d.rotate(Math.toRadians(degrees), height / 2.0, width / 2.0);
        g2d.translate((height - width) / 2, (width - height) / 2);
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        
        return rotatedImage;
    }
    
    private Image[] createAnimationFrames(Image spriteSheet, int cols, int rows) {
        if (spriteSheet == null) {
            return new Image[0];
        }
        
        int width = spriteSheet.getWidth(null) / cols;
        int height = spriteSheet.getHeight(null) / rows;
        Image[] frames = new Image[cols * rows];
        
        BufferedImage bufferedSpriteSheet = new BufferedImage(
            spriteSheet.getWidth(null),
            spriteSheet.getHeight(null),
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = bufferedSpriteSheet.createGraphics();
        g2d.drawImage(spriteSheet, 0, 0, null);
        g2d.dispose();
        
        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                frames[index] = bufferedSpriteSheet.getSubimage(
                    col * width, row * height, width, height
                );
                index++;
            }
        }
        
        return frames;
    }
    
    
    class OceanBackgroundPanel extends JPanel {
        private Image background;

        public OceanBackgroundPanel(Image background, LayoutManager layout) {
            super(layout);
            this.background = background;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (background != null) {
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private void createStartScreen() {
        startPanel = new OceanBackgroundPanel(oceanBackground, new BorderLayout());
        startPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create title with custom font
        titleLabel = new JLabel("BATTLESHIP", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Impact", Font.BOLD, 72));
        titleLabel.setForeground(Color.WHITE);
        
        // Create logo panel
        logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw a battleship silhouette
                drawBattleshipLogo(g2d, getWidth(), getHeight());
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(300, 150));
        
        // Create buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        
        newGameButton = createMenuButton("New Game");
        exitButton = createMenuButton("Exit");
        
        newGameButton.addActionListener(e -> startNewGame());
        exitButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(newGameButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createVerticalGlue());
        
        // Add components to start panel
        startPanel.add(titleLabel, BorderLayout.NORTH);
        startPanel.add(logoPanel, BorderLayout.CENTER);
        startPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(new Color(100, 149, 237));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 50));
        button.setPreferredSize(new Dimension(200, 50));
        
        return button;
    }
    
    private void drawBattleshipLogo(Graphics2D g2d, int width, int height) {
        // Set up ship colors
        Color hullColor = new Color(60, 60, 60);
        Color deckColor = new Color(80, 80, 80);
        Color smokeColor = new Color(200, 200, 200, 150);
        
        // Center position
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Ship dimensions
        int shipWidth = width * 3 / 4;
        int shipHeight = height / 3;
        int hullHeight = shipHeight / 2;
        
        // Draw hull
        g2d.setColor(hullColor);
        
        // Main hull shape
        Path2D hull = new Path2D.Double();
        hull.moveTo(centerX - shipWidth / 2, centerY);
        hull.lineTo(centerX - shipWidth / 3, centerY + hullHeight);
        hull.lineTo(centerX + shipWidth / 3, centerY + hullHeight);
        hull.lineTo(centerX + shipWidth / 2, centerY);
        hull.closePath();
        g2d.fill(hull);
        
        // Draw deck structures
        g2d.setColor(deckColor);
        
        // Main command tower
        int towerWidth = shipWidth / 6;
        int towerHeight = shipHeight * 2 / 3;
        g2d.fillRect(centerX - towerWidth / 2, centerY - towerHeight, towerWidth, towerHeight);
        
        // Forward gun turret
        int turretSize = shipWidth / 10;
        g2d.fillOval(centerX - shipWidth / 3, centerY - turretSize, turretSize * 2, turretSize);
        
        // Rear gun turret
        g2d.fillOval(centerX + shipWidth / 6, centerY - turretSize, turretSize * 2, turretSize);
        
        // Draw guns
        g2d.setStroke(new BasicStroke(turretSize / 2));
        g2d.setColor(hullColor);
        
        // Forward guns
        g2d.drawLine(centerX - shipWidth / 3 + turretSize, centerY - turretSize / 2, 
                     centerX - shipWidth / 2, centerY - turretSize / 2);
        
        // Rear guns
        g2d.drawLine(centerX + shipWidth / 6 + turretSize, centerY - turretSize / 2, 
                     centerX + shipWidth / 3, centerY - turretSize / 2);
        
        // Draw smoke from smokestack
        g2d.setColor(smokeColor);
        int smokeX = centerX;
        int smokeY = centerY - towerHeight;
        int smokeSize = turretSize;
        
        for (int i = 0; i < 4; i++) {
            g2d.fillOval(smokeX - smokeSize / 2 + (i * 5), smokeY - (i * 15), smokeSize, smokeSize);
            smokeSize += 5;
        }
        
        // Draw water waves
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        
        int waveWidth = width / 6;
        int waveCount = 5;
        int startX = centerX - (waveCount * waveWidth) / 2;
        
        for (int i = 0; i < waveCount; i++) {
            int waveX = startX + (i * waveWidth);
            g2d.drawArc(waveX, centerY + hullHeight + 10, waveWidth, height / 10, 0, 180);
        }
    }
    
    private void createPlacementScreen() {
        placementPanel = new OceanBackgroundPanel(oceanBackground, new BorderLayout());
        placementPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create player's grid for placement
        playerPlacementPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 2, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw water background
                g.drawImage(oceanBackground, 0, 0, getWidth(), getHeight(), this);
            }
        };
        playerPlacementPanel.setOpaque(false);
        playerPlacementPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 100), 2), 
                "Place Your Ships", 
                javax.swing.border.TitledBorder.CENTER, 
                javax.swing.border.TitledBorder.TOP,
                new Font("Naval", Font.BOLD, 16),
                Color.BLACK));
        
        // Create ship placement info panel
        JPanel shipInfoPanel = new JPanel();
        shipInfoPanel.setOpaque(false);
        shipInfoPanel.setLayout(new BoxLayout(shipInfoPanel, BoxLayout.Y_AXIS));
        
        JLabel shipListTitle = new JLabel("Ships to Place:");
        shipListTitle.setFont(new Font("Arial", Font.BOLD, 16));
        shipListTitle.setForeground(Color.BLACK);
        shipListTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel shipsList = new JPanel();
        shipsList.setOpaque(false);
        shipsList.setLayout(new BoxLayout(shipsList, BoxLayout.Y_AXIS));
        
        for (int i = 0; i < SHIP_NAMES.length; i++) {
            JPanel shipEntry = new JPanel(new FlowLayout(FlowLayout.LEFT));
            shipEntry.setOpaque(false);
            
            JLabel shipLabel = new JLabel(SHIP_NAMES[i] + " (" + SHIP_SIZES[i] + " cells)");
            shipLabel.setForeground(Color.BLACK);
            shipLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            final int index = i;
            JPanel shipColorSwatch = new JPanel() {
                
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (shipImages[index] != null) {
                        g.drawImage(shipImages[index], 0, 0, getWidth(), getHeight(), this);
                    } else {
                        g.setColor(SHIP_COLORS[index]);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };
            shipColorSwatch.setPreferredSize(new Dimension(40, 20));
            shipColorSwatch.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            
            shipEntry.add(shipColorSwatch);
            shipEntry.add(shipLabel);
            shipsList.add(shipEntry);
        }
        
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setOpaque(false);
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        
        JLabel instructionsTitle = new JLabel("Instructions:");
        instructionsTitle.setFont(new Font("Arial", Font.BOLD, 16));
        instructionsTitle.setForeground(Color.WHITE);
        instructionsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JTextArea instructions = new JTextArea(
            "1. Click on the grid to place ships\n" +
            "2. Use the Rotate button to change orientation\n" +
            "3. Ships cannot touch each other\n" +
            "4. Click Reset to start over\n" +
            "5. Click Start Battle when ready"
        );
        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setFont(new Font("Arial", Font.PLAIN, 14));
        instructions.setForeground(Color.WHITE);
        instructions.setOpaque(false);
        instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        instructionsPanel.add(instructionsTitle);
        instructionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        instructionsPanel.add(instructions);
        
        shipInfoPanel.add(shipListTitle);
        shipInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        shipInfoPanel.add(shipsList);
        shipInfoPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        shipInfoPanel.add(instructionsPanel);
        
        // Create control panel for ship placement
        placementControlPanel = new JPanel();
        placementControlPanel.setBackground(new Color(20, 30, 70, 180));
        
        rotatePlacementButton = new JButton("Rotate Ship (Currently: Horizontal)");
        rotatePlacementButton.setBackground(new Color(100, 149, 237));
        rotatePlacementButton.setForeground(Color.BLACK);
        rotatePlacementButton.addActionListener(e -> {
            isHorizontal = !isHorizontal;
            rotatePlacementButton.setText("Rotate Ship (Currently: " + (isHorizontal ? "Horizontal" : "Vertical") + ")");
        });
        
        resetPlacementButton = new JButton("Reset Placement");
        resetPlacementButton.setBackground(new Color(100, 149, 237));
        resetPlacementButton.setForeground(Color.BLACK);
        resetPlacementButton.addActionListener(e -> resetShipPlacement());
        
        startBattleButton = new JButton("Start Battle");
        startBattleButton.setBackground(new Color(50, 200, 50));
        startBattleButton.setForeground(Color.BLACK);
        startBattleButton.setEnabled(false);
        startBattleButton.addActionListener(e -> startBattle());
        
        placementControlPanel.add(rotatePlacementButton);
        placementControlPanel.add(resetPlacementButton);
        placementControlPanel.add(startBattleButton);
        
        // Status panel
        JPanel placementStatusPanel = new JPanel();
        placementStatusPanel.setBackground(new Color(30, 40, 80, 180));
        placementStatusLabel = new JLabel("Place your " + SHIP_NAMES[currentShipIndex] + " (" + SHIP_SIZES[currentShipIndex] + " cells)");
        placementStatusLabel.setForeground(Color.WHITE);
        placementStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        placementStatusPanel.add(placementStatusLabel);
        
        // Add components to placement panel
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(playerPlacementPanel);
        centerPanel.add(shipInfoPanel);
        
        placementPanel.add(centerPanel, BorderLayout.CENTER);
        placementPanel.add(placementControlPanel, BorderLayout.NORTH);
        placementPanel.add(placementStatusPanel, BorderLayout.SOUTH);
    }
    
    private void createBattleScreen() {
        battlePanel = new OceanBackgroundPanel(oceanBackground, new BorderLayout());
        battlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create panels for player and computer grids
        JPanel gridsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        gridsPanel.setOpaque(false);
        
        // Player's grid
        playerBattlePanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 2, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw water background
                g.drawImage(oceanBackground, 0, 0, getWidth(), getHeight(), this);
            }
        };
        playerBattlePanel.setOpaque(false);
        playerBattlePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 100), 2), 
                "Your Fleet", 
                javax.swing.border.TitledBorder.CENTER, 
                javax.swing.border.TitledBorder.TOP,
                new Font("Naval", Font.BOLD, 16),
                Color.WHITE));
        
        // Computer's grid
        computerPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 2, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw water background
                g.drawImage(oceanBackground, 0, 0, getWidth(), getHeight(), this);
            }
        };
        computerPanel.setOpaque(false);
        computerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 0, 0), 2), 
                "Enemy Waters", 
                javax.swing.border.TitledBorder.CENTER, 
                javax.swing.border.TitledBorder.TOP,
                new Font("Naval", Font.BOLD, 16),
                Color.BLACK));
        
        gridsPanel.add(playerBattlePanel);
        gridsPanel.add(computerPanel);
        
        // Create battle controls
        JPanel battleControlPanel = new JPanel();
        battleControlPanel.setBackground(new Color(20, 30, 70, 180));
        
        surrenderButton = new JButton("Surrender");
        surrenderButton.setBackground(new Color(200, 50, 50));
        surrenderButton.setForeground(Color.BLACK);
        surrenderButton.addActionListener(e -> {
            playerWon = false;
            showGameOverScreen();
        });
        
        battleControlPanel.add(surrenderButton);
        
        // Create status panel
        JPanel battleStatusPanel = new JPanel();
        battleStatusPanel.setBackground(new Color(30, 40, 80, 180));
        battleStatusLabel = new JLabel("Battle started! Your turn! Select a target.");
        battleStatusLabel.setForeground(Color.BLACK);
        battleStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        battleStatusPanel.add(battleStatusLabel);
        
        // Add components to battle panel
        battlePanel.add(gridsPanel, BorderLayout.CENTER);
        battlePanel.add(battleControlPanel, BorderLayout.NORTH);
        battlePanel.add(battleStatusPanel, BorderLayout.SOUTH);
    }
    
    private void createGameOverScreen() {
        gameOverPanel = new OceanBackgroundPanel(oceanBackground, new BorderLayout());
        gameOverPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create game over title
        JLabel gameOverTitle = new JLabel("GAME OVER", SwingConstants.CENTER);
        gameOverTitle.setFont(new Font("Impact", Font.BOLD, 72));
        gameOverTitle.setForeground(Color.BLACK);
        
        // Create result panel
        JPanel resultPanel = new JPanel();
        resultPanel.setOpaque(false);
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        
        gameResultLabel = new JLabel("Victory!", SwingConstants.CENTER);
        gameResultLabel.setFont(new Font("Arial", Font.BOLD, 36));
        gameResultLabel.setForeground(Color.BLACK);
        gameResultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create buttons panel
        JPanel gameOverButtonPanel = new JPanel();
        gameOverButtonPanel.setLayout(new BoxLayout(gameOverButtonPanel, BoxLayout.Y_AXIS));
        gameOverButtonPanel.setOpaque(false);
        gameOverButtonPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        
        playAgainButton = createMenuButton("Play Again");
        exitToMenuButton = createMenuButton("Main Menu");
        
        playAgainButton.addActionListener(e -> startNewGame());
        exitToMenuButton.addActionListener(e -> mainLayout.show(mainPanel, START_SCREEN));
        
        
        gameOverButtonPanel.add(playAgainButton);
        gameOverButtonPanel.add(exitToMenuButton);
        gameOverButtonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        
        
        
        // Add components to result panel
        resultPanel.add(Box.createVerticalGlue());
        resultPanel.add(gameResultLabel);
        resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        resultPanel.add(createStatsPanel());
        resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Add components to game over panel
        gameOverPanel.add(gameOverTitle, BorderLayout.NORTH);
        gameOverPanel.add(resultPanel, BorderLayout.CENTER);
        gameOverPanel.add(gameOverButtonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createStatsPanel() {
// The code continues from where it left off in createStatsPanel()
        
        // Create a stats panel with a dark semi-transparent background
        JPanel statsPanel = new JPanel();
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        
        // Create some placeholder stats labels (will be updated when game ends)
        JLabel shotsLabel = new JLabel("Shots Fired: 0", SwingConstants.CENTER);
        JLabel hitsLabel = new JLabel("Hits: 0", SwingConstants.CENTER);
        JLabel accuracyLabel = new JLabel("Accuracy: 0%", SwingConstants.CENTER);
        
        // Style the stats labels
        Font statsFont = new Font("Arial", Font.BOLD, 18);
        Color statsColor = Color.WHITE;
        
        shotsLabel.setFont(statsFont);
        shotsLabel.setForeground(statsColor);
        shotsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        hitsLabel.setFont(statsFont);
        hitsLabel.setForeground(statsColor);
        hitsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        accuracyLabel.setFont(statsFont);
        accuracyLabel.setForeground(statsColor);
        accuracyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add labels to the stats panel
        statsPanel.add(shotsLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statsPanel.add(hitsLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statsPanel.add(accuracyLabel);
        
        return statsPanel;
    }
    
    private void startWaveAnimation() {
        // Create a timer to animate water waves
        waveTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                waveOffset = (waveOffset + 1) % 20;
                repaint();
            }
        });
        waveTimer.start();
    }
    
    private void startNewGame() {
        // Initialize game state
        resetGameState();
        
        // Initialize player grid for placement
        GridManager.initializePlayerGrid(playerPlacementPanel,
        	    playerGrid,
        	    GRID_SIZE,
        	    SHIP_SIZES,
        	    currentShipIndex,
        	    isHorizontal,
        	    shipImages,
        	    shipImagesVertical,
        	    new MouseAdapter() {
        	        @Override
        	        public void mousePressed(MouseEvent e) {
        	            Object source = e.getSource();
        	            if (source instanceof JButton button) {
        	                for (int row = 0; row < GRID_SIZE; row++) {
        	                    for (int col = 0; col < GRID_SIZE; col++) {
        	                        if (playerGrid[row][col].getButton() == button) {
        	                            previewShipPlacement(row, col);
        	                            return;
        	                        }
        	                    }
        	                }
        	            }
        	        }

        	        @Override
        	        public void mouseReleased(MouseEvent e) {
        	            Object source = e.getSource();
        	            if (source instanceof JButton button) {
        	                for (int row = 0; row < GRID_SIZE; row++) {
        	                    for (int col = 0; col < GRID_SIZE; col++) {
        	                        if (playerGrid[row][col].getButton() == button) {
        	                            placeShip(row, col);
        	                            return;
        	                        }
        	                    }
        	                }
        	            }
        	        }
        	    },
        	    this::updatePlacementStatus,
        	    this::previewShipPlacement,
        	    this::placeShip);
        
        // Show placement screen
        mainLayout.show(mainPanel, PLACEMENT_SCREEN);
    }
    
    private void resetGameState() {
        // Reset game state variables
        gameInProgress = false;
        placementPhase = true;
        playerTurn = true;
        isHorizontal = true;
        currentShipIndex = 0;
        
        // Reset grids
        playerGrid = new Cell[GRID_SIZE][GRID_SIZE];
        computerGrid = new Cell[GRID_SIZE][GRID_SIZE];
    }
    
 
    
 
    private void previewShipPlacement(int row, int col) {
        GridManager.clearPlacementPreview(playerGrid, playerPlacementPanel, GRID_SIZE);
        
        int shipSize = SHIP_SIZES[currentShipIndex];
        boolean isValid = true;
        
        // Check if the ship fits on the board
        if (isHorizontal) {
            if (col + shipSize > GRID_SIZE) {
                isValid = false;
            }
        } else {
            if (row + shipSize > GRID_SIZE) {
                isValid = false;
            }
        }
        
        // Check if any part of the ship would overlap with another ship
        if (isValid) {
            for (int i = 0; i < shipSize; i++) {
                int r = isHorizontal ? row : row + i;
                int c = isHorizontal ? col + i : col;
                
                if (r < GRID_SIZE && c < GRID_SIZE) {
                    if (playerGrid[r][c].hasShip() || 
                        Gameutils.cellAdjacentToShip(r, c, playerGrid, true)) {
                        isValid = false;
                        break;
                    }
                }
            }
        }
        
        // Set preview state for cells
        for (int i = 0; i < shipSize; i++) {
            int r = isHorizontal ? row : row + i;
            int c = isHorizontal ? col + i : col;
            
            if (r < GRID_SIZE && c < GRID_SIZE) {
                playerGrid[r][c].setPreview(true);
                playerGrid[r][c].setValidPlacement(isValid);
            }
        }
        
        playerPlacementPanel.repaint();
    }
    
 
    
    private void placeShip(int row, int col) {
        if (currentShipIndex >= SHIP_SIZES.length) {
            return; // All ships placed
        }
        
        int shipSize = SHIP_SIZES[currentShipIndex];
        boolean isValid = true;
        
        // Check if the ship fits on the board
        if (isHorizontal) {
            if (col + shipSize > GRID_SIZE) {
                isValid = false;
            }
        } else {
            if (row + shipSize > GRID_SIZE) {
                isValid = false;
            }
        }
        
        // Check if any part of the ship would overlap with another ship
        if (isValid) {
            for (int i = 0; i < shipSize; i++) {
                int r = isHorizontal ? row : row + i;
                int c = isHorizontal ? col + i : col;
                
                if (r < GRID_SIZE && c < GRID_SIZE) {
                    if (playerGrid[r][c].hasShip() || 
                        Gameutils.cellAdjacentToShip(r, c, playerGrid, true)) {
                        isValid = false;
                        break;
                    }
                }
            }
        }
        
        if (isValid) {
            // Place the ship
            for (int i = 0; i < shipSize; i++) {
                int r = isHorizontal ? row : row + i;
                int c = isHorizontal ? col + i : col;
                
                playerGrid[r][c].setShip(true);
                playerGrid[r][c].setShipType(currentShipIndex);
                playerGrid[r][c].setShipPart(i);
                playerGrid[r][c].setHorizontalPlacement(isHorizontal);
            }
            
            // Move to the next ship
            currentShipIndex++;
            
            // Update status
            updatePlacementStatus();
            
            // Check if all ships are placed
            if (currentShipIndex >= SHIP_SIZES.length) {
                startBattleButton.setEnabled(true);
                placementStatusLabel.setText("All ships placed! Click 'Start Battle' to begin!");
            }
            
            playerPlacementPanel.repaint();
        }
    }
    
    private void updatePlacementStatus() {
        if (currentShipIndex < SHIP_SIZES.length) {
            placementStatusLabel.setText("Place your " + SHIP_NAMES[currentShipIndex] + 
                                         " (" + SHIP_SIZES[currentShipIndex] + " cells)");
        } else {
            placementStatusLabel.setText("All ships placed! Click 'Start Battle' to begin!");
        }
    }
    
    private void resetShipPlacement() {
        // Reset ship placement
        currentShipIndex = 0;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                playerGrid[r][c].setShip(false);
                playerGrid[r][c].setShipType(-1);
                playerGrid[r][c].setShipPart(-1);
            }
        }
        
        // Update UI elements
        startBattleButton.setEnabled(false);
        updatePlacementStatus();
        playerPlacementPanel.repaint();
    }
    
    private void startBattle() {
        // Set game in progress
        gameInProgress = true;
        placementPhase = false;
        
        // Initialize computer grid and place ships randomly
        Gameutils.initializeComputerGrid(computerGrid);

        
        // Set up the battle UI
        setupBattleUI();
        
        // Show battle screen
        mainLayout.show(mainPanel, BATTLE_SCREEN);
    }
    
    
    private void setupBattleUI() {
        // Set up player's grid in battle view
        playerBattlePanel.removeAll();
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JPanel cellPanel = GridManager.createCellPanel(playerGrid[row][col], true,SHIP_SIZES, shipImages, shipImagesVertical, explosionFrames, splashFrames);
                playerBattlePanel.add(cellPanel);
            }
        }
        
        // Set up computer's grid in battle view
        computerPanel.removeAll();
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JPanel cellPanel = GridManager.createCellPanel(computerGrid[row][col], false,SHIP_SIZES, shipImages, shipImagesVertical, explosionFrames, splashFrames);
                
                // Add click listener for player's turn
                final int finalRow = row;
                final int finalCol = col;
                
                cellPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (gameInProgress && playerTurn && !computerGrid[finalRow][finalCol].isHit()) {
                            playerTurn(finalRow, finalCol);
                        }
                    }
                });
                
                computerPanel.add(cellPanel);
            }
        }
        
        playerBattlePanel.revalidate();
        playerBattlePanel.repaint();
        computerPanel.revalidate();
        computerPanel.repaint();
    }
    
    private void playerTurn(int row, int col) {
        if (!gameInProgress || !playerTurn) {
            return;
        }
        
        // Process player's shot
        boolean hit = computerGrid[row][col].hasShip();
        computerGrid[row][col].setHit(true);
        startCellAnimation(computerGrid[row][col]);
        
        String resultMessage = hit ? "HIT!" : "Miss.";
        battleStatusLabel.setText("You fired at " + COLUMN_LABELS[col] + ROW_LABELS[row] + ". " + resultMessage);
        
        // Check if a ship was sunk
        if (hit && isShipSunk(row, col, computerGrid)) {
            int shipType = computerGrid[row][col].getShipType();
            battleStatusLabel.setText(battleStatusLabel.getText() + " You sank the enemy's " + SHIP_NAMES[shipType] + "!");
        }
        
        // Check for game over
        if (allShipsSunk(computerGrid)) {
            gameInProgress = false;
            playerWon = true;
            
            // Delay to show last hit
            Timer delayTimer = new Timer(1500, e -> showGameOverScreen());
            delayTimer.setRepeats(false);
            delayTimer.start();
            
            return;
        }
        
        computerPanel.repaint();
        
        // Switch turns
        playerTurn = false;
        
        // Computer's turn after a delay
        Timer computerTurnTimer = new Timer(1000, e -> computerTurn());
        computerTurnTimer.setRepeats(false);
        computerTurnTimer.start();
    }
    
    private void computerTurn() {
        if (!gameInProgress || playerTurn) {
            return;
        }
        
        // Computer's targeting logic:
        // 1. If there are damaged ships, try to finish them off
        // 2. Otherwise, choose a random untargeted cell
        
        int row, col;
        boolean validTarget = false;
        
        // Try to find and target damaged ships
        boolean foundDamagedShip = false;
        int[] target = findDamagedShip();
        
        if (target != null) {
            row = target[0];
            col = target[1];
            foundDamagedShip = true;
            validTarget = true;
        } else {
            // No damaged ships, use random targeting
            // Make up to 100 attempts to find a valid target
            int attempts = 0;
            do {
                row = random.nextInt(GRID_SIZE);
                col = random.nextInt(GRID_SIZE);
                validTarget = !playerGrid[row][col].isHit();
                attempts++;
            } while (!validTarget && attempts < 100);
            
            // If we couldn't find a target after 100 attempts, try sequential search
            if (!validTarget) {
                for (int r = 0; r < GRID_SIZE && !validTarget; r++) {
                    for (int c = 0; c < GRID_SIZE && !validTarget; c++) {
                        if (!playerGrid[r][c].isHit()) {
                            row = r;
                            col = c;
                            validTarget = true;
                        }
                    }
                }
            }
        }
        
        if (validTarget) {
            // Process computer's shot
            boolean hit = playerGrid[row][col].hasShip();
            playerGrid[row][col].setHit(true);
            startCellAnimation(playerGrid[row][col]);
            
            String targetMethod = foundDamagedShip ? "targets the damaged ship" : "fires";
            String resultMessage = hit ? "HIT!" : "Miss.";
            battleStatusLabel.setText("Computer " + targetMethod + " at " + COLUMN_LABELS[col] + ROW_LABELS[row] + ". " + resultMessage);
            
            // Check if a ship was sunk
            if (hit && isShipSunk(row, col, playerGrid)) {
                int shipType = playerGrid[row][col].getShipType();
                battleStatusLabel.setText(battleStatusLabel.getText() + " Your " + SHIP_NAMES[shipType] + " was sunk!");
            }
            
            // Check for game over
            if (allShipsSunk(playerGrid)) {
                gameInProgress = false;
                playerWon = false;
                
                // Delay to show last hit
                Timer delayTimer = new Timer(1500, e -> showGameOverScreen());
                delayTimer.setRepeats(false);
                delayTimer.start();
                
                return;
            }
            
            playerBattlePanel.repaint();
        }
        
        // Switch turns back to player
        playerTurn = true;
    }
    
    private int[] findDamagedShip() {
        // Look for cells that are hit, have ships, and have adjacent ship cells that aren't hit
    	
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
            	int targetR = r, targetC = c;
                if (playerGrid[r][c].isHit() && playerGrid[r][c].hasShip()) {
                    int shipType = playerGrid[r][c].getShipType();
                    
                    // Check adjacent cells in four directions for ship parts
                    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                    
                    for (int[] dir : directions) {
                        int nr = r + dir[0];
                        int nc = c + dir[1];
                        
                        
                        // Check if in bounds
                        if (nr >= 0 && nr < GRID_SIZE && nc >= 0 && nc < GRID_SIZE) {
                            // If found adjacent ship part of same type
                            if (playerGrid[nr][nc].hasShip() && 
                                playerGrid[nr][nc].getShipType() == shipType) {
                                
                                // Look for unhit cells in this direction
                                
                                
                                // Check in reverse direction first
                                int checkR = r - dir[0];
                                int checkC = c - dir[1];
                                
                                if (checkR >= 0 && checkR < GRID_SIZE && checkC >= 0 && checkC < GRID_SIZE &&
                                    !playerGrid[checkR][checkC].isHit()) {
                                    return new int[]{checkR, checkC};
                                }
                                
                                // Continue in forward direction
                                checkR = nr;
                                checkC = nc;
                                
                                while (checkR >= 0 && checkR < GRID_SIZE && checkC >= 0 && checkC < GRID_SIZE) {
                                    if (playerGrid[checkR][checkC].hasShip() && 
                                        playerGrid[checkR][checkC].getShipType() == shipType) {
                                        
                                        if (!playerGrid[checkR][checkC].isHit()) {
                                            return new int[]{checkR, checkC};
                                        }
                                        
                                        // Continue in the same direction
                                        checkR += dir[0];
                                        checkC += dir[1];
                                    } else {
                                        break;
                                    }
                                }
                                
                                // Check next direction if needed
                                checkR = nr + dir[0];
                                checkC = nc + dir[1];
                                
                                if (checkR >= 0 && checkR < GRID_SIZE && checkC >= 0 && checkC < GRID_SIZE &&
                                    !playerGrid[checkR][checkC].isHit() &&
                                    playerGrid[checkR][checkC].hasShip() &&
                                    playerGrid[checkR][checkC].getShipType() == shipType) {
                                    return new int[]{checkR, checkC};
                                }
                            }
                            
                            // If found adjacent unhit cell, consider it as a candidate
                            if (!playerGrid[nr][nc].isHit()) {
                                // This is a possible target, save it for later if nothing better is found
                                 targetR = nr;
                                 targetC = nc;
                            }
                        }
                    }
                    
                    // If no better target found, return adjacent unhit cell
                    if (targetR != r || targetC != c) {
                        return new int[]{targetR, targetC};
                    }
                }
            }
        }
        
        // No damaged ships found
        return null;
    }
    
    private boolean isShipSunk(int row, int col, Cell[][] grid) {
        if (!grid[row][col].hasShip()) {
            return false;
        }
        
        int shipType = grid[row][col].getShipType();
        
        // Check all cells for this ship type to see if they're all hit
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (grid[r][c].hasShip() && grid[r][c].getShipType() == shipType && !grid[r][c].isHit()) {
                    return false; // Found an unhit part of this ship
                }
            }
        }
        
        return true; // All parts of this ship are hit
    }

    private boolean allShipsSunk(Cell[][] grid) {
        // Check if all ships in the grid have been sunk
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (grid[r][c].hasShip() && !grid[r][c].isHit()) {
                    return false; // Found an unhit ship part
                }
            }
        }
        return true; // All ship parts are hit
    }

    private void startCellAnimation(Cell cell) {
        // Set the cell as animating
        cell.setAnimating(true);
        cell.setAnimationFrame(0);
        
        // Create timer for animation
        Timer animationTimer = new Timer(100, new ActionListener() {
            private int frame = 0;
            private final int MAX_FRAMES = cell.hasShip() ? explosionFrames.length : splashFrames.length;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                frame++;
                cell.setAnimationFrame(frame);
                
                // Repaint the appropriate panel
                if (cell.getRow() < GRID_SIZE && cell.getCol() < GRID_SIZE) {
                    if (computerGrid[0][0] == cell || isInGrid(cell, computerGrid)) {
                        computerPanel.repaint();
                    } else if (playerGrid[0][0] == cell || isInGrid(cell, playerGrid)) {
                        playerBattlePanel.repaint();
                    }
                }
                
                // Stop the animation after all frames
                if (frame >= MAX_FRAMES - 1) {
                    ((Timer)e.getSource()).stop();
                    cell.setAnimating(false);
                }
            }
        });
        
        animationTimer.setRepeats(true);
        animationTimer.start();
    }

    private boolean isInGrid(Cell cell, Cell[][] grid) {
        // Helper method to check if a cell is in a specific grid
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (grid[r][c] == cell) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showGameOverScreen() {
        // Calculate game statistics
        int totalShots = 0;
        int totalHits = 0;
        
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (computerGrid[r][c].isHit()) {
                    totalShots++;
                    if (computerGrid[r][c].hasShip()) {
                        totalHits++;
                    }
                }
            }
        }
        
        // Calculate accuracy
        double accuracy = totalShots > 0 ? (double)totalHits / totalShots * 100 : 0;
        
        // Update stats labels
        gameOverPanel.removeAll();
        JPanel statsPanel = createStatsPanel();
        
        // Get the stats labels
        Component[] components = statsPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getText().startsWith("Shots Fired:")) {
                    label.setText("Shots Fired: " + totalShots);
                } else if (label.getText().startsWith("Hits:")) {
                    label.setText("Hits: " + totalHits);
                } else if (label.getText().startsWith("Accuracy:")) {
                    label.setText("Accuracy: " + String.format("%.1f", accuracy) + "%");
                }
            }
        }
        
        // Create game over message
        JLabel gameOverLabel = new JLabel(playerWon ? "VICTORY!" : "DEFEAT!", SwingConstants.CENTER);
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 36));
        gameOverLabel.setForeground(Color.WHITE);
        gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel resultLabel = new JLabel(
            playerWon ? "You sank all enemy ships!" : "All your ships were sunk!",
            SwingConstants.CENTER
        );
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        resultLabel.setForeground(Color.WHITE);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create play again button
        JButton playAgainButton = new JButton("Play Again");
        playAgainButton.setFont(new Font("Arial", Font.BOLD, 18));
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.addActionListener(e -> startNewGame());
        
        JButton exitToMenuButton= new JButton("exit");
        exitToMenuButton.setFont(new Font("Arial", Font.BOLD, 18));
        exitToMenuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitToMenuButton.addActionListener(e ->  mainLayout.show(mainPanel, START_SCREEN));
        
        // Add components to the game over panel
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        contentPanel.add(gameOverLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(resultLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(statsPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        contentPanel.add(playAgainButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0,40)));
        contentPanel.add(exitToMenuButton);
        
        gameOverPanel.add(contentPanel, BorderLayout.CENTER);
        gameOverPanel.revalidate();
        gameOverPanel.repaint();
        
        // Show game over screen
        mainLayout.show(mainPanel, GAME_OVER_SCREEN);
    }

    // Inner class for cells
    //private class Cell {
       
    // Main method to start the game
   
    }
