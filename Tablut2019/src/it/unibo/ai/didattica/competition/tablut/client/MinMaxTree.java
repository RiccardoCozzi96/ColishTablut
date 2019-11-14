package it.unibo.ai.didattica.competition.tablut.client;
import java.time.LocalTime;
import java.util.LinkedList;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class MinMaxTree extends MyTree<State> {


	public MinMaxTree(State s1) {
		super(s1);
	}

	public MinMaxTree() {
		super();
	}

	public LinkedList<State> computeFutureStates(MyNode<State> node, Turn turn, Game game) {
		return this.computeFutureStates(getState(node), turn, game);
	}
	
	/**
	 * @param row
	 * @param column
	 * @param state
	 * @return List of all the possible next states for a specific pawn of a faction, in all the four directions
	 */
	public LinkedList<State> computeFutureStates(int row, int col, State state, Turn turn, Game game) {
		//System.out.println("Computing the moves from cell " + state.getBox(row, col));
		LinkedList<State> elegibiles = new LinkedList<>();
		LinkedList<String> toTry = new LinkedList<>();
		String from = state.getBox(row, col);
		Pawn[][] board = state.getBoard();
		int dim = board.length;
		
		for (int r = 0; r < dim; r++)	
			if (r != row && board[r][col].equals(Pawn.EMPTY)) toTry.add(state.getBox(r, col));	// free cells on vertical axis 
			//else break; // non va messo
		for (int c = 0; c < dim; c++)	
			if (c != col && board[row][c].equals(Pawn.EMPTY)) toTry.add(state.getBox(row, c));	// free cells on horizontal axis 
			//else break;// non va messo
		
		for (String to:toTry) {
			try {
				/*TODO checkMove modifica lo stato ad ogni controllo, quindi serve fare una copia dello stato ogni volta
				 * GameAshtonTablut.java riga 268  */
				elegibiles.add(game.checkMove(state.clone(), new Action(from, to, turn)).clone());
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
	public LinkedList<State> computeFutureStates(State state, Turn turn, Game game) {
		LinkedList<State> elegibiles = new LinkedList<>();
		Pawn[][] board = state.getBoard();
		int dim = board.length;
		for (int r = 0; r < dim; r++)
			for (int c = 0; c < dim; c++)
				if (board[r][c].toString().equals(turn.toString())) {
					//System.out.println("Trovata pedina " + turn);
					elegibiles.addAll(computeFutureStates(r, c, state, turn, game));
				} //else System.out.println("pedina diversa " + board[r][c]);
		return elegibiles;
	}
	
	@Deprecated
	public State getValue(MyNode<State> node) {
		return super.getValue(node);
	}
	
	public State getState(MyNode<State> node) {
		return this.getValue(node);
	}
	

	/**
	 * MAIN
	 */
	public static void main(String[] args) {
		
		int moveCache = -1;
		int repeated = 0;
		int counter = 0;
		State s = new StateTablut();
		System.out.println("initial state" + s);
		Game game = new GameAshtonTablut(s, repeated, moveCache, "logs", "Riccardo", "Alessandro");
		LinkedList<State> level1, level2, level3;
		MinMaxTree t = new MinMaxTree(s);
		
		
		try {
			System.out.println(s);
			s.setTurn(Turn.BLACK);
			
			level1 = t.computeFutureStates(t.getRoot(), Turn.BLACK, game);
			t.setSons(t.getRoot(), level1);
			
			for (MyNode<State> son1 : t.getSons(t.getRoot())) {
				t.getState(son1).setTurn(Turn.WHITE);
				level2 = t.computeFutureStates(son1, Turn.WHITE, game);
				t.setSons(son1, level2);
				
				for (MyNode<State> son2 : t.getSons(t.getNode(son1))) {
					t.getState(son2).setTurn(Turn.BLACK);
					level3 = t.computeFutureStates(son2, Turn.BLACK, game);
					t.setSons(son2, level3);
					counter += level3.size();
				}
			}
			
		} catch (Exception e) {}
		
	}
}
