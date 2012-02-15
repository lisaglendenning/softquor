/*
 * Created on Jan 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package softquor;

import java.util.StringTokenizer;

/**
 * @author shade
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConnectMessage extends Message {
	
	public int who;
	
	public ConnectMessage() {}
	
	public ConnectMessage(int who) {
		this.who = who;
	}
	
	protected String protocolString() {
		return ("connect " + who);
	}
	
	public void parse(StringTokenizer rest) {
		who = Integer.valueOf(rest.nextToken()).intValue();
	}

}
