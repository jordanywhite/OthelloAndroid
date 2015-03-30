package edu.up.cs301.othello;

public class SuicideOthelloComputerPlayer extends SmartOthelloComputerPlayer {

	public SuicideOthelloComputerPlayer(String name) {
		super(name);
	}

	@Override
    protected double quantifyState(OthelloState state){
    	return -super.quantifyState(state);
    }

}
