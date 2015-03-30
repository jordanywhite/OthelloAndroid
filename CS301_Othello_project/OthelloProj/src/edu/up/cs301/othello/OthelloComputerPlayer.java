package edu.up.cs301.othello;

import java.util.ArrayList;


import android.app.Activity;
import android.app.AlertDialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.TextView;
import edu.up.cs301.animation.AnimationSurface;
import edu.up.cs301.animation.Animator;
import edu.up.cs301.game.GameComputerPlayer;
import edu.up.cs301.game.GameMainActivity;
import edu.up.cs301.game.R;
import edu.up.cs301.game.infoMsg.GameInfo;



/**
 * An abstract class which defines an Othello computer player. It also contains the
 * code to allow the computer player to draw a gui if needed.
 * 
 * @author Stephen Robinson
 * @version November 2013
 */

public abstract class OthelloComputerPlayer extends GameComputerPlayer implements OthelloPlayer, Animator {

	protected OthelloState state;

	//If this player is responsible for drawing the gui.
	private boolean isGui = false;

	// instance variables used in drawing pieces onto game-board
	private static final int SQUARE_SIZE = 78;
	private static final int BORDER_SIZE = 13;
	private static final int PADDING = 10;
	private static final int DISC_SIZE = 60;
	private static final int HINT_SIZE = 40;

	// the current activity
	private Activity myActivity;

	// bitmap used to draw the background of the othello game-board
	private Bitmap background;

	// bitmaps used to draw othello discs
	private Bitmap whiteDisc;
	private Bitmap blackDisc;

	// text views used in the GUI
	private TextView currentTurnDisplay;
	private TextView blackScore;
	private TextView whiteScore;

	// Strings used in text-views
	private static final String whiteScoreString = "White Score:\n\t\t\t\t ";
	private static final String blackScoreString = "Black Score:\n\t\t\t\t ";
	private static final String turnString = "Turn:\n";

	private Handler myHandler;

	private AlertDialog noState;

	private boolean noStateShown = false;


	// our animation surface. (We're not actually doing moving animation, but this
	// surface is a convenient way to draw our image.)
	private AnimationSurface surface;

	/**
	 * OthelloComputerPlayer constructor
	 */
	public OthelloComputerPlayer(String name) {
		super(name);
	}//ctor

	/**
	 * called when the state has been updated and it is this computer player's turn.
	 */
	abstract protected void makeMove();

	/**
	 * Called when computer player receives information from the game.
	 * 
	 * @param info the info message from the game
	 */
	@Override
	protected void receiveInfo(GameInfo info) {

		//update the turn and points display
		if (this.isGui && state != null){

			myHandler.post(
				new Runnable() {
					public void run() {
						whiteScore.setText(whiteScoreString + state.getWhiteScore());
						blackScore.setText(blackScoreString + state.getBlackScore());
						currentTurnDisplay.setText(turnString + allPlayerNames[state.getTurn()]);
					}
				}
			);
		}

		//if this is a new updated state.
		if (info instanceof OthelloState){
			this.state = (OthelloState)info;

			//make sure it is our turn.
			if (this.state.getTurn() == this.playerNum){

				//Get all possible legal moves for this turn
				ArrayList<int[]> moves = state.listLegalMoves();

				//If there are no legal moves then pass.
				if(moves.size() == 0) {
					//sleep so it looks like the computer is deciding its move...
					sleep(1200);

					this.game.sendAction(new OthelloPassAction(this));
				} else {
					//tell the implementing class to make a move with the updated state.
					this.makeMove();
				}
			}
		}
	} //recieveInfo

	/**
	 * @return true because this supports the gui
	 */
	@Override
	public boolean supportsGui(){
		return true;
	}

