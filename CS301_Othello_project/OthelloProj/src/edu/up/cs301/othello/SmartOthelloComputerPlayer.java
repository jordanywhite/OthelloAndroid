package edu.up.cs301.othello;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;

import edu.up.cs301.game.infoMsg.GameInfo;
import android.graphics.Point;

/**
 * A computer ai player which analysis the game tree of possible worlds 
 * and selects the ideal branch.
 * 
 * @author Stephen Robinson
 * @version November 2013
 * 
 */
public class SmartOthelloComputerPlayer extends OthelloComputerPlayer
{
    /**The number of turns (including opponent turns) to look ahead.*/
    public final int MOVES_LOOKAHEAD = 4;

    /**This is the minimum capacity to hold the buds for the gametree.
     * It is based on statistical average and is meant to stop constant
     * reallocation of memory.
     */
    public final int CAPACITY = 30000;
        
        
    //These are the weights to multiply by the different criterion for
    //quantifying a gamestate. All weights will be negative for the opponent's
    //turn. Large positive is good, large negative is bad.
    
    /**Score.*/
    public final double SCORE = 2.;

    /**Captured Corners. Number of captured corners*/
    public final double CORNER = 600.; //Always want this unless it costs the game.

    //Not used
    ///**Move count. How many possible moves the next turn will have.*/
    //public final double MOVES = -1.;

    ///**Pass. If this move is a pass.*/
    //public final double PASS = -25.; 

    ///**Victory. If this state is a winner.*/
    //public final double VICTORY = 2500.;

	/**
	 * Constructor for SmartOthelloComputerPlayer
	 * 
	 * @param name the player name
	 */
	public SmartOthelloComputerPlayer(String name) {
		super(name);
	}//ctor

    /**
     * A class which represents a node in the game tree.
     * holds a reference to Any number of child nodes,
     * the move performed to get from the parent state to this state
     * and the possible world OthelloState.
     */
     class Node{
        private ArrayList<Node> children;
        private OthelloState state;
        private int[] move;

        public Node(Node parent, int[] move, OthelloState state)
        {
            this.state = state;
            this.children = new ArrayList<Node>();

            this.move = new int[2];
            if (move != null && move.length == 2){
                this.move[0] = move[0];
                this.move[1] = move[1];
            } else {
                this.move = null;
            }

            //Add this node to its parent's list of children.
            if(parent != null){
	            parent.addChild(this);
            }
        } //ctor

        /**Add a child to this node. Called by child's ctor.*/
        protected void addChild(Node child){
            if(child != null){
                children.add(child);
            }
        } //addChild
        
        /**Get the leaf nodes of this tree. Leaf nodes are the ones without
         * any child branches...
         * @return an ArrayList<Node> of leaf nodes.
         */
        public ArrayList<Node> getLeaves(){
        	ArrayList<Node> leaves = new ArrayList<Node>();
        	if(children.size() > 0){ //Not a leaf. Go down farther
	        	for(Node child: children){
	        		leaves.addAll(child.getLeaves());
	        	}
        	} else { //This is a leaf.
        		leaves.add(this);
        	}
            return leaves;
        } //getLeaves

        public int[] getMove(){
            return move;
        }
        public ArrayList<Node> getChildren(){
            return children;
        }
        public OthelloState getState(){
            return state;
        }
    } //class Node

    /**
     * Quantify how ideal a particular state is.
     * 
     * @return a double representing how ideal the state is for the computer player.
     */
    protected double quantifyState(OthelloState state){
        double stateScore = 0.;
       
        //Tally up points
        int points = state.getWhiteScore() - state.getBlackScore();
        points *= this.playerNum == 1 ? 1 : -1;
        
        stateScore += points*SCORE;
       
        //Detect captured corners
        int corners = 0;
        for(int x=0; x<4; ++x){
        	CellState piece = state.getDisk((x%2)*(OthelloState.BOARD_HEIGHT-1),
        			                        (x/2)*(OthelloState.BOARD_WIDTH-1));
        	if(piece != null && !piece.equals(CellState.EMPTY)){
        		if(piece.equals(color[playerNum])){
        			corners += 1;
        		} else {
        			corners -= 1;
        		}
        	}
        }
        
        stateScore += corners*CORNER;
     
        /*Taken out for speed. ListLegalMoves is too costly.
         *It is far better to use the time to look another move ahead*/
        /*
        int moves = state.listLegalMoves().size();
        //Detect if this move is a pass
        if (moves == 0){
        	stateScore += state.getTurn() == this.playerNum ? PASS : -PASS;
        
        	//Detect victory/loss
        	state.setTurn(1-state.getTurn());
        	if (state.listLegalMoves().size() == 0){ //Game over state
        		if (points > 0){//victory
        			stateScore += VICTORY;
        		} else if (points < 0){ //loss
        			stateScore -= VICTORY;
        		} else {
        			//ignoring ties.
        		}
        	}
        	state.setTurn(1-state.getTurn()); //set turn back
        }
        
        //Take into consideration how many moves this state has
        stateScore += state.getTurn() == this.playerNum ? moves * MOVES : -moves * MOVES;
        */

        //TODO: remove, debug
        if(state == this.state){
        	System.err.println("My corner score:" + corners*CORNER);
        }
        
        return stateScore;
    } // quantifyState
   
