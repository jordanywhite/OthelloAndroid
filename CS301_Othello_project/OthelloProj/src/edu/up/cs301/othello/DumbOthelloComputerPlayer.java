
package edu.up.cs301.othello;

import java.util.ArrayList;


/**
 * This ai simply makes a random legal move after a delay.
 * 
 * @author Stephen Robinson
 * @versio2 November 2013
 */
public class DumbOthelloComputerPlayer extends OthelloComputerPlayer
{
    /*
     * DumbOthelloComputerPlayer constructor
     */
    public DumbOthelloComputerPlayer(String name) {
        super(name);
    }

    /**
     * Called when the state has been updated and it is this computer player's
     * turn. For this ai, a random move is selected.
     */
    @Override
    protected void makeMove() {
    	
    	//sleep so it looks like the computer is deciding its move...
    	sleep(1200);

    	//Get all possible legal moves for this turn
        ArrayList<int[]> moves = state.listLegalMoves();
       
        //Select a random legal move and send it to the game.
        int choice = (int) (Math.random() * moves.size());
        
        int x = moves.get(choice)[0];
        int y = moves.get(choice)[1];

        this.game.sendAction(new OthelloMoveAction(this, x, y));
    }
} // class DumbOthelloComputerPlayer
