/*
 * Created on Nov 1, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package softquor;
import java.util.Vector;

import softquor.board.Board;
import softquor.board.Move;
import softquor.board.BoardState;

/**
 * @author lglenden
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Quoridor {

	protected Board board;
	protected int timelimit;
	protected Vector messageQueue;

	public Quoridor() {
		board = null;
		timelimit = 1000;
		messageQueue = new Vector();
	}
	
	public Board board() {
		return board;
	}
	
	public int timelimit() {
		return timelimit;
	}
	
	public Message query() {
		if(messageQueue.isEmpty()) {
			return null;
		}
		return (Message)messageQueue.remove(0);
	}
	
	protected void queue(Message message) {
		messageQueue.add(messageQueue.size(), message);
	}
	
	public void tell(Message message) {
		if(message instanceof InitMessage) {
			init((InitMessage)message);
		}
		else if(message instanceof QuitMessage) {
			quit((QuitMessage)message);
		}
		else if(message instanceof RegisterMessage) {
			register((RegisterMessage)message);
		}
		else if(message instanceof StartMessage) {
			start((StartMessage)message);
		}		
		else if(message instanceof MoveMessage) {
			move((MoveMessage)message);		
		}
	}
	
	protected void init(InitMessage message) {
		try {
			board = new Board(message.rows, message.cols, message.walls);
			timelimit = message.timelimit;
			queue(message);
		}
		catch(GameException e) {
			queue(new ExceptionMessage(e));
		}
	}
	
	protected void quit(QuitMessage message) {
		board = null;
	}

	protected void register(RegisterMessage message) {
		try {
			int id = message.gameid;
			if(id == -1) {
				id = 1;
				while(board.player(id) != null) {
					id++;
				}
				message.gameid = id;
			}
			board.register(id);
			queue(message);
		}
		catch(GameException e) {
			queue(new ExceptionMessage(e));
		}
	}
	
	protected void start(StartMessage message) {
		try {
			if(board.phase() == BoardState.PREGAME) {
				board.start(message.first);
			}
			else {
				board.restart(message.first);
			}
			queue(message);
			int nextPlayer = board.nextPlayer();
			TurnMessage turn = new TurnMessage(nextPlayer, timelimit);
			queue(turn);
		}
		catch(GameException e) {
			queue(new ExceptionMessage(e));
		}
	}
	
	protected void move(MoveMessage message) {
		Move nextMove = message.move;
		boolean gameOver = board.move(nextMove);
		int nextPlayer = board.nextPlayer();
		queue(message);
		Message nextMessage = null;
		if(!gameOver) {
			nextMessage = new TurnMessage(nextPlayer, timelimit);
		}
		else {
			nextMessage = new WinMessage(nextPlayer);
		}
		queue(nextMessage);
	}
	
}
