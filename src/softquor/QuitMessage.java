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
public class QuitMessage extends Message {
	public String reason;
	
	public QuitMessage() {}
	
	public QuitMessage(String message) {
		reason = message;
	}
	
	public void parse(StringTokenizer tok) {
		StringBuffer buff = new StringBuffer();
		while(tok.hasMoreTokens()) {
			buff.append(tok.nextToken() + " ");
		}
		buff.deleteCharAt(buff.length()-1);
		reason = buff.toString();
	}
	
	protected String protocolString() {
		return ("quit " + reason);
	}
}
