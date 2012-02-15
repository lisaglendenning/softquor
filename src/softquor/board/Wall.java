/*
 * Created on Apr 5, 2004
 *
 */
package softquor.board;
import java.util.StringTokenizer;
/**
 * @author Lisa Glendenning
 *
 */
public class Wall implements Cloneable {
	
	private Cell nw;
	private boolean horizontal;
	
	public Wall(Cell nw, boolean horizontal) {
		this.nw = nw;
		this.horizontal = horizontal;
	}
	
	public static Wall parse(StringTokenizer tok) {
		Cell nw = Cell.parse(tok);
		boolean h = Boolean.valueOf(tok.nextToken()).booleanValue();
		return new Wall(nw, h);
	}
	
	public String protocol() {
		return (nw.protocol() + " " + horizontal);
	}
	
	public Cell northwest() {
		return nw;
	}
	
	public boolean horizontal() {
		return horizontal;
	}
	
	public Object clone() {
		Wall child = null;
		try {
			child = (Wall)super.clone();
			child.nw = (Cell)nw.clone();
			
		}
		catch(CloneNotSupportedException e) {
			// this shouldn't happen because Wall is cloneable						
		}
		return child;
	}
	
	public String toString() {
		return "(Wall[nw=" + nw 
			+ "][horizontal="+ horizontal + "])";
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof Wall) {
			Wall w = (Wall)obj;
			if(!nw.equals(w.nw)) {
				return false;
			}
			return (horizontal == w.horizontal);
		}
		return false;
	}
	
	public int hashCode() {
		return nw.hashCode();
	}	
}
