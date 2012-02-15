/*
 * Created on Apr 7, 2004
 *
 */
package softquor.board;
import java.util.Vector;
import java.util.ListIterator;

/**
 * @author Lisa Glendenning
 *
 */
public class BoardState implements Cloneable {

	// for purposes of providing a static hashCode
	static private int NUMSTATES = 0;
	static public final int PREGAME = 0;
	static public final int GAME = 1;
	static public final int ENDGAME = 2;
	private int id;
	
	public Vector players;
	public Vector walls;
	public int nextplayer;	
	public int phase;
	public int turns;
	
	public BoardState() {
		id = NUMSTATES;
		NUMSTATES++;
		players = new Vector();
		walls = new Vector();
		nextplayer = 0;
		phase = PREGAME;
		turns = 0;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof BoardState)) {
			return false;
		}
		BoardState state = (BoardState)obj;
		if(!players.equals(state.players)) {
			return false;
		}
		if(!walls.equals(state.walls)) {
			return false;
		}
		if(nextplayer != state.nextplayer) {
			return false;
		}
		if(phase != state.phase) {
			return false;
		}
		if(turns != state.turns) {
			return false;
		}
		return true;
	}
	
	public int hashCode() {
		return id;
	}
	
	public BoardState child() {
		BoardState child = new BoardState();

		ListIterator lit = players.listIterator();
		while(lit.hasNext()) {
			Player p = (Player)((Player)lit.next()).clone();
			child.players.add(p);			
		}
			
		lit = walls.listIterator();
		while(lit.hasNext()) {
			child.walls.add(((Wall)lit.next()).clone());			
		}

		child.nextplayer = nextplayer;
		child.phase = phase;
		child.turns = turns;
		
		return child;
	}
	
	public Object clone() {
		BoardState child = null;
		try {
			child = (BoardState)super.clone();
			
			child.players = new Vector();
			ListIterator lit = players.listIterator();
			while(lit.hasNext()) {
				Player p = (Player)((Player)lit.next()).clone();
				child.players.add(p);			
			}

			child.walls = new Vector();			
			lit = walls.listIterator();
			while(lit.hasNext()) {
				child.walls.add(((Wall)lit.next()).clone());			
			}
		}
		catch(CloneNotSupportedException e) {
	        // this shouldn't happen because BoardState is cloneable			
		}
		return child;
	}	
	
	public String toString() {
		return "(BoardState[id=" + id + "][players=" + players + "][walls="
			+ walls + "][nextplayer=" + nextplayer + "][phase=" + phase + 
			"][turns=" + turns + "])";
	}
}
