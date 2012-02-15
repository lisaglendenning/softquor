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
public abstract class Move implements Cloneable {
	private static int NUM_MOVES = 0;
	protected int id;
	public int userid;
	
	public Move(int userid) {
		this.id = NUM_MOVES;
		NUM_MOVES++;
		this.userid = userid;
	}
	
	public static Move parse(StringTokenizer tok) {
		Move move = null;
		int id = Integer.valueOf(tok.nextToken()).intValue();
		int userid = Integer.valueOf(tok.nextToken()).intValue();
		String type = tok.nextToken();
		if(type.equals("walk")) {
			move = new WalkMove(userid);
		}
		else if(type.equals("wall")) {
			move = new WallMove(userid);
		}
		move.parseProtocol(tok);
		return move;
	}
	
	protected abstract void parseProtocol(StringTokenizer rest);
	
	public String protocol() {
		return (id + " " + userid + " " + protocolString());
	}
	
	protected abstract String protocolString();
}
