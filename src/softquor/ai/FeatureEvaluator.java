/*
 * Created on Sep 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package softquor.ai;

/**
 * @author lglenden
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FeatureEvaluator {
	
	public static int NFEATURES = 10;
	public static int NRELAX = 10;
	
	public FeatureVector eval(int playerid, AIBoard board, boolean[] eval) {
		FeatureVector fv = new FeatureVector();
		int oppid = board.getOpponent(playerid);
		double[] features = new double[NFEATURES];
		
		int feature = 0;
		if(eval[feature]) {
			features[feature] = shortestPath(playerid, board);
		}
		feature++;
		if(eval[feature]) {
			features[feature] = shortestPath(oppid, board);
		}
		feature++;
		if(eval[feature]) {
			features[feature] = markovChain(playerid, board);
		}
		feature++;
		if(eval[feature]) {
			features[feature] = markovChain(oppid, board);
		}
		feature++;
		if(eval[feature]) {
			features[feature] = manhatten(playerid, board);
		}
		feature++;
		if(eval[feature]) {
			features[feature] = manhatten(oppid, board);
		}
		feature++;		
		if(eval[feature]) {
			features[feature] = pawnDistance(playerid, board);
		}
		feature++;
		if(eval[feature]) {
			features[feature] = goalSide(playerid, board);
		}
		feature++;
		if(eval[feature]) {
			features[feature] = goalSide(oppid, board);
		}
		feature++;
		if(eval[feature]) {
			features[feature] = numWalls(playerid, board);
		}
		feature++;
		
		fv.features(features);
		return fv;
	}
	
	public int numberFeatures() {
		return NFEATURES;
	}
	
	public double shortestPath(int playerid, AIBoard board) {
		double longest = board.rows()*board.cols();
		double path = board.shortestPath(playerid);
		return (longest-path)/longest;
	}
	
	public double relaxation(int playerid, AIBoard board, int iterations) {
		return board.relaxation(playerid, iterations);
	}
	
	public double markovChain(int playerid, AIBoard board) {
		return board.markovChain(playerid);
	}
	
	public double manhatten(int playerid, AIBoard board) {
		double longest = board.rows();
		if(board.cols() > longest) {
			longest = board.cols();
		}
		return (longest-board.manhatten(playerid))/longest;
	}
	
	public double pawnDistance(int playerid, AIBoard board) {
		double longest = board.rows()*board.cols();
		return (longest-board.pawnDistance())/longest;
	}
	
	public double goalSide(int playerid, AIBoard board) {
		return board.goalSide(playerid);
	}
	
	public double numWalls(int playerid, AIBoard board) {
		double totalWalls = board.nwalls()/board.numPlayers();
		double walls = board.nwalls(playerid);
		return (walls/totalWalls);
	}

}
