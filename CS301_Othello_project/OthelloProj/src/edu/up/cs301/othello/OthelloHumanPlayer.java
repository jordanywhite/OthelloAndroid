package edu.up.cs301.othello;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.up.cs301.animation.AnimationSurface;
import edu.up.cs301.animation.Animator;
import edu.up.cs301.game.GameHumanPlayer;
import edu.up.cs301.game.GameMainActivity;
import edu.up.cs301.game.GamePlayer;
import edu.up.cs301.game.R;
import edu.up.cs301.game.infoMsg.GameInfo;
import edu.up.cs301.game.infoMsg.IllegalMoveInfo;
import edu.up.cs301.game.infoMsg.NotYourTurnInfo;

/**
 * A GUI that allows a human to play othello. Moves are made by touching
 * squares on the game board
 * 
 * @author Chandler Underwood & Taylor Spooner
 * @version November 2013
 */
public class OthelloHumanPlayer extends GameHumanPlayer implements Animator, OnClickListener {



	// instance variables used in drawing pieces onto game-board
	private static final int SQUARE_SIZE = 78;
	private static final int BORDER_SIZE = 13;
	private static final int PADDING = 10;
	private static final int DISC_SIZE = 60;
	private static final int HINT_SIZE = 40;

	// the game's state
	protected OthelloState state;

	// the current activity
	private Activity myActivity;

	// bitmap used to draw the background of the othello game-board
	private Bitmap background;

	// bitmaps used to draw othello discs
	private Bitmap whiteDisc;
	private Bitmap blackDisc;

	// bitmap used to draw an x for a hint
	private Bitmap hintX;

	// buttons used in the GUI
	private Button hintButton;
	private Button passButton;
	private Button quitButton;

	// text views used in the GUI
	private TextView currentTurnDisplay;
	private TextView blackScore;
	private TextView whiteScore;

	// Strings used in text-views
	private static final String whiteScoreString = "White Score:\n\t\t\t\t ";
	private static final String blackScoreString = "Black Score:\n\t\t\t\t ";
	private static final String turnString = "Turn:\n";

	// boolean used to tell if hints are enabled or not
	private boolean hint;
	
	// variables used for font colors
	private static final int GREEN = Color.GREEN;
	private static final int RED = Color.RED;

	// variables used to play different sounds
	private SoundPool soundPool;
	private int errorSound;
	private int discSound;
	
	// Handler used to run code out of the tick method.
	private Handler myHandler;
	
	// when there is no state, this alert dialog will be shown
	private AlertDialog noState;
	
	// boolean to let the program know if there is a state or not.
	private boolean noStateShown = false;


	// our animation surface. (We're not actually doing moving animation, but this
	// surface is a convenient way to draw our image.)
	private AnimationSurface surface;

	/**
	 * constructor
	 * 
	 * @param name
	 * 		the player's name
	 */
	public OthelloHumanPlayer(String name) {
		super(name);
	}

	/**
	 * Callback method, called when player gets a message
	 * 
	 * @param info
	 * 		the message
	 */
	@Override
	public void receiveInfo(GameInfo info)
	{
		if (info instanceof IllegalMoveInfo || info instanceof NotYourTurnInfo) {
			// if the move was out of turn or otherwise illegal,
			// plays error sound
			soundPool.play(errorSound, 1, 1, 1, 0, 1f);
		}
		else if (!(info instanceof OthelloState))
		{
			// if we do not have an illegal move or an OthelloState, ignore
			return;
		}
		else {
			// update our 'state' variable with the new state
			this.state = (OthelloState)info;


			Log.i("human player", "receiving");

			// sets the score text views with current scores
			whiteScore.setText(whiteScoreString + state.getWhiteScore());
			blackScore.setText(blackScoreString + state.getBlackScore());

			// sets the current turn text view with the name of the current player
			if(state.getTurn() == this.playerNum)
			{
				currentTurnDisplay.setTextColor(GREEN);
			}
			else
			{
				currentTurnDisplay.setTextColor(RED);
			}
			currentTurnDisplay.setText(turnString + allPlayerNames[state.getTurn()]);

			// boolean used to determine if the hints should be drawn or not
			// default is false, until user presses hint button
			hint = false;

			// disables the hint button if it is not the current players turn
			hintButton.setEnabled(this.state.getTurn() == this.playerNum);

			// pass button is disabled by default
			passButton.setEnabled(false);

			// if it is the current players turn, and there are no legal moves available, enable pass button
			if(this.state.getTurn() == this.playerNum && this.state.listLegalMoves().size() == 0)
			{
				passButton.setEnabled(true);		
			}
		}
	}