    /**Calculates the average score of leaf nodes of the tree passed in.
     * 
     * @param root the root of the tree.
     * @return the average score of the leaf nodes.
     */
    private double avgNodeScore(Node root){
    	double score = 0.;
    	int count = 0;
    	for (Node leaf: root.getLeaves()){
    		++count;
    		score += quantifyState(leaf.getState());
    	}
    	return score/(double)count;
    }
    
    
    /**
     * Called when the state has been updated and it is this computer player's turn.
     */
    @Override
    protected void makeMove(){

        //These are nodes which have not had their children generated yet.
        ArrayList<Node> buds = new ArrayList<Node>();
        
        //Lets give the ArrayList a large section of memory, because we will need it!
        buds.ensureCapacity(CAPACITY);
        
        //Generate the tree ==================================

        //The root node just contains a copy of the current state.
        Node root = new Node(null, null, new OthelloState(this.state));
        buds.add(root);

        for (int i=0; i<MOVES_LOOKAHEAD; ++i){
            for (Node bud: new ArrayList<Node>(buds)){

                //If the bud had no legal moves then it is a pass.
                if (bud.getState().listLegalMoves().size() == 0){
	                    Node branch = new Node(bud, null, new OthelloState(bud.getState()));
	                    buds.add(branch);
                } else {
	                //create a node for each legal move. 'sprout' the bud.
	                for (int[] move: bud.getState().listLegalMoves()){
	
	                    //make a new possible world state with the move performed
	                    OthelloState newState = new OthelloState(bud.getState());
	                    newState.playDisk(move[0], move[1]);
	
	                    Node branch = new Node(bud, move, newState);
	                    buds.add(branch);
	                }
                }
               
                //remove the sprouted bud from the list.
                buds.remove(bud);
            }
        }

        //Total up the scores for each possible next move.
        //This looks at all possible next moves (The immediate child
        //branches of the root) and computes the average score of their
        //leaf children (branches with no children at the top of the tree).
        //Takes the branch with the best average leaf score.
        Node bestBranch = null;
        Double bestScore = null;
        
        for(Node branch: root.getChildren()){
        	double score = avgNodeScore(branch);
        	if((bestScore == null || bestScore < score) && branch.getMove() != null){
        		bestScore = score;
        		bestBranch = branch;
        	}
        }

        //Send the game action based on the branch
        int row = 0;
        int col = 0;
        
        if (bestBranch != null){
        	row = bestBranch.getMove()[0];
        	col = bestBranch.getMove()[1];
        }
        
        if (!state.isLegalMove(row, col)){
        	//This shouldn't happen, but if it does, select a random move.
        	//In testing this hasn't happened, but it never hurts to check and
        	//it may prevent a rare bug from crashing a game.
	        ArrayList<int[]> moves = state.listLegalMoves();
	        int choice = (int) (Math.random() * (moves.size()));
	        
	        //TODO: remove: debug
	        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	        System.err.println("NO MOVE FOUND ================================");
	        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	        System.err.println("I WANTED TO MOVE: " + col + ", " + row);
	        row = moves.get(choice)[0];
	        col = moves.get(choice)[1];
        } else {
        	//Ready to send the move.

	        //TODO: remove: debug
        	System.err.println("I MOVE =======================================");
	        System.err.println("MOVE: " + col + ", " + row);
	        System.err.println("BRANCH CONFIDENCE: " + bestScore);
	        System.err.println("HAPPYNESS: " + quantifyState(bestBranch.getState()));
	        quantifyState(this.state);
	        this.game.sendAction(new OthelloMoveAction(this, row, col));
        }
    } //makeMove

}


