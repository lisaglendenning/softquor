/*
 * Created on Apr 5, 2004
 *
 */
package softquor.board;
import java.util.Vector;
import java.util.ListIterator;

/**
 * @author Lisa Glendenning
 *
 */
public class Player implements Cloneable {

	private int userid;
	public int nwalls;
	public Cell position;
	public Vector goal;
	
	public Player(int userid) {
		this.userid = userid;
		nwalls = 0;
		position = new Cell(0, 0);
		goal = new Vector();
	}
	
	public int id() {
		return userid;
	}
	
	public Object clone() {
		Player child = null;
		try {
			child = (Player)super.clone();
			child.position = (Cell)position.clone();
			child.goal = new Vector();
			ListIterator lit = goal.listIterator();
			while(lit.hasNext()) {
				child.goal.add(((Cell)lit.next()).clone());
			}
		}
		catch(CloneNotSupportedException e) {
			// this shouldn't happen because Player is cloneable						
		}
		return child;
	}
	
	public String toString() {
		return "(Player[userid=" + userid + "][nwalls="
			+ nwalls + "][position=" + position + 
			"][goal=" + goal + "])";
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof Player) {
			Player p = (Player)obj;
			if(userid != p.userid) {
				return false;
			}
		
			if(nwalls != p.nwalls) {
				return false;
			}
		
			if(!position.equals(p.position)) {
				return false;
			}	
		
			if(!goal.equals(p.goal)) {
				return false;
			}
				
			return true;
		}
		return false;
	}
	
	public int hashCode() {
		return userid;
	}	
}