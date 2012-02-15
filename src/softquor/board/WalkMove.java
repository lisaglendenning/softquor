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
public class WalkMove extends Move implements Cloneable {

	public Cell from;
	public Cell to;
	
	public WalkMove(int userid) {
		super(userid);
	}
	
	public WalkMove(int userid, Cell from, Cell to) {
		super(userid);
		this.from = from;
		this.to = to;
	}
	
	protected void parseProtocol(StringTokenizer rest) {
		from = Cell.parse(rest);
		to = Cell.parse(rest);
	}
	
	protected String protocolString() {
		return ("walk " + from.protocol() + " " + to.protocol());
	}
	
	public String toString() {
		return "(WalkMove[id=" + id + "][userid=" + userid+ "][from=" + from + "][to=" + to + "])";
	}
	
	public Object clone() {
		WalkMove child = null;
		try {
			child = (WalkMove)super.clone();
			child.from = (Cell)from.clone();
			child.to = (Cell)to.clone();
		}
		catch(CloneNotSupportedException e) {
			// this shouldn't happen because WalkMove is cloneable						
		}
		return child;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof WalkMove)) {
			return false;
		}
		
		WalkMove move = (WalkMove)obj;

		if(move.userid != userid) {
			return false;
		}
		if(!move.from.equals(from)) {
			return false;
		}
		if(!move.to.equals(to)) {
			return false;
		}
		
		return true;
	}
	
	public int hashCode() {
		return id;
	}
}
