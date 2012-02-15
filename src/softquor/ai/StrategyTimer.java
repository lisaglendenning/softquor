/*
 * Created on Nov 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package softquor.ai;
import java.util.TimerTask;

/**
 * @author Lisa
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StrategyTimer extends TimerTask {
	private Strategy strategy;
	
	public StrategyTimer(Strategy strategy) {
		this.strategy = strategy;		
	}
	
	public void run() {
		if(strategy.debug()) {
			System.out.println("Time's up!");
		}
		strategy.timeup = true;
	}
}
