/*
 * Created on Sep 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package softquor.ai;
import softquor.board.*;
import java.io.IOException;

/**
 * @author lglenden
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Agent {
	private int id;
	private FeatureEvaluator evaluator;
	private Strategy strategy;
	private WeightVector weights;
	private GameLog log;
	
	public Agent(FeatureEvaluator evaluator, Strategy strategy, WeightVector weights) {
		init(-1, evaluator, strategy, weights);
	}
	
	public Agent(int id, FeatureEvaluator evaluator, Strategy strategy, WeightVector weights) {
		init(id, evaluator, strategy, weights);
	}
	
	public void restart() {
		strategy.restart();
		log.restart();
	}
	
	public void stop() {
		strategy.stop();
	}
	
	private void init(int id, FeatureEvaluator evaluator, Strategy strategy, WeightVector weights) {
		this.id = id;
		this.evaluator = evaluator;
		this.strategy = strategy;
		this.weights = weights;
		this.log = new GameLog();		
	}
	
	public int id() {
		return id;
	}
	
	public void id(int id) {
		this.id = id;
	}
	
	public void stats() {
		((ABStrategy)strategy).stats();
	}
	/*
	public void logBoard(AIBoard board, int playerid) {
		// log curernt feature vector
		FeatureVector fv = evaluator.eval(playerid, board, weights.eval());
		log.feature(fv);
	}*/
	
	public Move turn(AIBoard board, int timelimit) {
		//generate next move
		Move nextMove = strategy.nextMove(evaluator, board, weights, timelimit);
		return nextMove;
	}
	/*
	public void learn(boolean win) throws java.io.IOException {
		Vector fvs = log.features();
		//int turns = fvs.size();
		double gamma = 0.1;
		FeatureVector sum = new FeatureVector();
		int g = 1;
		for( int i=fvs.size()-1; i>=0;  i--, g++ ) {
			FeatureVector next = (FeatureVector)fvs.get(i);
			next.mult(Math.pow(gamma, (double)g));
			if(i==fvs.size()-1) {
				double[] features = new double[next.features().length];
				for( int f=0;  f<features.length;  f++ ) {
					features[f] = next.features()[f];
				}
				sum.features(features);
			}
			else {
				sum.add(next);
			}
		}
		
		if(!win) {
			sum.mult(-1);			
		}
		//System.out.println(weights);
		//System.out.println(sum);
		weights.add(sum.features());
		//System.out.println(weights);
		weights.output();
	}*/
	
	public void logError(String message) {
		message += "\n";
		String weightFile = weights.file();
		int extension = weightFile.lastIndexOf('.');
		String logFile = weightFile.substring(0, extension) + ".log";
		try {
			log.output(logFile, message);
		}
		catch(IOException e) {}
	}
	/*
	public void logGame(int opponent, boolean win) {
		int w = win? 1 : 0;
		String weightFile = weights.file();
		int extension = weightFile.lastIndexOf('.');
		String logFile = weightFile.substring(0, extension) + ".log";
		String message = "[" + weights.toString() + "][" + opponent + "][" + w + "]\n";
		try {
			log.output(logFile, message);
		}
		catch(IOException e) {}
	}*/
}
