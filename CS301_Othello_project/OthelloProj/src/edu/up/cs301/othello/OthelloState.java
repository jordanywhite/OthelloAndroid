
package edu.up.cs301.othello;

import java.util.ArrayList;

import android.util.Log;

import edu.up.cs301.game.infoMsg.GameState;

/**
 * Contains the state of an Othello game. Sent by the game when a player wants
 * to enquire about the state of the game. Contains the board, pieces, and
 * player turn.
 * 
 * @author Jordan White
 * @version 11/10/2013
 */
public class OthelloState extends GameState
{
    private static final long serialVersionUID = 7552321013488624386L;

    // Board dimension constants
    public static final int BOARD_WIDTH = 8;
    public static final int BOARD_HEIGHT = 8;

    // /////////////////////////////////////////////////
    // ************** instance variables ************   
    // /////////////////////////////////////////////////

    // Contains the actual board and every disk on it
    private CellState[][] board;

    // Defines which player's turn it is (black = 0, white = 1)
    private int playerTurn;

    // You'll never guess what these are
    private int whiteScore;
    private int blackScore;

    /**
     * Constructor for objects of class OthelloState
     */
    public OthelloState()
    {
        // Initialize the state to be a brand new game
        board = new CellState[BOARD_HEIGHT][BOARD_WIDTH];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = CellState.EMPTY;
            }
        }

        // Starting pieces
        setDisk(3, 3, CellState.WHITE);
        setDisk(4, 4, CellState.WHITE);
        setDisk(3, 4, CellState.BLACK);
        setDisk(4, 3, CellState.BLACK);

        // make it black's turn
        playerTurn = 0;

        // Starting score of 2 for each player
        whiteScore = 2;
        blackScore = 2;

    }// constructor

    /**
     * Copy constructor for class OthelloState
     * 
     * @param original the OthelloState object that we want to clone
     */
    public OthelloState(OthelloState original)
    {
        // create a new 3x3 array, and copy the values from
        // the original
        board = new CellState[BOARD_HEIGHT][8];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = original.board[i][j];
            }
        }

        // copy the player-to-move information
        playerTurn = original.playerTurn;

        // Get the scores from the board
        setScores();
    }

    /**
     * Checks a move to see if it captures pieces in at least one direction
     * 
     * @param row the row
     * @param col the column
     * @return true if move is legal, false if illegal
     */
    public boolean isLegalMove(int row, int col) {

        // player represents the color the player whose move it is
        CellState player = (playerTurn == 0 ? CellState.BLACK : CellState.WHITE);
        CellState enemy = (playerTurn == 0 ? CellState.WHITE : CellState.BLACK);

        // Ensures no null pointer nonsense
        if (getDisk(row, col) == null) {
            Log.e("isLegalMove()", "Requested move not on board");
            return false;
        }
        
        // A cell must be empty to be a valid move
        if(!getDisk(row, col).equals(CellState.EMPTY)) return false;

        // Cycle through all 8 directions that could make a move potentially legal
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {

                // Ignore the location of the move anything not on the board
                if (i == 0 && j == 0) continue;
                if (getDisk(row + i, col + j) == null) continue;

                // Returns true if there is a player disk  proceeding any number of
                // enemy disks in a linear direction
                int count = 1;
                while (getDisk(row + i * count, col + j * count).equals(enemy)) {
                    count++;

                    CellState checkDisk = getDisk(row + i * count, col + j * count);
                    if (checkDisk == null) break;
                    if (checkDisk.equals(CellState.EMPTY)) break;
                    if (checkDisk.equals(player)) return true;
                }
            }
        }
        

        // Checked every potential direction where the move could be legal and
        // found no captures. Not a legal move.
        return false;
    }

    /**
     * Runs through the board and finds all legal moves for the player whose
     * turn it is. Returns these as a grid of bools.
     * 
     * @return A boolean array where each true identifies a legal move for the
     *         player on the board
     * 
     * @deprecated use listLegalMoves or isLegalMove instead.
     */
    @Deprecated
    public boolean[][] getLegalMoves() {
        boolean[][] legalMoves = new boolean[BOARD_WIDTH][BOARD_HEIGHT];

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                legalMoves[i][j] = isLegalMove(i, j);
            }
        }

        return legalMoves;
    }

    /**
     * Runs through the board and finds all legal moves for the player whose
     * turn it is. Returns these as an Arraylist<int[]> where each int[] is a coord.
     * 
     * @return An arraylist of legal move coords.
     */
    public ArrayList<int[]> listLegalMoves() {
    	ArrayList<int[]> legalMoves = new ArrayList<int[]>();

        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (isLegalMove(i, j)){
                	legalMoves.add(new int[]{i, j});
                }
            }
        }

        return legalMoves;
    }

    /**
     * Captures all enemy pieces that are allowed based on the move sent by the
     * local game.
     * 
     * @param row The row of the move being sent
     * @param col The column of the move being sent
     */
    public void playDisk(int row, int col) {

        // player represents the color the player whose move it is
        CellState player = (playerTurn == 0 ? CellState.BLACK : CellState.WHITE);
        CellState enemy = (playerTurn == 0 ? CellState.WHITE : CellState.BLACK);

        // Ensures no null pointer nonsense
        if (getDisk(row, col) == null) {
            Log.e("playDisk", "A null move action made it through LocalGame into GameState");
            return;
        }

        // Cycles through all 8 directions that pieces could be captured in
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {

                // Ignore the move cell and anything off of the board
                if (i == 0 && j == 0) continue;
                if (getDisk(row + i, col + j) == null) continue;

                // Same algorithm isLegalMove() uses to see what should be captures
                int count = 1;
                while (getDisk(row + i * count, col + j * count).equals(enemy)) {
                    count++;

                    CellState checkDisk = getDisk(row + i * count, col + j * count);
                    if (checkDisk == null)
                        break;
                    if (checkDisk.equals(CellState.EMPTY))
                        break;
                    if (checkDisk.equals(player)) {
                        count--;
                        checkDisk = getDisk(row + i * count, col + j * count);
                        
                        // Captures all enemy pieces between the player piece and move
                        // location.
                        while (checkDisk.equals(enemy)) {
                            setDisk(row + i * count, col + j * count, player);
                            count--;
                            checkDisk = getDisk(row + i * count, col + j * count);

                            // Never figured out what causes this one, but it can happen.
                            if(checkDisk == null) {
                                Log.e("playDisk()", "Something went very, very wrong");
                                return;
                            }
                        }
                        // Places piece at the move location
                        setDisk(row, col, player);
                    }
                }
            }
        }
        
        // Updates player scores
        setScores();
        
        // Next players turn
        playerTurn = playerTurn == 0 ? 1 : 0;
    }

    /**
     * Counts every piece on the board and updates the black and white scores
     * accordingly
     */
    public void setScores() {
        // Resets score for each count
        whiteScore = 0;
        blackScore = 0;

        // Cycles through board and gives each player 1 point for each piece
        // they own
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].equals(CellState.WHITE)) {
                    whiteScore++;
                } else if (board[i][j].equals(CellState.BLACK)) {
                    blackScore++;
                }
            }
        }
    }

    /**
     * Find out which disk is in a cell
     * 
     * @param row The row
     * @param col The column
     * @return the piece at the given square; EMPTY if no piece there; null if
     *         it is an illegal square
     */
    public CellState getDisk(int row, int col) {
        // if we're out of bounds or something is horribly wrong, we return null
        if (board == null || row < 0 || col < 0 ||
                row >= BOARD_HEIGHT || col >= BOARD_WIDTH) {
            return null;
        }

        // return the CellState of the requested position
        return board[row][col];
    }

    /**
     * Sets a disk on a cell
     * 
     * @param row the row being queried
     * @param col the column being queried
     * @param piece the piece to place
     */
    public void setDisk(int row, int col, CellState disk) {
        // if we're out of bounds, return and do nothing
        if (board == null || row < 0 || col < 0 ||
                row >= BOARD_HEIGHT || col >= BOARD_WIDTH) {

            Log.e("OthelloState setDisk()", "Attempted to place disk at a location not on the board");
            return;
        }

        // Places the piece
        board[row][col] = disk;
    }

    /**
     * Tells whose move it is.
     * 
     * @return the index (0 or 1) of the player whose move it is.
     */
    public int getTurn() {
        return playerTurn;
    }

    /**
     * set whose move it is
     * 
     * @param playerTurn the player we want to accept the next move from
     */
    public void setTurn(int playerNum) {
        if (playerNum == 0 || playerNum == 1)
            playerTurn = playerNum;
        else {
            Log.e("OthelloState setTurn()", "Invalid player assignment, default to black");
            playerTurn = 0;
        }
    }
    
    
    /**
     * @return Number of white pieces on the board
     */
    public int getWhiteScore() {
        return whiteScore;
    }

    /**
     * @return Number of black pieces on the board
     */
    public int getBlackScore() {
        return blackScore;
    }
}
