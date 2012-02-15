/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package softquor;

import softquor.ai.AIBoard;

/**
 * @author shade
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AIQuoridor extends Quoridor {
	protected void init(InitMessage message) {
		try {
			board = new AIBoard(message.rows, message.cols, message.walls);
			timelimit = message.timelimit;
			queue(message);
		}
		catch(GameException e) {
			queue(new ExceptionMessage(e));
		}
	}
}
