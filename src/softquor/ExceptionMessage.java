/*
 * Created on Jan 11, 2005
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
public class ExceptionMessage extends Message {
	public String exception;
	
	public ExceptionMessage() {}
	
	public ExceptionMessage(String message) {
		exception = message;
	}
	
	public ExceptionMessage(Exception e) {
		exception = e.getMessage();
	}
	
	public void parse(StringTokenizer rest) {
		StringBuffer buff = new StringBuffer();
		while(rest.hasMoreTokens()) {
			buff.append(rest.nextToken() + " ");
		}
		buff.deleteCharAt(buff.length()-1);
		exception = buff.toString();
	}
	
	protected String protocolString() {
		return "exception " + exception;
	}
}
