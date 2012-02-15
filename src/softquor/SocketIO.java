/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package softquor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;


/**
 * @author shade
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SocketIO {
	public int id;
	public Socket socket;
	public PrintWriter out;
	public BufferedReader in;
	
	public SocketIO(int id, Socket socket) throws IOException {
		this.id = id;
		this.socket = socket;
		this.out = new PrintWriter(socket.getOutputStream(), true);
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public void startTimer(int timeout) {
		try {
			socket.setSoTimeout(timeout);
		}
		catch(SocketException e) {}
	}
	
	public void cancelTimer() {
		try {
			socket.setSoTimeout(0);
		}
		catch(SocketException e) {}		
	}
	
	public String read() throws IOException {
		synchronized(in) {
			return in.readLine();
		}
	}
	
	public void write(String str) {
		synchronized(out) {
			out.println(str);
			out.flush();
		}
	}
	
	public synchronized void close() throws IOException {
		if(socket != null) {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
			socket = null;
			in.close();
			out.close();
		}
	}
}
