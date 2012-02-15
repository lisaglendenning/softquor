/*
 * Created on Nov 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package softquor.board;
import java.util.Vector;

/**
 * @author Lisa
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestBoard {

	public static void main(String[] args) {
		Player p = new Player(1);
		p.nwalls = 10;
		Cell position = new Cell(4, 5);
		p.position = position;
		Cell g1 = new Cell(8,0);
		Cell g2 = new Cell(8,1);
		Cell g3 = new Cell(8,2);
		Vector goal = new Vector();
		goal.add(g1);
		goal.add(g2);
		goal.add(g3);
		p.goal = goal;
		
		BoardState state = new BoardState();
		state.players.add(p);
		
		System.out.println(state);
		BoardState state2 = (BoardState)state.clone();
		System.out.println(state2);
		System.out.println(state2.equals(state));
	}
}
