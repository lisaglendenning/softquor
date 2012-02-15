/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package softquor;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * @author shade
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SocketThread extends Thread {
	public Client client;
	public SocketIO socket;
	
	public SocketThread(Client client, SocketIO socket) {
		this.client = client;
		this.socket = socket;
	}

	public void run() {
		String inputLine;
		Message message;

		try {
			while ((inputLine = socket.read()) != null) {
				message = Message.createMessage(inputLine);
				if(message != null) {
					client.socketMessage(message);
				}
	    	}
	    }
		catch(SocketTimeoutException e) {
			message = new ExceptionMessage("Timeout: " + Server.TIMEOUT + " ms");
			message.from = Client.SERVER;
			message.to = socket.id;
			socket.write(message.protocol());
			try {
				socket.close();
			}
			catch(IOException io) {
			}	
		}
		catch(IOException e) {
			client.socketError();
			return;
		}
		client.socketClose();
	}
}