	/**
	 * sets the current player as the activity's GUI
	 * 
	 * 
	 */
	public void setAsGui(GameMainActivity activity) {

		// the current activity so we can find our GUI views
		myActivity = activity;

		// Load the layout resource for the new configuration
		activity.setContentView(R.layout.othello_human_player);

		// set the animator for the animation surface
		surface = (AnimationSurface) myActivity
				.findViewById(R.id.GameBoardSurface);
		surface.setAnimator(this);

		// sets the background bitmap to an image from the resource folder
		background = BitmapFactory.decodeResource(activity.getResources(), R.drawable.felt_background);

		// sets up the bitmaps for the black/white discs
		whiteDisc = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_piece);
		blackDisc = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_piece);

		// sets up a bitmap for the drawable hint-x
		hintX = BitmapFactory.decodeResource(activity.getResources(), R.drawable.hint_x);

		// sets up our buttons and gives them all an on click listener
		hintButton = (Button)myActivity.findViewById(R.id.HintButton);
		hintButton.setOnClickListener(this);

		passButton = (Button)myActivity.findViewById(R.id.PassButton);
		passButton.setOnClickListener(this);

		quitButton = (Button)myActivity.findViewById(R.id.QuitButton);
		quitButton.setOnClickListener(this);

		// sets up our text views for the turn display, and both player scores
		currentTurnDisplay = (TextView)myActivity.findViewById(R.id.CurrentTurnDisplay);

		blackScore = (TextView)myActivity.findViewById(R.id.BlackScore);

		whiteScore = (TextView)myActivity.findViewById(R.id.WhiteScore);

		// adding SoundPool to play noise when buttons pressed
		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);

		// loads the sounds we want for button-presses and game winning
		errorSound = soundPool.load(myActivity, R.raw.error, 1);
		discSound = soundPool.load(myActivity, R.raw.disc_drop, 2);
		
		// creates a handler so we can post code to our activity from the tick method
		myHandler = new Handler();
		
		// calls this method to set up our no state alert dialog
		noStateAlert();


		// if we have state, "simulate" that it just came from the game
		if (state != null) 
		{
			receiveInfo(state);
		}


	}

	/**
	 * returns the GUI's top view
	 * 
	 * @return
	 * 		the GUI's top view
	 */
	@Override
	public View getTopView() {
		return myActivity.findViewById(R.id.GameBoardSurface);
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
	 * perform any initialization that needs to be done after the player
	 * knows what their game-position and opponents' names are.
	 */
	protected void initAfterReady() {
		myActivity.setTitle("Othello: "+allPlayerNames[0]+" vs. "+allPlayerNames[1]);
	}

	/**
	 * tick method is used to animate the AnimatorSurface
	 * 
	 * @param g - the canvas on which to draw
	 */
	public void tick(Canvas g) 
	{
		// draws the background of our board
		background = Bitmap.createScaledBitmap(background, g.getWidth(), g.getHeight(), true);
		g.drawBitmap(background, 0, 0, null);

		// creates bitmaps for our black/white discs as well as the hint-x
		whiteDisc = Bitmap.createScaledBitmap(whiteDisc, DISC_SIZE, DISC_SIZE, true);
		blackDisc = Bitmap.createScaledBitmap(blackDisc, DISC_SIZE, DISC_SIZE, true);

		hintX = Bitmap.createScaledBitmap(hintX, HINT_SIZE, HINT_SIZE, true);

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
				if(hint && state.isLegalMove(row, col))
				{
					g.drawBitmap(hintX, xLocation + 10, yLocation + 10, null);
				}
				else if(state.getDisk(row, col) == CellState.WHITE)
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
	 * 
	 * 
	 */
	public int foregroundColor(){
		return Color.BLACK;
	}

	/**
	 * Method used to handle the touches on the AnimatorSurface that represents the 
	 * Othello Game Board
	 * 
	 * @param event - the motion event that was detected
	 */
	public void onTouch(MotionEvent event) {

		// returns if the action was not an "up"
		// we only want to handle "ups" or "releases" 
		if (event.getAction() != MotionEvent.ACTION_UP) return;

		// get the x and y coordinates of the touch event
		int x = (int) event.getX();
		int y = (int) event.getY();

		// map the pixel coordinates to a point
		// x-coordinate of point represents row, y-coordinate represents column
		Point p = mapPixelToSquare(x, y);

		// only creates move action if the row & column are within game boundaries
		if(p.x < OthelloState.BOARD_HEIGHT && p.x >= 0 && p.y < OthelloState.BOARD_WIDTH && p.y >= 0)
		{
			// create an OthelloMoveAction at the row and column of the press 
			// send this action to the game
			this.game.sendAction(new OthelloMoveAction(this, p.x, p.y));

			// if the move is legal, play sound
			if(this.state.isLegalMove(p.x, p.y) && this.state.getTurn() == this.playerNum)
			{
				// plays disc sound
				soundPool.play(discSound, 1, 1, 1, 0, 1f);
			}

		}


	}

	/**
	 * Method used to handle any user presses on the GUI buttons
	 * 
	 * @param v - the view that was pressed by the user
	 * 
	 */
	public void onClick(View v) 
	{	
		// if the hint button is pressed, toggle the hint boolean to show/hide hints
		if(v == hintButton)
		{
			hint = !hint;
		}
		// if quit button is pressed, calls the method quit pressed, to prompt the user for dialog
		else if(v == quitButton)
		{

			quitPressed();

		}
		// if the pass button is pressed, create a new pass action and send it to the game
		else if(v == passButton)
		{
			this.game.sendAction(new OthelloPassAction(this));
		}



	}

	/**
	 * This method is called when the user presses the quit button on the gui
	 * Gives the user a choice of quitting or keep playing so that the game 
	 * is never accidentally quit.
	 * 
	 */
	public void quitPressed()
	{	
		// a reference to the current player in order to reset the game.
		final GamePlayer currentPlayer = this;
		
		// creates a dialog box to confirm that the user wants to quit
		AlertDialog.Builder quitAlert = new AlertDialog.Builder(myActivity);
		quitAlert.setTitle("Do you really want to quit?");

		// sets the positive button of our alert, and tells it to finish and close the activity
		quitAlert.setPositiveButton("Quit", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				myActivity.finish();
			}
		});

		// sets the negative button of our alert which allows user to keep playing
		quitAlert.setNegativeButton("Keep Playing", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		// sets the neutral button of our alert which allows user to start a new game
		quitAlert.setNeutralButton("Restart", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				game.sendAction(new OthelloRestartAction(currentPlayer));
			}
		});		
		// displays the alert
		quitAlert.show();
	}
	
	/**
	 * This method is used to set-up an alert dialog
	 * It's function is to tell the user when there is no state yet available.
	 * 
	 */
	public void noStateAlert()
	{
		// creates an alert dialog builder
		AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
		
		// uses builder made above to create our alert dialog
		// sets the message to inform user and disables cancelling of the alert
		noState = builder.create();
		noState.setMessage("Waiting for other players to connect. Please wait or exit game.");
		noState.setCanceledOnTouchOutside(false);
		noState.setCancelable(false);
		
		// gives the user a button to exit the game.
		noState.setButton(AlertDialog.BUTTON_NEGATIVE, "Exit Game", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {

					myActivity.finish();
			}
		});
		
	}

	/**
	 * Method used to map a pixel coordinate to a row/column location
	 * that makes sense in the context of the othello board
	 * 
	 * @param x - the x-location of the touch
	 * @param y - the y-location of the touch
	 * @return - a point where the x-location represents a row and y-location represents a column
	 */
	public Point mapPixelToSquare(int x, int y)
	{
		// calculation to convert pixel coordinate to row/col
		int row = (y - BORDER_SIZE)/SQUARE_SIZE;
		int col = (x - BORDER_SIZE)/SQUARE_SIZE;

		// return a new point
		return new Point(row, col);
	}

}
