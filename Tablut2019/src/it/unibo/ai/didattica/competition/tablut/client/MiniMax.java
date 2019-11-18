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

	public  int maxUtility = 0;
	private Game game;
	private Turn player;
	private Integer maxDepth = 3;

	/**
	 * 
	 * @param game
	 * @param player player which the CPU wants to win
	 */
	public MiniMax(Game game, Turn player) {
		super();
		this.game = game;
		this.player = player;
	}


	public State minimaxDecision(State state) {
		Integer depth = 0;
		LinkedList<State> successors = successors(state);
		int max = Integer.MIN_VALUE;
		State bestMove = null;
		int v;

		for (State s : successors) {
			v = minValue(s, depth -1);
			
			if (v > max) {
				max = v;
				bestMove = s;
			}
		}
		System.out.println("max: " + max);
		return bestMove;

	}

	public int maxValue(State state, Integer depth) {
		depth = depth + 1;
		if (terminalTest(state) || depth == maxDepth) return utility(state);
		int v = Integer.MIN_VALUE;

		for (State s : successors(state)) 			
			v = Math.max(v, minValue(s, depth));

		return v;
	}

	public int minValue(State state, Integer depth) {
		depth = depth + 1;
		if (terminalTest(state) || depth == maxDepth) return utility(state);
		int v = Integer.MAX_VALUE;

		for (State s : successors(state)) 
			v = Math.min(v, maxValue(s, depth));

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
	 * 
	 * @param state
	 * @param turn
	 * @param game
	 * @return List of all the possible next states for a player
	 */
	public LinkedList<State> successors(State state) {

		LinkedList<State> elegibiles = new LinkedList<>();
		if (!terminalTest(state)) {
			Pawn[][] board = state.getBoard();
			int dim = board.length;
			for (int row = 0; row < dim; row++)
				for (int col = 0; col < dim; col++)
					if (board[row][col].toString().equals(state.getTurn().toString()) || 
							( state.getTurn().equals(Turn.WHITE) && board[row][col].toString().equals(Pawn.KING.toString()))) {
						LinkedList<String> toTry = new LinkedList<>();
						String from = state.getBox(row, col);

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
								elegibiles.add(game.checkMove(state.clone(), new Action(from, to, state.getTurn())).clone());
							} catch (Exception e) { /*System.out.println("\tNO"); */}
						}
					} 
		}
		return elegibiles;
	}


	public static void main(String args[]) {
		Game game = new GameAshtonTablut(99, 0, "garbage", "fake", "fake");
		MiniMax m = new MiniMax(game, Turn.WHITE);

		State s0 = new StateTablut(), s1;
		s0.setTurn(Turn.WHITE);

		System.out.println("*" +s0.getTurn());

		for (int i=0; i<9; i++)
			for (int j=0; j<9; j++)
				s0.getBoard()[i][j] = Pawn.EMPTY;

		//		s0.getBoard()[3][4] = Pawn.KING;
		//		s0.getBoard()[2][4] = Pawn.WHITE;
		//		s0.getBoard()[3][5] = Pawn.WHITE;
		//		s0.getBoard()[8][5] = Pawn.WHITE; // LUI MANGIA in 2 mosse
		//		s0.getBoard()[6][4] = Pawn.BLACK;

		//s0.getBoard()[2][4] = Pawn.BLACK;
		s0.getBoard()[5][3] = Pawn.BLACK;
		s0.getBoard()[4][4] = Pawn.KING;
		//s0.getBoard()[8][6] = Pawn.BLACK; // LUI MANGIA in 2 mosse



		s1 = m.minimaxDecision(s0);
		System.out.println(s0);
		System.out.println(s1);



	}

}
