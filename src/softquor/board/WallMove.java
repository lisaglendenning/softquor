/*
 * Created on Mar 31, 2004
 *
 */
package softquor.board;
import java.util.StringTokenizer;

/**
 * @author Lisa Glendenning
 *
 */
public class WallMove extends Move implements Cloneable {
	public Wall wall;
	
	public WallMove(int userid) {
		super(userid);
	}
	
	public WallMove(int userid, Wall wall) {
		super(userid);
		this.wall = wall;	
	}
	
	public void parseProtocol(StringTokenizer tok) {
		wall = Wall.parse(tok);
	}
	
	public String protocolString() {
		return ("wall " + wall.protocol());
	}
	
	public String toString() {
		return "(WallMove[id=" + id + "][userid=" + userid + "][wall=" + wall + "])";
	}	
	
	public Object clone() {
		WallMove child = null;
		try {
			child = (WallMove)super.clone();
			child.wall = (Wall)wall.clone();
		}
		catch(CloneNotSupportedException e) {
			// this shouldn't happen because WallMove is cloneable						
		}
		return child;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof WallMove)) {
			return false;
		}
		
		WallMove move = (WallMove)obj;
		
		if(move.userid != userid) {
			return false;
		}
		if(move.wall != wall) {
			return false;
		}
		return true;
	}
	
	public int hashCode() {
		return id;
	}
}