	/**
	 * sets this computer player as the activity's GUI
	 */
	public void setAsGui(GameMainActivity activity) {

		// remember that this is the gui
		this.isGui = true;
		
		// the current activity so we can find our GUI views
		this.myActivity = activity;

		// Load the layout resource for the new configuration
		activity.setContentView(R.layout.othello_human_player);

		// set the animator for the animation surface
		surface = (AnimationSurface) myActivity.findViewById(R.id.GameBoardSurface);
		surface.setAnimator(this);

		// sets the background bitmap to an image from the resource folder
		background = BitmapFactory.decodeResource(activity.getResources(), R.drawable.felt_background);

		// sets up the bitmaps for the black/white discs
		whiteDisc = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_piece);
		blackDisc = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_piece);

		// hide our buttons because they are not needed.
		myActivity.findViewById(R.id.HintButton).setVisibility(0);
		myActivity.findViewById(R.id.PassButton).setVisibility(0);
		myActivity.findViewById(R.id.QuitButton).setVisibility(0);

		// sets up our text views for the turn display, and both player scores
		currentTurnDisplay = (TextView)myActivity.findViewById(R.id.CurrentTurnDisplay);

		blackScore = (TextView)myActivity.findViewById(R.id.BlackScore);
		whiteScore = (TextView)myActivity.findViewById(R.id.WhiteScore);

		// creates a handler so we can post code to our activity from the tick method
		myHandler = new Handler();

		// if we have state, "simulate" that it just came from the game
		if (state != null) 
		{
			receiveInfo(state);
		}


	}

	/** 
	 * @return
	 * 		the time interval for the animation
	 */
	public int interval() {
		// 50 milliseconds, or 20 times per second
		return 50;
	}

	/**
	 * @return
	 * 		the animation's background color
	 */
	public int backgroundColor() {
		return Color.rgb(0, 128, 0);
	}

	/**
	 * @return
	 * 		whether the animation should be paused
	 */
	public boolean doPause() {
		// never tell the animation to pause
		return false;
	}

	/**
	 * @return
	 * 		whether the animation should quit
	 */
	public boolean doQuit() {
		// never tell the animation to quit
		return false;
	}

	/**
	 * Don't respond to touches.
	 */
	@Override
	public void onTouch(MotionEvent event){}
	
	/**
	 * perform any initialization that needs to be done after the player
	 * knows what their game-position and opponents' names are.
	 */
	protected void initAfterReady() {
		if( this.isGui ){
			myActivity.setTitle("Othello: "+allPlayerNames[0]+" vs. "+allPlayerNames[1]);
		}
	}

	/**
	 * tick method is used to animate the AnimatorSurface
	 * 
	 * @param g - the canvas on which to draw
	 */
	public void tick(Canvas g) 
	{
		if (!this.isGui){ return; }
		// draws the background of our board
		background = Bitmap.createScaledBitmap(background, g.getWidth(), g.getHeight(), true);
		g.drawBitmap(background, 0, 0, null);

		// creates bitmaps for our black/white discs as well as the hint-x
		whiteDisc = Bitmap.createScaledBitmap(whiteDisc, DISC_SIZE, DISC_SIZE, true);
		blackDisc = Bitmap.createScaledBitmap(blackDisc, DISC_SIZE, DISC_SIZE, true);

		// if we don't have a state, exit the animator and alert user with a dialog
		if(state == null)
		{
			// creates a runnable containing code that opens the alert dialog
			// then posts this code to the handler of our activity
			Runnable showRunner = new Runnable() {
				public void run() {
					noState.show();
				}
			};
			noStateShown = true;
			myHandler.post(showRunner);
			return;
		}
		// if we do have a state, and the no-state dialog is displayed, close the dialog
		else if(noStateShown)
		{
			// creates a runnable containing code that closes the alert dialog
			// then posts this code to the handler of our activity
			Runnable hideRunner = new Runnable() {
				public void run() {
					noState.hide();
					noState.dismiss();
				}
			};
			noStateShown = false;
			myHandler.post(hideRunner);
		}


		// cycles through each spot on the board and draws the correct disc
		for(int row = 0; row < OthelloState.BOARD_HEIGHT; row++)
		{
			for(int col = 0; col < OthelloState.BOARD_WIDTH; col++)
			{
				// calculates the x and y coordinates at which to draw the discs
				int xLocation = col*SQUARE_SIZE + BORDER_SIZE + PADDING;
				int yLocation = row*SQUARE_SIZE + BORDER_SIZE + PADDING;

				// draws a white disc if the CellState is white, black if it is black
				if(state.getDisk(row, col) == CellState.WHITE)
				{
					g.drawBitmap(whiteDisc, xLocation, yLocation, null);
				}
				else if(state.getDisk(row, col) == CellState.BLACK)
				{
					g.drawBitmap(blackDisc, xLocation, yLocation, null);
				}
			}

		}
	}

	/**
	 * @return - the color to paint with
	 */
	public int foregroundColor() {
		return Color.BLACK;
	}

}//class OthelloComputerPlayer
