/*
 * Created on Nov 1, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package softquor;

import java.util.StringTokenizer;

/**
 * @author lglenden
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TurnMessage extends Message {
	public int next;
	public int timelimit;
	
	public TurnMessage() {}
	
	public TurnMessage(int nextPlayer, int limit) {
		next = nextPlayer;
		timelimit = limit;
	}
	
	public void parse(StringTokenizer tok) {
		next = Integer.valueOf(tok.nextToken()).intValue();
		timelimit = Integer.valueOf(tok.nextToken()).intValue();
	}
	
	protected String protocolString() {
		return ("turn " + next + " " + timelimit);
	}
}
