
package edu.up.cs301.othello;

import edu.up.cs301.game.GamePlayer;
import edu.up.cs301.game.LocalGame;
import edu.up.cs301.game.actionMsg.GameAction;

/**
 * The OthelloLocalGame class. Accepts actions from players and maintains the
 * official game state.
 * 
 * @author Stephen Robinson
 * @author Jordan White
 * @version 11/10/2013
 */

public class OthelloLocalGame extends LocalGame implements OthelloGame {

    // the game's state
    private OthelloState state;

    /**
     * Constructor for OthelloLocalGame
     */
    public OthelloLocalGame() {
        super();

        // Make the official state.
        this.state = new OthelloState();
    } // ctor

    /**
     * Determine if the game is over. If it is, returns a string with victory
     * message, otherwise returns null
     * 
     * @return the victory message string or null if the game is not over
     */
    @Override
    public String checkIfGameOver() {
    	// the player number of the current turn
        int playerNo = this.state.getTurn();

        try {
            // check if there are still legal moves for current player
            if (this.state.listLegalMoves().size() != 0) {
                return null;
            }

            // check for the other player
            this.state.setTurn(1 - playerNo);
            if (this.state.listLegalMoves().size() != 0) {
                return null;
            }

        } finally {
            // make absolutely sure to make sure that the correct player turn
            // number is maintained.
            this.state.setTurn(playerNo);
        }

        // Has not returned yet so the game is over.

        if (this.state.getBlackScore() > this.state.getWhiteScore()) {
            return "The winner is: " + this.playerNames[0] + "!";
        } else if (this.state.getBlackScore() != this.state.getWhiteScore()) {
            return "The winner is: " + this.playerNames[1] + "!";
        } else {
            return "Its a tie!";
        }

    } // checkIfGameOver

    /**
     * Notify the specified player of the new gamestate. Because this is a
     * perfect information game, the player is given a copy of the entire state.
     * 
     * @param p the player to update
     */
    @Override
    protected void sendUpdatedStateTo(GamePlayer p) {
        // Make a copy of the state and send it to the player.
        p.sendInfo(new OthelloState(this.state));
    } // sendUpdatedStateTo

    /**
     * Determine whether the given player may move.
     * 
     * @param playerNo the player number (1 or 0)
     * @return true if the player may move else false.
     */
    public boolean canMove(int playerNo) {
        return this.state.getTurn() == playerNo;
    }// canMove

    /**
     * Make a move or pass requested by a player. Checks the official state to
     * determine if it is the player's turn and the move/pass is legal for that
     * player.
     * 
     * @param action The GameAction the player sent.
     * @return True if the move was legal else false.
     */
    @Override
    public boolean makeMove(GameAction action){

        // Pass Action
        if (action instanceof OthelloPassAction) state.setTurn(1 - state.getTurn());

        // Move Action
        else if (action instanceof OthelloMoveAction) {
            OthelloMoveAction OMove = (OthelloMoveAction) action;

            // check if this move is legal according to the official game state
            if (!this.state.isLegalMove(OMove.getRow(), OMove.getCol())) {
                return false;
            }

            // update the official game state
            this.state.playDisk(OMove.getRow(), OMove.getCol());
        }
        
        //restart action
        else if (action instanceof OthelloRestartAction) {
        	//create a new state.
        	state = new OthelloState();
        	
        }

        return true;
    }// makeMove

}// class OthelloLocalGame

