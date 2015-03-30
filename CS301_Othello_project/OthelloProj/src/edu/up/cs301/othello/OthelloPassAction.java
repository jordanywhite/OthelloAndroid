package edu.up.cs301.othello;

import edu.up.cs301.game.GamePlayer;
import edu.up.cs301.game.actionMsg.GameAction;

/**
 * An empty move meant only to give the turn to the next player
 * 
 * @author Jordan White
 * @version 11/10/2013
 */
public class OthelloPassAction extends GameAction {
    static final long serialVersionUID = 1L;

    public OthelloPassAction(GamePlayer player) {
        super(player);
    }

}
