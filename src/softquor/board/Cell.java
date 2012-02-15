/*
 * Created on Mar 31, 2004
 *
 * 
 */
package softquor.board;
import java.util.StringTokenizer;
/**
 * @author Lisa Glendenning
 *
 */
public class Cell implements Cloneable {
	private int x;
	private int y;

	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int x() {
		return x;
	}
	
	public int y() {
		return y;
	}
	
	public static Cell parse(StringTokenizer tok) {
		int x = Integer.parseInt(tok.nextToken());
		int y = Integer.parseInt(tok.nextToken());
		return new Cell(x, y);
	}
	
	public String protocol() {
		return (x + " " + y);
	}
	
    protected Object clone() {
        Cell child = null;    	
        try {
            child = (Cell)super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen because Cell is cloneable
        }
        return child;
    }
	
	
	public boolean equals(Object obj) {
		if(obj instanceof Cell) {
			Cell c = (Cell)obj;
			return (c.x==x && c.y==y);
		}
		return false;
	}
	
	public int hashCode() {
		return x+y;
	}
	
	public String toString() {
		return "(Cell[x=" + x + "][y=" + y + "])";
	}
}
