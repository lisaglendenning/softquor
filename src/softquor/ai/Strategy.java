/*
 * Created on Sep 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package softquor.ai;
import softquor.board.*;
import java.util.Timer;

/**
 * @author lglenden
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class Strategy {
	
	public boolean timeup;
	protected Timer timer;
	protected boolean debug;
	
	public Strategy(boolean debug) {
		timeup = false;
		timer = new Timer();
		this.debug = debug;
	}
	
	public boolean debug() {
		return debug;
	}
	
	public void stop() {
		timer.cancel();
		timer = new Timer();
	}
	
	public void restart() {
		timeup = false;
		timer = new Timer();
		restartStats();
	}
	
	public Move nextMove(FeatureEvaluator eval, AIBoard board,
	    WeightVector weights, long timelimit) {
		timeup = false;
		if(debug) {
			System.out.println(timelimit + " milliseconds...");
		}
		timer.schedule(new StrategyTimer(this), timelimit);
		Move move = start(eval, board, weights);
		return move;
	}
	
	public abstract void printStats(); 
	
	protected abstract void restartStats();
	
	protected abstract Move start(FeatureEvaluator eval, AIBoard board, WeightVector weights);

}
