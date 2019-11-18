package it.unibo.ai.didattica.competition.tablut.client;

import java.util.LinkedList;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class MiniMax {

	public static final int WIN = 3;
	public static final int LOOSE = -3;
	public static final int DRAW = -1;
	public static final int NEUTRAL = 0;

	private Game game;
	private Turn player;

	public MiniMax(Game game, Turn player) {
		super();
		this.game = game;
		this.player = player;
	}


	public State minimaxDecision(State state) {
		int v = maxValue(state);
		System.out.println("V: " + v);
		for (State s : successors(state)) {
			System.out.println("------- utility: " + utility(s) + ",    v : " + v);

			if (utility(s) == v) 
			{
				
				return s;
			}
		}
		return null;
	}

	public int maxValue(State state) {
		if (terminalTest(state))  {
			System.out.println("(maxvalue)"+state+"terminal! utility: " + utility(state));
			return utility(state);
		}
		int v = Integer.MIN_VALUE;
		for (State s : successors(state)) {
			v = Math.max(v, minValue(s));
		}
		return v;
	}

	public int minValue(State state) {
		if (terminalTest(state)) {
			System.out.println("(minvalue) "+state+"terminal! utility: " + utility(state));
			return utility(state);
		}
		int v = Integer.MAX_VALUE;
		for (State s : successors(state)) {
			v = Math.min(v, maxValue(s));
		}
		return v;
	}

	public boolean terminalTest(State state) {
		
		if (state.getTurn().equals(Turn.BLACKWIN) ||
				state.getTurn().equals(Turn.WHITEWIN) || 
				state.getTurn().equals(Turn.DRAW)) {
			System.out.println("TURNO TERMINALE: " + state.getTurn());
		return true;
		} else return false;
		
	}


	/**
	 * 
	 * @param state
	 * @return
	 */
	public int utility(State state) {
		if (player.equals(Turn.BLACK)) {
			if (state.getTurn().equals(Turn.BLACKWIN)) {
				return WIN;
			}
			if (state.getTurn().equals(Turn.WHITEWIN)) {
				return LOOSE;
			}
		}

		if (player.equals(Turn.WHITE)) {
			if (state.getTurn().equals(Turn.WHITEWIN)) {
				return WIN;
			}
			if (state.getTurn().equals(Turn.BLACKWIN)) {
				return LOOSE;
			}
		}

		if (state.getTurn().equals(Turn.DRAW)) {
			return DRAW;
		}

		return NEUTRAL;
	}











	/**
	 * @param row
	 * @param column
	 * @param state
	 * @return List of all the possible next states for a specific pawn of a faction, in all the four directions
	 */
	public LinkedList<State> successors(int row, int col, State state) {
		//System.out.println("Computing the moves from cell " + state.getBox(row, col));
		LinkedList<State> elegibiles = new LinkedList<>();
		LinkedList<String> toTry = new LinkedList<>();
		String from = state.getBox(row, col);
		Pawn[][] board = state.getBoard();
		int dim = board.length;

		// up
		for (int r = row-1; r >= 0; r--)	
			if (board[r][col].equals(Pawn.EMPTY)) toTry.add(state.getBox(r, col));	else break;// free cells on vertical axis 
		// right
		for (int c = col+1; c < dim; c++)	
			if (board[row][c].equals(Pawn.EMPTY)) toTry.add(state.getBox(row, c));	else break;// free cells on horizontal axis 
		// down
		for (int r = row+1; r < dim; r++)	
			if (board[r][col].equals(Pawn.EMPTY)) toTry.add(state.getBox(r, col));	else break; // free cells on vertical axis 
		// elft
		for (int c = col-1; c >= 0; c--)	
			if (board[row][c].equals(Pawn.EMPTY)) toTry.add(state.getBox(row, c));	else break;// free cells on horizontal axis 


		for (String to:toTry) {
			try {
				/*TODO checkMove modifica lo stato ad ogni controllo, quindi serve fare una copia dello stato ogni volta
				 * GameAshtonTablut.java riga 268  */
				elegibiles.add(game.checkMove(state.clone(), new Action(from, to, state.getTurn())).clone());
			} catch (Exception e) { /*System.out.println("\tNO"); */}
		}
		return elegibiles;
	}


	/**
	 * 
	 * @param state
	 * @param turn
	 * @param game
	 * @return List of all the possible next states for a player
	 */
	public LinkedList<State> successors(State state) {
		LinkedList<State> elegibiles = new LinkedList<>();
		Pawn[][] board = state.getBoard();
		int dim = board.length;
		for (int r = 0; r < dim; r++)
			for (int c = 0; c < dim; c++)
				if (board[r][c].toString().equals(state.getTurn().toString())) {
					//System.out.println("Trovata pedina " + turn);
					elegibiles.addAll(successors(r, c, state));
				} //else System.out.println("pedina diversa " + board[r][c]);
		return elegibiles;
	}


	public static void main(String args[]) {
		Game game = new GameAshtonTablut(99, 0, "garbage", "fake", "fake");
		MiniMax m = new MiniMax(game, Turn.BLACK);
				
		State s0 = new StateTablut();
		
		for (int i=0; i<9; i++)
			for (int j=0; j<9; j++)
				s0.getBoard()[i][j] = Pawn.EMPTY;
		
		s0.getBoard()[3][4] = Pawn.KING;
		s0.getBoard()[2][4] = Pawn.BLACK;
		s0.getBoard()[3][3] = Pawn.BLACK;
		s0.getBoard()[8][5] = Pawn.BLACK;
		s0.getBoard()[4][4] = Pawn.THRONE;

		
		System.out.println(s0);

		State s1 = m.minimaxDecision(s0);
		
		for (State s : m.successors(s0))
			System.out.println(s + "\tutility: " + m.utility(s) + "\n\tis terminal: " + m.terminalTest(s));

		System.out.println("\n\n\n------> " + s1);

	}

}
