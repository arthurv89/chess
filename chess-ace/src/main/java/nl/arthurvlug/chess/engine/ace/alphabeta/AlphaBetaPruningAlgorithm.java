package nl.arthurvlug.chess.engine.ace.alphabeta;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.EngineConstants;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.ace.transpositiontable.HashElement;
import nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.ChessEngineConfiguration;
import nl.arthurvlug.chess.utils.game.Move;

import static java.util.Collections.swap;
import static nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable.*;

@Slf4j
public class AlphaBetaPruningAlgorithm {
	private static final int CURRENT_PLAYER_WINS = 1000000000;
	private static final int OTHER_PLAYER_WINS = -CURRENT_PLAYER_WINS;

	@Getter
	private int nodesEvaluated;
	@Getter
	private int cutoffs;
	@Getter
	private int hashHits;

	private BoardEvaluator<ACEBoard, Integer> evaluator;
	private final int quiesceMaxDepth;
	private int depth;
//	private static final int HASH_TABLE_LENGTH = 128; // Must be a power or 2
	private static final int HASH_TABLE_LENGTH = 1048576; // Must be a power or 2


	private static final TranspositionTable transpositionTable = new TranspositionTable(HASH_TABLE_LENGTH);
	private boolean quiesceEnabled = true;

	public AlphaBetaPruningAlgorithm(final ChessEngineConfiguration<ACEBoard, Integer> configuration) {
		this.evaluator = configuration.getEvaluator();
		this.quiesceMaxDepth = configuration.getQuiesceMaxDepth();
		this.depth = configuration.getSearchDepth();
	}

	public Move think(final ACEBoard engineBoard) {
		Preconditions.checkArgument(depth > 0);

		cutoffs = 0;
		nodesEvaluated = 0;
		hashHits = 0;

		Optional<Move> priorityMove = Optional.empty();
		for (int depthNow = 1; depthNow <= depth; depthNow++) {
			final Move bestMove = alphaBetaRoot(engineBoard, depthNow, priorityMove);
			if(bestMove == null) {
				return null;
			}
			priorityMove = Optional.of(bestMove);
		}
		return priorityMove.get();
	}

	private Move alphaBetaRoot(final ACEBoard engineBoard, final int depth, final Optional<Move> priorityMove) {
		final List<Move> generatedMoves = engineBoard.generateMoves();
		reorder(generatedMoves, priorityMove);
		final List<ACEBoard> successorBoards = engineBoard.generateSuccessorBoards(generatedMoves);

		// TODO: Remove
		Preconditions.checkState(successorBoards.size() > 0);

		int alpha = OTHER_PLAYER_WINS;
		int beta = CURRENT_PLAYER_WINS;
		Move bestMove = null;
		for(ACEBoard successorBoard : successorBoards) {
			// Do a recursive search
			final int score = -alphaBeta(successorBoard, -beta, -alpha, depth - 1);

			final Move lastMove = successorBoard.getLastMove();
			if (score >= beta) {
				cutoffs++;
				return lastMove;
			}
			if (score > alpha) {
				alpha = score;
				bestMove = lastMove;
			}
		}
		return bestMove;
	}

	private void reorder(final List<Move> moves, final Optional<Move> priorityMove) {
		priorityMove.flatMap(prioMove -> {
			Stream<Integer> range = IntStream.range(0, moves.size()).boxed();
			return range
				.flatMap((Integer i) -> findPrioPosition(moves, prioMove, i))
				.findFirst();
		})
		.ifPresent(pos -> swap(moves, 0, pos));
	}

	private Stream<Integer> findPrioPosition(final List<Move> generatedMoves, final Move prioMove, final Integer i) {
		if (generatedMoves.get(i).equals(prioMove)) {
			return Stream.of(i);
		}
		return Stream.empty();
	}

	private int alphaBeta(final ACEBoard engineBoard, int alpha, final int beta, final int depth) {
		if (engineBoard.getFiftyMove() >= 50 || engineBoard.getRepeatedMove() >= 3) {
			return 0;
		}

		int hashf = hashfALPHA;
		int zobristHash = engineBoard.getZobristHash();
		final HashElement hashElement = transpositionTable.get(zobristHash);
		if (hashElement != null) {
			hashHits++;
			if (hashElement.depth >= depth) {
				if (hashElement.flags == hashfEXACT)
					return hashElement.val;
				if ((hashElement.flags == hashfALPHA) && (hashElement.val <= alpha))
					return alpha;
				if ((hashElement.flags == hashfBETA) && (hashElement.val >= beta))
					return beta;
			}
		}

		if (depth == 0) {
			// IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
			return quiesceSearch(engineBoard, alpha, beta, quiesceMaxDepth);
		}
		
		List<Move> generatedMoves = engineBoard.generateMoves();

		final List<ACEBoard> successorBoards = engineBoard.generateSuccessorBoards(generatedMoves);
		Move bestMove = null;
		for(ACEBoard successorBoard : successorBoards) {
			// Do a recursive search
			int score = -alphaBeta(successorBoard, -beta, -alpha, depth-1);

			if (score > alpha) {
				if (score >= beta) {
					cutoffs++;
					transpositionTable.set(depth, score, hashfBETA, successorBoard.getLastMove(), successorBoard.getZobristHash());
					return beta;
				}
				bestMove = successorBoard.getLastMove();
				alpha = score;
				hashf = TranspositionTable.hashfEXACT;
			}
		}

		transpositionTable.set(depth, alpha, hashf, bestMove, zobristHash);
		return alpha;
	}

	private void debug() {
//		System.out.println(cutoffs + "/" + nodesEvaluated);
	}

	private int quiesceSearch(final ACEBoard engineBoard, int alpha, final int beta, final int depth) {
		int stand_pat = calculateScore(engineBoard);
		if(depth == 0 || !quiesceEnabled) {
			return stand_pat;
		}

		if (stand_pat >= beta) {
			return beta;
		}
		if( alpha < stand_pat )
			alpha = stand_pat;

//		// IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
//
//
		if(engineBoard.hasNoKing()) {
			return OTHER_PLAYER_WINS;
		}

		final List<ACEBoard> successorBoards = engineBoard.generateSuccessorTakeBoards();
		for(ACEBoard successorBoard : successorBoards) {
			final int score = -quiesceSearch(successorBoard, -beta, -alpha, depth-1);
//			log.debug("Evaluating board\n{}Score: {}\n", successorBoard, value);

			if (score >= beta) {
				// Beta cut-off
				cutoffs++;
				debug();
//				log.debug("Beta cut-off");
				return beta;
			} else if (score > alpha) {
				alpha = score;
			}
		}
		return alpha;
	}

	private int calculateScore(final ACEBoard board) {
		nodesEvaluated++;
		Integer score = evaluator.evaluate(board);

		if (board.getToMove() == EngineConstants.BLACK) {
			return -score;
		}
		return score;
	}

	void setDepth(final int depth) {
		this.depth = depth;
	}

	void disableQuesce() {
		this.quiesceEnabled = false;
	}

	void useSimplePieceEvaluator() {
		this.evaluator = new SimplePieceEvaluator();
	}
}
