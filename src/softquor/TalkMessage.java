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
public class TalkMessage extends Message {

	public String message;
	
	public TalkMessage() {}
	
	
	public TalkMessage(String message) {
		this.message = message;
	}
	
	/* (non-Javadoc)
	 * @see softquor.Message#protocolString()
	 */
	protected String protocolString() {
		return ("talk " + message);
	}

	/* (non-Javadoc)
	 * @see softquor.Message#parse(java.util.StringTokenizer)
	 */
	public void parse(StringTokenizer rest) throws Exception {
		StringBuffer buff = new StringBuffer();
		while(rest.hasMoreTokens()) {
			buff.append(rest.nextToken() + " ");
		}
		buff.deleteCharAt(buff.length()-1);
		message = buff.toString();
	}

}
