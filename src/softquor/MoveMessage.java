/*
 * Created on Nov 1, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package softquor;

import softquor.board.Move;
import java.util.StringTokenizer;

/**
 * @author lglenden
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MoveMessage extends Message {
	public Move move;
	
	public MoveMessage() {}
	
	public MoveMessage(Move nextMove) {
		move = nextMove;
	}
	
	public void parse(StringTokenizer tok) {
		move = Move.parse(tok);
	}
	
	protected String protocolString() {
		return ("move " + move.protocol());
	}
}
