/*
 * Created on Feb 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package softquor;

/**
 * @author Lisa
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ClientEvent {
	protected Client source;
	protected String command;
	
	public ClientEvent(Client source, String command) {
		this.source = source;
		this.command = command;
	}
	
	public Client getSource() {
		return source;
	}
	
	public String getCommand() {
		return command;
	}
}
