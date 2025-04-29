// Ship.java

public class Ship {
    private String name;
    private int size;
    private int hits = 0;
    private boolean isSunk = false;
    private Coordinate[] coordinates;
    
    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
        this.coordinates = new Coordinate[size];
    }
    
    public String getName() {
        return name;
    }
 
    public int getSize() {
        return size;
    }
    
    public boolean isSunk() {
        return hits >= size;
    }
    
    public void hit() {
        hits++;
        if (hits >= size) {
            isSunk = true;
        }
    }
    
    public void setCoordinate(int index, int row, int col) {
        if (index < size) {
            coordinates[index] = new Coordinate(row, col);
        }
    }
    
    public Coordinate[] getCoordinates() {
        return coordinates;
    }
    
    public static class Coordinate {
        private int row;
        private int col;
        
        public Coordinate(int row, int col) {
            this.row = row;
            this.col = col;
        }
        
        public int getRow() {
            return row;
        }
        
        public int getCol() {
            return col;
        }
    }

}