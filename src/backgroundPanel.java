import javax.swing.*;
import java.awt.*;

public class backgroundPanel extends JPanel {
    private Image backgroundImage;

    public backgroundPanel(String imagePath) {
        backgroundImage = new ImageIcon(imagePath).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Stretch image to panel size
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}
