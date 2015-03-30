package edu.up.cs301.othello;

import java.util.ArrayList;

import edu.up.cs301.game.GameMainActivity;
import edu.up.cs301.game.GamePlayer;
import edu.up.cs301.game.LocalGame;
import edu.up.cs301.game.config.GameConfig;
import edu.up.cs301.game.config.GamePlayerType;

/**
 * this is the primary activity for Othello game
 * 
 * @author Chandler Underwood
 * @author Taylor Spooner
 * @author Jordan White
 * @author Stephen Robinson
 * @version November 2013
 */
public class OthelloMainActivity extends GameMainActivity {
	
	public static final int PORT_NUMBER = 5213;

	/**
	 * an Othello game has a two player max. The default is human vs. computer
	 */
	@Override
	public GameConfig createDefaultConfig() {

		// Define the allowed player types
		ArrayList<GamePlayerType> playerTypes = new ArrayList<GamePlayerType>();
		
		// yellow-on-blue GUI
		playerTypes.add(new GamePlayerType("Local Human Player") {
			public GamePlayer createPlayer(String name) {
				return new OthelloHumanPlayer(name);
			}
		});
		
		
		// dumb computer player
		playerTypes.add(new GamePlayerType("Computer Player (dumb)") {
			public GamePlayer createPlayer(String name) {
				return new DumbOthelloComputerPlayer(name);
			}
		});
		
		// smarter computer player
		playerTypes.add(new GamePlayerType("Computer Player (smart)") {
			public GamePlayer createPlayer(String name) {
				return new SmartOthelloComputerPlayer(name);
			}
		});
		
		// try-to-lose computer player
		playerTypes.add(new GamePlayerType("Computer Player (trys to lose)") {
			public GamePlayer createPlayer(String name) {
				return new SuicideOthelloComputerPlayer(name);
			}
		});

		// Create a game configuration class for Tic-tac-toe
		GameConfig defaultConfig = new GameConfig(playerTypes, 2,2, "Othello", PORT_NUMBER);

		// Add the default players
		defaultConfig.addPlayer("Human", 0); // Human player
		defaultConfig.addPlayer("Computer", 1); // dumb computer player

		// Set the initial information for the remote player
		defaultConfig.setRemoteData("Remote Player", "", 0); 
		
		//done!
		return defaultConfig;
		
	}//createDefaultConfig


	/**
	 * createLocalGame
	 * 
	 * Creates a new game that runs on the server tablet,
	 * 
	 * @return a new, game-specific instance of a sub-class of the LocalGame
	 *         class.
	 */
	@Override
	public LocalGame createLocalGame() {
		return new OthelloLocalGame();
	}

}
