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
public class WinMessage extends Message {
	public int winner;
	
	public WinMessage() {}
	
	public WinMessage(int winner) {
		this.winner = winner;
	}
	
	public void parse(StringTokenizer tok) {
		winner = Integer.valueOf(tok.nextToken()).intValue();
	}
	
	protected String protocolString() {
		return ("win " + winner);
	}
}
