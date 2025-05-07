import javax.swing.JButton;

public class Cell{
	
	 private JButton button;
	 private int row, col;
     private boolean hasShip;
     private boolean isHit;
     private boolean isPreview;
     private boolean validPlacement;
     private int shipType = -1;
     private int shipPart = -1;
     private boolean horizontalPlacement;
     private boolean animating;
     private int animationFrame;
     
     public Cell(int row, int col) {
         this.row = row;
         this.col = col;
         this.hasShip = false;
         this.isHit = false;
         this.isPreview = false;
         this.validPlacement = false;
         this.animating = false;
         this.animationFrame = 0;
     }
     
     public boolean hasShip() {
         return hasShip;
     }
     
     public void setShip(boolean hasShip) {
         this.hasShip = hasShip;
     }
     
     public boolean isHit() {
         return isHit;
     }
     
     public void setHit(boolean isHit) {
         this.isHit = isHit;
     }
     
     public boolean isPreview() {
         return isPreview;
     }
     
     public void setPreview(boolean isPreview) {
         this.isPreview = isPreview;
     }
     
     public boolean isValidPlacement() {
         return validPlacement;
     }
     
     public void setValidPlacement(boolean validPlacement) {
         this.validPlacement = validPlacement;
     }
     
     public int getShipType() {
         return shipType;
     }
     
     public void setShipType(int shipType) {
         this.shipType = shipType;
     }
     
     public int getShipPart() {
         return shipPart;
     }
     
     public void setShipPart(int shipPart) {
         this.shipPart = shipPart;
     }
     
     public boolean isHorizontalPlacement() {
         return horizontalPlacement;
     }
     
     public void setHorizontalPlacement(boolean horizontalPlacement) {
         this.horizontalPlacement = horizontalPlacement;
     }
     
     public boolean isAnimating() {
         return animating;
     }
     
     public void setAnimating(boolean animating) {
         this.animating = animating;
     }
     
     public int getAnimationFrame() {
         return animationFrame;
     }
     
     public void setAnimationFrame(int animationFrame) {
         this.animationFrame = animationFrame;
     }
     
     public JButton getButton() {
    	    return button;
    	}

    public void setButton(JButton button) {
    	    this.button = button;
    	}
    
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

 }


