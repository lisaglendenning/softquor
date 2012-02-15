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
public class RegisterMessage extends Message {
	public int gameid;
	
	public RegisterMessage() {}
	
	public RegisterMessage(int gameid) {
		this.gameid = gameid;
	}
	
	public void parse(StringTokenizer tok) {
		gameid = Integer.valueOf(tok.nextToken()).intValue();
	}
	
	protected String protocolString() {
		return ("register " + gameid);
	}
}
