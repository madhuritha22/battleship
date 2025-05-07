// BattleshipLauncher.java
import javax.swing.*;
import java.awt.*;

public class BattleshipLauncher {
	 public static void main(String[] args) {
	        SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                new BattleshipGame();
	            }
	        });
        JFrame welcomeFrame = new JFrame("Battleship Game");
        welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        welcomeFrame.setSize(500, 300);
        welcomeFrame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("BATTLESHIP");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel descriptionLabel = new JLabel("<html><center>A strategic guessing game for two players.<br>" +
                "Sink all enemy ships before they sink yours!</center></html>");
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton startButton = new JButton("Start Game");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(150, 40));
        startButton.addActionListener(e -> {
            welcomeFrame.dispose();
            SwingUtilities.invokeLater(() -> new BattleshipGame());
        });
        
        JButton exitButton = new JButton("Exit");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setMaximumSize(new Dimension(150, 40));
        exitButton.addActionListener(e -> System.exit(0));
        
        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(descriptionLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(startButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(exitButton);
        panel.add(Box.createVerticalGlue());
        
        welcomeFrame.add(panel);
        welcomeFrame.setVisible(true);
        
  
    }
}