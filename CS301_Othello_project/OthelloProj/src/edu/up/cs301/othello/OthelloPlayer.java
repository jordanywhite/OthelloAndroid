package edu.up.cs301.othello;

import edu.up.cs301.game.GamePlayer;

/**
 * An interface that defines a Othello game player.
 * 
 * @author Whoever wants credit
 * @version 11/10/2013
 */
public interface OthelloPlayer extends GamePlayer
{
    // There are no additional methods for those player beyond those in
    // GamePlayer.
	
	public CellState[] color = {CellState.BLACK, CellState.WHITE};
}

