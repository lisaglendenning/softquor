package softquor.ai;
import java.util.ListIterator;
import java.util.Vector;

import softquor.board.BoardState;
import softquor.board.Move;
import softquor.board.WalkMove;
import softquor.board.WallMove;
/*
 * Created on Jan 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author shade
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ABStrategy extends Strategy {

	private boolean firstBranch;
	private long totalBranching;
	private long evalTime;
	private long numEval;
	private long leaves;
	private long LEAFCAP = 18000; // ~2 ply
	private long totalDepth;
	private long numCalls;
	private final double WINVALUE = Double.MAX_VALUE;
	private PrincipalVariation oldPV;		
	private FeatureEvaluator eval; 
	private AIBoard board;
	private WeightVector weights;
	
	public ABStrategy(boolean debug) { 
		super(debug);
		restartStats();
	} 
	
	protected void restartStats() {
		firstBranch = true;
		totalBranching = 0;
		evalTime = 0;
		numEval = 0;
		totalDepth = 0;
		numCalls = 0;		
	}
	
	public void printStats() {
		System.out.println("branching=" + totalBranching + 
			", depth=" + totalDepth + ", numCalls=" + numCalls);
	}
	
	public void stats() {
		double avgEval = (double)evalTime / (double)numEval;
		double avgDepth = (double)totalDepth / (double)numCalls;
		System.out.println("Avg eval: " + avgEval);
		System.out.println("Avg depth: " + avgDepth);
	}
	
	/* (non-Javadoc)
	 * @see softquor.ai.Strategy#start(softquor.ai.FeatureEvaluator, softquor.ai.AIBoard, softquor.ai.WeightVector)
	 */
	protected Move start(FeatureEvaluator eval, AIBoard board,
			WeightVector weights) {
		oldPV = new PrincipalVariation();
		this.eval = eval;
		this.board = board;
		this.weights = weights;
		return iterativeAlphaBeta();	
	}
	
	private Move iterativeAlphaBeta() {
		Vector bestMoves = new Vector();
		Move bestMove = null;
		int maxdepth = 1;
		leaves = 0;
		boolean lastMove = false;
		firstBranch = true;
		while(!timeup) {
			if(debug) {
				System.out.println("\nIterative Depth: " + maxdepth);
			}
			PrincipalVariation newPV = alphabeta(0, maxdepth, Double.NEGATIVE_INFINITY, 
						Double.POSITIVE_INFINITY, new PrincipalVariation());
			if(debug) {
				System.out.println("newPV=" + newPV);
			}
			oldPV = newPV;
			if(newPV.value(0) == WINVALUE) {
				bestMove = newPV.move(0);
				if(debug) {
					System.out.println("Winning Final Move: " + bestMove);
				}		
				stop();
				return bestMove;
			}
			//System.out.println("Depth " + maxdepth + " move " + newPV.move(0));
			maxdepth++;
			bestMoves.add(newPV.move(0));
			long branching = 132;
			int leafPlayer = (maxdepth%2 == 0) ? 
					board.getOpponent(board.nextPlayer()): board.nextPlayer();
			if(board.nwalls(leafPlayer) == 0) {
				//System.out.println("No walls");
				branching = 4;
			}
			/*
			if(leaves*branching >= LEAFCAP) {
				lastMove = true;
				if(debug) {
					System.out.println("Leaf cap: " + leaves + 
							" " + branching);
				}
				stop();
				break;
			}*/
			//System.out.println("Depth " + (maxdepth-1) + " Leaves " + leaves);
			leaves = 0;
		}
		totalDepth += maxdepth-1;
		//System.out.println("Final Depth " + (maxdepth-1));
		numCalls++;
		if(bestMoves.size() == 1) {
			bestMove = (Move)bestMoves.get(0);
		}
		else {
			int begin = lastMove ? bestMoves.size()-1 : 
					bestMoves.size()-2;
			
			for( int i=begin;  bestMove==null && i>=0;  i-- ) {
				bestMove = (Move)bestMoves.get(i);
			}
		}
		if(debug) {
			System.out.println("Final Move: " + bestMove);
		}		
		return bestMove;
	}
	
	private PrincipalVariation alphabeta(int depth, int maxdepth, double alpha, double beta,
			PrincipalVariation parentPV) {
		
		if(debug) {		
			System.out.print(board.toText(depth));
			for(int i=0;  i<depth;  i++ ) {
				System.out.print(" ");
			}
			System.out.println("alpha=" + alpha + " beta=" + beta);				
		}
		
		PrincipalVariation bestPV = parentPV;
		BoardState state = board.state();
		bestPV.push(state, null, Double.POSITIVE_INFINITY);
		
		if(board.gameOver()) {
			if(debug) {
				for(int i=0;  i<depth;  i++ ) {
					System.out.print(" ");
				}
				System.out.println("<-- -MaxValue");
			}
			leaves++;
			bestPV.value(depth, -WINVALUE);
			return parentPV;
		}
		
		if(depth == maxdepth) {
			long startTime = System.currentTimeMillis();
			FeatureVector features = eval.eval(board.nextPlayer(), board, weights.eval());
			long stopTime = System.currentTimeMillis();
			//long etime = stopTime - startTime;
			//System.out.println(etime);
			evalTime += stopTime - startTime;
			
			numEval++;
			if(debug) {
				for(int i=0;  i<depth;  i++ ) {
					System.out.print(" ");
				}
				System.out.println("<-- " + features + " * " + weights + " = " + features.apply(weights));
			}
			leaves++;
			bestPV.value(depth, features.apply(weights));
			return parentPV;
		}

		Vector moves = board.legalMoves();
		if(firstBranch) {
			totalBranching += moves.size();
			firstBranch = false;
		}
		// move ordering
		BoardState oldPVState = oldPV.state(depth);
		if(oldPVState!=null && oldPVState.equals(state)) {
			Move prevMove = oldPV.move(depth);
			if(prevMove != null) {
				moves.removeElement(prevMove);
				moves.add(0, prevMove);
			}	
		}
		
		ListIterator lit = moves.listIterator();
		while(!timeup && lit.hasNext()) {
			Move move = (Move)lit.next();
			if(debug) {
				for(int i=0;  i<depth;  i++ ) {
					System.out.print(" ");
				}
				System.out.println(move);				
			}	
			
			board.move(move);
			
			if(!parentPV.contains(board.state())) {
				int childDepth = depth+1;
				PrincipalVariation childPV = 
						alphabeta(childDepth, maxdepth, -beta, -alpha,
								(PrincipalVariation)parentPV.clone());
				childPV.negateValue(childDepth);
				double val = childPV.value(childDepth);
				board.state(state);
				if(debug) {
					for(int i=0;  i<depth;  i++ ) {
						System.out.print(" ");
					}
					System.out.println("val=" + val);				
				}
				
				if((maxdepth == 1)  &&  (val == WINVALUE)) {
					if(debug) {
						for(int i=0;  i<depth;  i++ ) {
							System.out.print(" ");
						}
						System.out.println("<-- Found winning move!");				
					}
					childPV.move(depth, move);
					childPV.value(depth, val);
					return childPV;
				}
				
				if(val!=Double.NEGATIVE_INFINITY && val!=Double.POSITIVE_INFINITY) {
					if (val >= beta) {
						if(debug) {
							for(int i=0;  i<depth;  i++ ) {
								System.out.print(" ");
							}
							System.out.println("<-- cutoff");				
						}
						childPV.move(depth, move);
						childPV.value(depth, val);
						return childPV;
					}
					if (val > alpha) {
						if(debug) {
							for(int i=0;  i<depth;  i++ ) {
								System.out.print(" ");
							}
							System.out.println("alpha=" + val);				
						}
						bestPV = childPV;
						bestPV.move(depth, move);
						bestPV.value(depth, val);
						alpha = val;
					}
				}
			}
			else {
				if(debug) {
					for(int i=0;  i<depth;  i++ ) {
						System.out.print(" ");
					}
					System.out.println("Loop");
				}
				board.state(state);
			}
	    }
		if(debug) {
			for(int i=0;  i<depth;  i++ ) {
				System.out.print(" ");
			}
			System.out.println("<-- alpha=" + alpha + " bestMove=" + bestPV.move(depth));				
		}
	    return bestPV;
	}
	
	class PrincipalVariation implements Cloneable {
		public Vector nodes;
		
		public PrincipalVariation() {
			nodes = new Vector();
		}
		
		public BoardState state(int depth) {
			if(nodes.size() <= depth) {
				return null;
			}
			return ((TreeNode)nodes.get(depth)).state;
		}
		
		public Move move(int depth) {
			if(nodes.size() <= depth) {
				return null;
			}
			return ((TreeNode)nodes.get(depth)).move;
		}
		
		public void move(int depth, Move move) {
			if(nodes.size() > depth) {
				((TreeNode)nodes.get(depth)).move = move;
			}
		}
		
		public double value(int depth) {
			if(nodes.size() <= depth) {
				return Double.POSITIVE_INFINITY;
			}
			return ((TreeNode)nodes.get(depth)).val;
		}
		
		public void value(int depth, double val) {
			if(nodes.size() > depth) {
				((TreeNode)nodes.get(depth)).val = val;
			}
		}
		
		public void negateValue(int depth) {
			TreeNode node = ((TreeNode)nodes.get(depth));
			node.val = -node.val;
		}
		
		public boolean contains(BoardState state) {
			ListIterator lit = nodes.listIterator();
			while(lit.hasNext()) {
				TreeNode next = (TreeNode)lit.next();
				if(next.state.equals(state)) {
					return true;
				}
			}
			return false;
		}
		
		public void push(BoardState state, Move move, double val) {
			nodes.add(new TreeNode(state, move, val));
		}
		
		public String toString() {
			return "(PrincipalVariation[nodes=" + nodes + "])";
		}
		
		public Object clone() {
			PrincipalVariation child = null;
			try {
				child = (PrincipalVariation)super.clone();
				child.nodes = new Vector();
				ListIterator lit = nodes.listIterator();
				while(lit.hasNext()) {
					child.nodes.add(((TreeNode)lit.next()).clone());
				}
			}
			catch(CloneNotSupportedException e) {
				// this shouldn't happen because PrincipalVariation is cloneable						
			}
			return child;
		}
		
		public class TreeNode implements Cloneable {
			public BoardState state;
			public Move move;
			public double val;
			
			public TreeNode(BoardState state, Move move, double val) {
				this.state = state;
				this.move = move;
				this.val = val;
			}
			
			public String toString() {
				return "(TreeNode[state=" + state + "][move=" + move +
					"][val=" + val +"])";
			}
			
			public Object clone() {
				TreeNode child = null;
				try {
					child = (TreeNode)super.clone();
					if(state != null) {
						child.state = (BoardState)state.clone();
					}
					else {
						child.state = null;
					}
					
					if(move != null) {
						if(move instanceof WalkMove) {
							WalkMove wm = (WalkMove)move;
							child.move = (Move)wm.clone();
						}
						else {
							WallMove wm = (WallMove)move;
							child.move = (Move)wm.clone();
						}
					}	
					else {
						child.move = null;
					}
				}
				catch(CloneNotSupportedException e) {
					// this shouldn't happen because TreeNode is cloneable						
				}
				return child;
			}
		}
	}
	
}
