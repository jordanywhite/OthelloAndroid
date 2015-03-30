package edu.up.cs301.othello;

import edu.up.cs301.game.GamePlayer;
import edu.up.cs301.game.actionMsg.GameAction;

/**
 * An empty move meant to reset the game
 * 
 * @author Taylor Spooner
 * @version 11/26/2013
 */

public class OthelloRestartAction extends GameAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7880460147210017155L;

	public OthelloRestartAction(GamePlayer player) {
		super(player);
	}
	
	
}
