
/** This is a simple wrapper around a 2D location.
 */
public class TwoDLoc {

    public int row, col;

    public TwoDLoc(int row, int col) {
	this.row = row;
	this.col = col;
    }

    public void set(int row, int col) {
	this.row = row;
	this.col = col;
    }

    public int getRow() {
	return row;
    }

    public int getCol() {
	return col;
    }

    public int getX() {
	return col;
    }

    public int getY() {
	return row;
    }

    public String toString() {
	return "(r,c) = ("+row+","+col+")";
    }
}
