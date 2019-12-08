// 27/11/2019 - 12:25

package it.unibo.ai.didattica.competition.tablut.client;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class MiniMax {

	private static final short maxDepth = 4;
	private short time = 58;

	private String AIColor;															// color of the AI player
	private String opponentColor;													// color of the opponent

	private LinkedList<Action> eligibleActions = new LinkedList<Action>();			// eligible actions of the first search level
	private LinkedList<String> AIPawnsList = new LinkedList<String>();				// list of pawns of the AI player
	private LinkedList<String> opponentPawnsList = new LinkedList<String>();		// list of pawns of the opponent
	private String kingLoc = "e5";													// position of the KING

	private static final short WIN = 1000;												// WIN score
	private static final short LOOSE = -1000;											// LOOSE score
	private static final short CAPTURE = 100;											// CAPTURE score
	
	// Number of pawns to capture (*2 if you can't re-enter that cell)

	private int maxReturns = 0;														// alphaBeta cuts on MAX
	private int minReturns = 0;														// alphaBeta cuts on MIN
	private int maxCaptureReturns = 0;												// captures cuts on MAX
	private int minCaptureReturns = 0;												// captures cuts on MIN

	private boolean firstWhite = true;
	private boolean firstPrint = true;												// used to print for just one time
	private LocalTime start;

	private LinkedList<String> nearThrone = new LinkedList<String>();
	private LinkedList<String> nearCitadel = new LinkedList<String>();

	public MiniMax(Game game, Turn player) {
		super();
		this.AIColor = player.toString();
		if (player.equals(Turn.WHITE)) opponentColor = Turn.BLACK.toString();
		else opponentColor = Turn.WHITE.toString();
		this.eligibleActions = new LinkedList<Action>();

	}

	// alphaBeta minimax function
	public Action alphaBetaSearch(State state) {

		this.eligibleActions.clear();								// clear the list of eligible actions
		updatePawns(state);											// update number of pawns on board
		LinkedList<Integer> v_values = new LinkedList<Integer>();	// list to keep track of the possible scores
		LinkedList<State> successors = successors(state, true);		// list to hold the first level of successors
		Integer depth = 0;											// initialize level of depth of search
		int max = Integer.MIN_VALUE;//-maxDepth;					// initialize MAX value (minimax)
		int bestMoveIndex = -1;										// holds the index of the best move found
		int v = Integer.MIN_VALUE;									// holds the value of the analyzed node
		int counter = 0;											// counter to get the index of the best move

		start = LocalTime.now();									// start timer

		// minimax algorithm
		for (State s : successors) {
			if (this.checkTimeout()) break;			// check timeout

			int capturePoints = countCapturedPawns(s, opponentColor)*CAPTURE;
			v = alphaBetaMin(s, depth + 1, Integer.MIN_VALUE, Integer.MAX_VALUE)+capturePoints;
			//TODO v += beCareful(s, eligibleActions.get(counter));
			v_values.add(v);
			if (v > max) {
				max = v;
				bestMoveIndex = counter;
			}
			System.out.print(++counter + " ");
		}

		List<Integer> bestIndexes = new LinkedList<Integer>();

		for (int i = 0; i<v_values.size(); i++) {					// per ogni valore di v
			if (v_values.get(i) == max)								// 		se � pari al massimo
				bestIndexes.add(i);									//			aggiungi il valore ai massimi
		}
		
		if (AIColor.equals(Pawn.WHITE.toString()) && firstWhite) {
			firstWhite = false;
			bestMoveIndex = bestIndexes.get(0);		// 		scegline uno random trai migliori
		}
		else  bestMoveIndex = bestIndexes.get(new Random().nextInt(bestIndexes.size()));		// 		scegline uno random trai migliori
		
		LocalTime end = LocalTime.now();		// stop timer
		System.out.println("\n\n\n___________\nmossa in circa : " + ((end.toSecondOfDay() - start.toSecondOfDay())) + " secondi" + " con valore: " + max);
		System.out.println("V_values: " + v_values);
		//System.out.println("maxReturns: " + maxReturns + " minReturns: " + minReturns);
		//System.out.println("maxCaptureReturns: " + maxCaptureReturns + " minCaptureReturns: " + minCaptureReturns);
		return this.eligibleActions.get(bestMoveIndex);		
	}

	// maxValue function
	public int alphaBetaMax(State state, Integer depth, Integer alpha, Integer beta) {
		int capturePoints = countCapturedPawns(state, AIColor)*CAPTURE;
		if (capturePoints > 0) capturePoints += maxDepth-depth;
		updatePawns(state);
		if (terminalTest(state) || depth == maxDepth) {
			return utility(state)-1+capturePoints;
		}
		int v = Integer.MIN_VALUE;
		for (State s : successors(state, false)) {
			if (this.checkTimeout()) return v;			// check timeout
			v = Math.max(v, alphaBetaMin(s, depth+1, alpha, beta));
			if (v >= beta) {
				maxReturns++;
				return v-1+capturePoints;
			}
			alpha = Math.max(alpha, v);
		}
		return v-1;
	}

	// minValue function
	public int alphaBetaMin(State state, Integer depth, Integer alpha, Integer beta) {
		int capturePoints = countCapturedPawns(state, opponentColor)*CAPTURE;
		if (capturePoints > 0) capturePoints += maxDepth-depth;
		updatePawns(state);
		if (terminalTest(state) || depth == maxDepth)  {
			return utility(state)-1+capturePoints;
		}
		int v = Integer.MAX_VALUE;
		for (State s : successors(state, false)) {
			if (this.checkTimeout()) return v;			// check timeout
			v = Math.min(v, alphaBetaMax(s, depth+1, alpha, beta));
			if (v <= alpha) {
				minReturns++;
				return v-1+capturePoints;
			}
			beta = Math.min(beta, v);
		}
		return v-1;
	}

	// returns the list of successors of a state
	public LinkedList<State> successors(State state, boolean firstLevel) {
		int row, col;
		String to; 
		List<String> playerPawns = new LinkedList<String>();
		LinkedList<State> elegibles = new LinkedList<>();
		if (!terminalTest(state)) {
			Pawn[][] board = state.getBoard();
			int dim = board.length;

			if (state.getTurn().toString().equals(AIColor)) 
				playerPawns = (List<String>) AIPawnsList.clone();
			else 
				playerPawns = (List<String>) opponentPawnsList.clone();


			if (state.getTurn().equals(Turn.WHITE))
				playerPawns.add(0, kingLoc);

			for (String from : playerPawns) {
				row = getRow(from);
				col = getColumn(from);
				LinkedList<Integer> toTryRow = new LinkedList<>();
				LinkedList<Integer> toTryCol = new LinkedList<>();

				// up
				for (int r = row-1; r >= 0; r--)  
					if (board[r][col].equals(Pawn.EMPTY) && !accessDenied(row, col, r, col)) {
						toTryRow.add(r); 
						toTryCol.add(col);
					} else break;// free cells on vertical axis 
				// right
				for (int c = col+1; c < dim; c++)  
					if (board[row][c].equals(Pawn.EMPTY) && !accessDenied(row, col, row, c)){
						toTryRow.add(row); 
						toTryCol.add(c);
					} else break;// free cells on vertical axis 
				// down
				for (int r = row+1; r < dim; r++)  
					if (board[r][col].equals(Pawn.EMPTY) && !accessDenied(row, col, r, col)){
						toTryRow.add(r); 
						toTryCol.add(col);
					} else break;// free cells on vertical axis 
				// left
				for (int c = col-1; c >= 0; c--)  
					if (board[row][c].equals(Pawn.EMPTY) && !accessDenied(row, col, row, c)){
						toTryRow.add(row); 
						toTryCol.add(c);
					} else break;// free cells on vertical axis

				for (int i = 0; i < toTryRow.size(); i++) {
					to =  state.getBox(toTryRow.get(i), toTryCol.get(i));
					try {

						Action a = new Action(from, to, state.getTurn());									// initiate useful variables
						State newState = state.clone();

						newState = movePawn(newState, a);												// muovo la pedina

						if (newState.getTurn().equalsTurn("W")) checkCaptureBlack(newState, a);		// a questo punto controllo lo stato per eventuali catture
						else if (newState.getTurn().equalsTurn("B")) checkCaptureWhite(newState, a);

						// aggiungo lo stato alla lista dei successori
						if (state.getPawn(row, col).equals(Pawn.KING)) {									// se l'azione consisteva in una mossa del re
							elegibles.add(0, newState);														// 		aggiungi la mossa all'inizio della lista
							if (firstLevel)  eligibleActions.add(0, a);										// 		se � il primo livello di ricerca aggiungi la mossa all'inizio della lista delle mosse possibili
						} else {																			// altrimenti
							elegibles.add(newState);														//      aggiungi la mossa alla fine della lista
							if (firstLevel)  eligibleActions.add(a);										//		se � il primo livello di ricercaa aggiungi la mossa alla fine della lista delle mosse possibili
						}

					} catch(Exception e) {System.out.println(e);}
				} 
			}
		}
		return elegibles;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////// UTILS ///////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// terminal test function
	public boolean terminalTest(State state) {
		// check the three possible terminal conditions
		if (state.getTurn().equals(Turn.BLACKWIN) ||
				state.getTurn().equals(Turn.WHITEWIN) || 
				state.getTurn().equals(Turn.DRAW)) {
			return true;
		} else return false;
	}

	// utility function - calculates the utility value of a move
	public int utility(State state) {
		String turn = state.getTurn().toString();
		int score = 0; // ogni volta che risali di livello poi, togli uno
		// if AI is a BLACK player
		if (AIColor.equals(Turn.BLACK.toString())) {
			if (turn.equals(Turn.BLACKWIN.toString())) {
				score += WIN;
			}
			if (turn.equals(Turn.WHITEWIN.toString())) {
				score +=  LOOSE;
			}
		}
		// if AI is a WHITE player
		if (AIColor.equals(Turn.WHITE.toString())) {
			if (turn.equals(Turn.WHITEWIN.toString())) {
				score +=  WIN;
			}
			if (turn.equals(Turn.BLACKWIN.toString())) {
				score += LOOSE;
			}
		}

		return score; 
	}

	// check move's score modifiers
	/*public int beCareful(State state, Action a) {
		int score = 0;
		String from = a.getFrom();
		String to = a.getTo();
		int colFrom = getColumn(from);
		int rowFrom = getRow(from);
		int colTo = getColumn(to);
		int rowTo = getRow(to);

		// FROM
		if (from.equals("e5"))																		// se il re esce dal trono:
			score -= THRONE;																		// 		togli punti
		if ( 		((rowTo != 0) && ((state.getPawn(rowTo-1, colTo))) == (Pawn.WHITE))
				|| ((rowTo != 8) && ((state.getPawn(rowTo+1, colTo))  == (Pawn.WHITE)))
				|| ((colTo != 0) && ((state.getPawn(rowTo, colTo-1))  == (Pawn.WHITE)))
				|| ((colTo != 8) && ((state.getPawn(rowTo, colTo+1)) == (Pawn.WHITE))))
			score += 1;
		else if (  ((rowTo != 0) && ((state.getPawn(rowTo-1, colTo))) == (Pawn.KING))
				|| ((rowTo != 8) && ((state.getPawn(rowTo+1, colTo))  == (Pawn.KING)))
				|| ((colTo != 0) && ((state.getPawn(rowTo, colTo-1))  == (Pawn.KING)))
				|| ((colTo != 8) && ((state.getPawn(rowTo, colTo+1)) == (Pawn.KING))))
			score += 2;
//		else if (isCitadel(colFrom, rowFrom)														// se un nero esce dalla citadel:
//				&& !isCitadel(colTo, rowTo))														// 		togli punti
//			score -= CITADEL;
//		else if ( nearThrone.contains(from))														// se si lascia il bordo del trono:
//			if (state.getPawn(rowFrom, colFrom).equals(Pawn.KING))									// 		se � il re:
//				score -= KINGNEARTHRONE;															// 			togli punti
//			else 																					// 		altrimenti:
//				score += NEARTHRONE;																// 			aggiungi punti
//		else if ( nearCitadel.contains(from))														// se si lascia il bordo di una citadel:
//			score += NEARCITADEL;																	// 		aggiungi punti
//		// TO
//		if ( nearThrone.contains(to))																// se ci si avvicina al bordo del trono
//			if (state.getPawn(rowTo, colTo).equals(Pawn.KING))										// se � il re:
//				score += KINGNEARTHRONE;															// 		aggiungi punti
//			else 																					// altrimenti:
//				score -= NEARTHRONE;																// 		togli punti
//		if ( nearCitadel.contains(to))																// se ci si avvicina al bordo di una citadel:
//			score -= NEARCITADEL;																	// 		togli punti

		return score;
	}*/
	

	// returns the number of captured pawns with a move
	public int countCapturedPawns(State newState, String color) {
		if (color.equals(AIColor)){													// se il nuovo turno � dell' AI
			return (AIPawnsList.size() - this.countNPawns(newState, color));		// 		sono state catturate le pedine AI
		} else {																	// altrimenti
			return (opponentPawnsList.size() - this.countNPawns(newState, color));	// 		sono state catturate le pedine avversarie
		}
	}

	// returns the number of pawns with a move
	public int countNPawns(State state, String color) {
		Pawn[][] board = state.getBoard();
		int count = 0;

		int dim = board.length;
		for (int row = 0; row < dim; row++)
			for (int col = 0; col < dim; col++) 
				if (board[row][col].toString().equals(color))
					count++;
		return count;
	}

	// updates the current number of pawns in the board
	public void updatePawns(State state) {
		// clear the two lists of pawns
		AIPawnsList.clear();
		opponentPawnsList.clear();
		// initialize useful variables
		Pawn[][] board = state.getBoard();
		int dim = board.length;
		// cicla l'intera scacchiera
		for (int row = 0; row < dim; row++)
			for (int col = 0; col < dim; col++) {
				if (board[row][col].toString().equals(AIColor))			// se la pedina � AI
					AIPawnsList.add(state.getBox(row, col));			// aggiungila alla lista delle pedine AI
				if (board[row][col].toString().equals(opponentColor))	// se la pedina � avversaria
					opponentPawnsList.add(state.getBox(row, col));		// aggiungila alla lista delle pedine avversarie
				if (board[row][col].equals(Pawn.KING))					// se la pedina � il re
					kingLoc = state.getBox(row, col);					// aggiorna la posizione del re
			}
	}

	// returns the correct column integer value
	public int getColumn(String position) {
		return ((int)position.charAt(0)) - 97;
	}

	// returns the corret row integer value
	public int getRow(String position) {
		return ((int)position.charAt(1)) - 49;
	}

	// checks the timeout
	public boolean checkTimeout() {
		LocalTime now = LocalTime.now();
		if (-(start.toSecondOfDay() - now.toSecondOfDay()) > (time-5)) {
			return true;
		}
		return false;
	}

	// checks whether a cell is accessible or not
	public boolean accessDenied(int rowFrom, int colFrom, int rowTo, int colTo) {
		return ( (!isCitadel(rowFrom, colFrom) && isCitadel(rowTo, colTo)) || isThrone(rowTo, colTo));
	}

	// cheks whether a cell is the throne cell
	public boolean isThrone(int rowTo, int colTo) {
		return (rowTo == 4 && colTo == 4);
	}

	// checks whether a cell is a citadel
	public boolean isCitadel(int row, int col) {
		return (((row == 0 || row == 8) && (col >= 3 && col <= 5)) ||
				((col == 0 || col == 8) && (row >= 3 && row <= 5)) ||
				((row == 1 || row == 7) && (col == 4)) ||
				((col == 1 || col == 7) && (row == 4)) );
	}

	// moves target pawn in the board
	private State movePawn(State state, Action a) {

		Pawn pawn = state.getPawn(a.getRowFrom(), a.getColumnFrom());	

		Pawn[][] newBoard = state.getBoard();

		if (a.getColumnFrom() == 4 && a.getRowFrom() == 4)				// libero il trono o una casella qualunque
			newBoard[a.getRowFrom()][a.getColumnFrom()] = Pawn.THRONE;
		else newBoard[a.getRowFrom()][a.getColumnFrom()] = Pawn.EMPTY;

		newBoard[a.getRowTo()][a.getColumnTo()] = pawn;					// metto nel nuovo tabellone la pedina mossa
		state.setBoard(newBoard);										// aggiorno il tabellone
		if (state.getTurn().equalsTurn(State.Turn.WHITE.toString())) 	// cambio il turno
			state.setTurn(State.Turn.BLACK);
		else state.setTurn(State.Turn.WHITE);

		return state;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////// CHECK CAPTURES //////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	private State checkCaptureBlackKingLeft(State state, Action a) {
		int columnTo = a.getColumnTo();
		int rowTo = a.getRowTo();

		// ho il re sulla sinistra
		if (columnTo > 1 && state.getPawn(rowTo, columnTo - 1).equalsPawn("K")) {
			// re sul trono
			if (state.getBox(rowTo, columnTo - 1).equals("e5")) {
				if (state.getPawn(3, 4).equalsPawn("B") && state.getPawn(4, 3).equalsPawn("B")
						&& state.getPawn(5, 4).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			// re adiacente al trono
			if (state.getBox(rowTo, columnTo - 1).equals("e4")) {
				if (state.getPawn(2, 4).equalsPawn("B") && state.getPawn(3, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			if (state.getBox(rowTo, columnTo - 1).equals("f5")) {
				if (state.getPawn(5, 5).equalsPawn("B") && state.getPawn(3, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			if (state.getBox(rowTo, columnTo - 1).equals("e6")) {
				if (state.getPawn(6, 4).equalsPawn("B") && state.getPawn(5, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			// sono fuori dalle zone del trono
			if (!state.getBox(rowTo, columnTo - 1).equals("e5")
					&& !state.getBox(rowTo, columnTo - 1).equals("e6")
					&& !state.getBox(rowTo, columnTo - 1).equals("e4")
					&& !state.getBox(rowTo, columnTo - 1).equals("f5")) {
				if (state.getPawn(rowTo, columnTo - 2).equalsPawn("B")
						|| isCitadel(rowTo, columnTo - 2)) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
		}
		return state;
	}

	private State checkCaptureBlackKingRight(State state, Action a) {
		int columnTo = a.getColumnTo();
		int rowTo = a.getRowTo();
		Pawn[][] board = state.getBoard();

		// ho il re sulla destra
		if (columnTo < board.length - 2
				&& (state.getPawn(rowTo, columnTo + 1).equalsPawn("K"))) {
			// re sul trono
			if (state.getBox(rowTo, columnTo + 1).equals("e5")) {
				if (state.getPawn(3, 4).equalsPawn("B") && state.getPawn(4, 5).equalsPawn("B")
						&& state.getPawn(5, 4).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			// re adiacente al trono
			if (state.getBox(rowTo, columnTo + 1).equals("e4")) {
				if (state.getPawn(2, 4).equalsPawn("B") && state.getPawn(3, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			if (state.getBox(rowTo, columnTo + 1).equals("e6")) {
				if (state.getPawn(5, 5).equalsPawn("B") && state.getPawn(6, 4).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			if (state.getBox(rowTo, columnTo + 1).equals("d5")) {
				if (state.getPawn(3, 3).equalsPawn("B") && state.getPawn(5, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			// sono fuori dalle zone del trono
			if (!state.getBox(rowTo, columnTo + 1).equals("d5")
					&& !state.getBox(rowTo, columnTo + 1).equals("e6")
					&& !state.getBox(rowTo, columnTo + 1).equals("e4")
					&& !state.getBox(rowTo, columnTo + 1).equals("e5")) {
				if (state.getPawn(rowTo, columnTo + 2).equalsPawn("B")
						|| isCitadel(rowTo, columnTo + 2)) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
		}
		return state;
	}

	private State checkCaptureBlackKingDown(State state, Action a) {
		int columnTo = a.getColumnTo();
		int rowTo = a.getRowTo();
		Pawn[][] board = state.getBoard();

		// ho il re sotto
		if (rowTo < board.length - 2
				&& state.getPawn(rowTo + 1, columnTo).equalsPawn("K")) {
			// re sul trono
			if (state.getBox(rowTo + 1, columnTo).equals("e5")) {
				if (state.getPawn(5, 4).equalsPawn("B") && state.getPawn(4, 5).equalsPawn("B")
						&& state.getPawn(4, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			// re adiacente al trono
			if (state.getBox(rowTo + 1, columnTo).equals("e4")) {
				if (state.getPawn(3, 3).equalsPawn("B") && state.getPawn(3, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			if (state.getBox(rowTo + 1, columnTo).equals("d5")) {
				if (state.getPawn(4, 2).equalsPawn("B") && state.getPawn(5, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			if (state.getBox(rowTo + 1, columnTo).equals("f5")) {
				if (state.getPawn(4, 6).equalsPawn("B") && state.getPawn(5, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			// sono fuori dalle zone del trono
			if (!state.getBox(rowTo + 1, columnTo).equals("d5")
					&& !state.getBox(rowTo + 1, columnTo).equals("e4")
					&& !state.getBox(rowTo + 1, columnTo).equals("f5")
					&& !state.getBox(rowTo + 1, columnTo).equals("e5")) {
				if (state.getPawn(rowTo + 2, columnTo).equalsPawn("B")
						|| isCitadel(rowTo + 2, columnTo)) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
		}
		return state;
	}

	private State checkCaptureBlackKingUp(State state, Action a) {
		int columnTo = a.getColumnTo();
		int rowTo = a.getRowTo();

		// ho il re sopra
		if (rowTo > 1 && state.getPawn(rowTo - 1, columnTo).equalsPawn("K")) {
			// re sul trono
			if (state.getBox(rowTo - 1, columnTo).equals("e5")) {
				if (state.getPawn(3, 4).equalsPawn("B") && state.getPawn(4, 5).equalsPawn("B")
						&& state.getPawn(4, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			// re adiacente al trono
			if (state.getBox(rowTo - 1, columnTo).equals("e6")) {
				if (state.getPawn(5, 3).equalsPawn("B") && state.getPawn(5, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			if (state.getBox(rowTo - 1, columnTo).equals("d5")) {
				if (state.getPawn(4, 2).equalsPawn("B") && state.getPawn(3, 3).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			if (state.getBox(rowTo - 1, columnTo).equals("f5")) {
				if (state.getPawn(4, 6).equalsPawn("B") && state.getPawn(3, 5).equalsPawn("B")) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
			// sono fuori dalle zone del trono
			if (!state.getBox(rowTo - 1, columnTo).equals("d5")
					&& !state.getBox(rowTo - 1, columnTo).equals("e4")
					&& !state.getBox(rowTo - 1, columnTo).equals("f5")
					&& !state.getBox(rowTo - 1, columnTo).equals("e5")) {
				if (state.getPawn(rowTo - 2, columnTo).equalsPawn("B")
						|| isCitadel(rowTo - 2, columnTo)) {
					state.setTurn(State.Turn.BLACKWIN);
				}
			}
		}
		return state;
	}

	private State checkCaptureBlackPawnRight(State state, Action a) {
		int columnTo = a.getColumnTo();
		int rowTo = a.getRowTo();
		Pawn[][] board = state.getBoard();

		// mangio a destra
		if (columnTo < board.length - 2
				&& state.getPawn(rowTo, columnTo + 1).equalsPawn("W")) {
			if (state.getPawn(rowTo, columnTo + 2).equalsPawn("B")) {
				state.removePawn(rowTo, columnTo + 1);
			}
			if (state.getPawn(rowTo, columnTo + 2).equalsPawn("T")) {
				state.removePawn(rowTo, columnTo + 1);
			}
			if (isCitadel(rowTo, columnTo + 2)) {
				state.removePawn(rowTo, columnTo + 1);
			}
			if (state.getBox(rowTo, columnTo + 2).equals("e5")) {
				state.removePawn(rowTo, columnTo + 1);
			}

		}

		return state;
	}

	private State checkCaptureBlackPawnLeft(State state, Action a) {
		int columnTo = a.getColumnTo();
		int rowTo = a.getRowTo();

		// mangio a sinistra
		if (columnTo > 1 && state.getPawn(rowTo, columnTo - 1).equalsPawn("W")
				&& (state.getPawn(rowTo, columnTo - 2).equalsPawn("B")
						|| state.getPawn(rowTo, columnTo - 2).equalsPawn("T")
						|| isCitadel(rowTo, columnTo - 2)
						|| (state.getBox(rowTo, columnTo - 2).equals("e5")))) {
			state.removePawn(rowTo, columnTo - 1);
		}
		return state;
	}

	private State checkCaptureBlackPawnUp(State state, Action a) {
		int columnTo = a.getColumnTo();
		int rowTo = a.getRowTo();

		// controllo se mangio sopra
		if (rowTo > 1 && state.getPawn(rowTo - 1, columnTo).equalsPawn("W")
				&& (state.getPawn(rowTo - 2, columnTo).equalsPawn("B")
						|| state.getPawn(rowTo - 2, columnTo).equalsPawn("T")
						|| isCitadel(rowTo - 2, columnTo)
						|| (state.getBox(rowTo - 2, columnTo).equals("e5")))) {
			state.removePawn(rowTo - 1, columnTo);
		}
		return state;
	}

	private State checkCaptureBlackPawnDown(State state, Action a) {
		int columnTo = a.getColumnTo();
		int rowTo = a.getRowTo();
		Pawn[][] board = state.getBoard();

		// controllo se mangio sotto
		if (rowTo < board.length - 2
				&& state.getPawn(rowTo + 1, columnTo).equalsPawn("W")
				&& (state.getPawn(rowTo + 2, columnTo).equalsPawn("B")
						|| state.getPawn(rowTo + 2, columnTo).equalsPawn("T")
						|| isCitadel(rowTo + 2, columnTo)
						|| (state.getBox(rowTo + 2, columnTo).equals("e5")))) {
			state.removePawn(rowTo + 1, columnTo);
		}
		return state;
	}

	private State checkCaptureWhite(State state, Action a) {
		int columnTo = a.getColumnTo();
		int rowTo = a.getRowTo();
		Pawn[][] board = state.getBoard();

		// controllo se mangio a destra
		if (columnTo < board.length - 2
				&& state.getPawn(rowTo, columnTo + 1).equalsPawn("B")
				&& (state.getPawn(rowTo, columnTo + 2).equalsPawn("W")
						|| state.getPawn(rowTo, columnTo + 2).equalsPawn("T")
						|| state.getPawn(rowTo, columnTo + 2).equalsPawn("K")
						|| (isCitadel(rowTo, columnTo + 2))
						&& !(columnTo + 2 == 8 && rowTo == 4)
						&& !(columnTo + 2 == 4 && rowTo == 0)
						&& !(columnTo + 2 == 4 && rowTo == 8)
						&& !(columnTo + 2 == 0 && rowTo == 4))) {
			state.removePawn(rowTo, columnTo + 1);
		}
		// controllo se mangio a sinistra
		if (columnTo > 1 && state.getPawn(rowTo, columnTo - 1).equalsPawn("B")
				&& (state.getPawn(rowTo, columnTo - 2).equalsPawn("W")
						|| state.getPawn(rowTo, columnTo - 2).equalsPawn("T")
						|| state.getPawn(rowTo, columnTo - 2).equalsPawn("K")
						|| (isCitadel(rowTo, columnTo - 2))
						&& !(columnTo - 2 == 8 && rowTo == 4)
						&& !(columnTo - 2 == 4 && rowTo == 0)
						&& !(columnTo - 2 == 4 && rowTo == 8)
						&& !(columnTo - 2 == 0 && rowTo == 4))) {
			state.removePawn(rowTo, columnTo - 1);
		}
		// controllo se mangio sopra
		if (rowTo > 1 && state.getPawn(rowTo - 1, columnTo).equalsPawn("B")
				&& (state.getPawn(rowTo - 2, columnTo).equalsPawn("W")
						|| state.getPawn(rowTo - 2, columnTo).equalsPawn("T")
						|| state.getPawn(rowTo - 2, columnTo).equalsPawn("K")
						|| (isCitadel(rowTo - 2, columnTo))
						&& !(columnTo == 8 && rowTo - 2 == 4)
						&& !(columnTo == 4 && rowTo - 2 == 0)
						&& !(columnTo == 4 && rowTo - 2 == 8)
						&& !(columnTo == 0 && rowTo - 2 == 4))) {
			state.removePawn(rowTo - 1, columnTo);
		}
		// controllo se mangio sotto
		if (rowTo < board.length - 2
				&& state.getPawn(rowTo + 1, columnTo).equalsPawn("B")
				&& (state.getPawn(rowTo + 2, columnTo).equalsPawn("W")
						|| state.getPawn(rowTo + 2, columnTo).equalsPawn("T")
						|| state.getPawn(rowTo + 2, columnTo).equalsPawn("K")
						|| (isCitadel(rowTo + 2, columnTo))
						&& !(columnTo == 8 && rowTo + 2 == 4)
						&& !(columnTo == 4 && rowTo + 2 == 0)
						&& !(columnTo == 4 && rowTo + 2 == 8)
						&& !(columnTo == 0 && rowTo + 2 == 4))) {
			state.removePawn(rowTo + 1, columnTo);
		}
		// controllo se ho vinto
		if (rowTo == 0 || rowTo == board.length - 1 || columnTo == 0
				|| columnTo == board.length - 1) {
			if (state.getPawn(rowTo, columnTo).equalsPawn("K")) {
				state.setTurn(State.Turn.WHITEWIN);
			}
		}
		return state;
	}

	private State checkCaptureBlack(State state, Action a) {

		this.checkCaptureBlackPawnRight(state, a);
		this.checkCaptureBlackPawnLeft(state, a);
		this.checkCaptureBlackPawnUp(state, a);
		this.checkCaptureBlackPawnDown(state, a);
		this.checkCaptureBlackKingRight(state, a);
		this.checkCaptureBlackKingLeft(state, a);
		this.checkCaptureBlackKingDown(state, a);
		this.checkCaptureBlackKingUp(state, a);

		return state;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////// MAIN /////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String args[]) {
		Game game = new GameAshtonTablut(99, 0, "garbage", "fake", "fake");
		MiniMax m = new MiniMax(game, Turn.WHITE);

		Action a1;
		State s0 = new StateTablut(), s1 = null;
		s0.setTurn(Turn.WHITE);

		System.out.println("*" +s0.getTurn());

//		for (int i=0; i<9; i++)
//			for (int j=0; j<9; j++)
//				s0.getBoard()[i][j] = Pawn.EMPTY;
//
//
//		s0.getBoard()[1][6] = Pawn.KING;
//		s0.getBoard()[0][3] = Pawn.BLACK;
//		s0.getBoard()[8][3] = Pawn.BLACK;
//		s0.getBoard()[3][6] = Pawn.WHITE;
//		s0.getBoard()[4][5] = Pawn.WHITE;
//		s0.getBoard()[2][6] = Pawn.WHITE;
//		s0.getBoard()[4][4] = Pawn.THRONE;
//		s0.getBoard()[4][0] = Pawn.BLACK;
//		s0.getBoard()[4][1] = Pawn.BLACK;
//		s0.getBoard()[5][0] = Pawn.BLACK;
//		s0.getBoard()[4][4] = Pawn.KING;
//		s0.getBoard()[3][5] = Pawn.BLACK;
//		s0.getBoard()[4][3] = Pawn.BLACK;
//		s0.getBoard()[3][6] = Pawn.WHITE;
//		s0.getBoard()[8][6] = Pawn.BLACK;



		try {
			a1 = m.alphaBetaSearch(s0);
			//s1 = game.checkMove(s0.clone(), a1);
			s1 = m.movePawn(s0.clone(), a1);
			if (s1.getTurn().equalsTurn("W")) m.checkCaptureBlack(s1, a1);		// a questo punto controllo lo stato per eventuali catture
			else if (s1.getTurn().equalsTurn("B")) m.checkCaptureWhite(s1, a1);

			System.out.println("Stato 0: \n" + s0);
			System.out.println("\nAzione 1: " + a1 + " \nStato 1: \n" + s1);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
