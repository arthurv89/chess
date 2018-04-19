package nl.arthurvlug.chess.engine.ace.alphabeta;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.ColorUtils;
import nl.arthurvlug.chess.engine.ace.ColoredPieceType;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.ace.evaluation.BoardEvaluator;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.engine.ace.transpositiontable.HashElement;
import nl.arthurvlug.chess.engine.ace.transpositiontable.TranspositionTable;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;

import static java.util.Collections.swap;
import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.NO_PIECE;
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

	private BoardEvaluator evaluator;
	private final int quiesceMaxDepth;
	private int depth;
//	private static final int HASH_TABLE_LENGTH = 128; // Must be a power or 2
	private static final int HASH_TABLE_LENGTH = 1048576; // Must be a power or 2


	private static final TranspositionTable transpositionTable = new TranspositionTable(HASH_TABLE_LENGTH);
	private boolean quiesceEnabled = true;

	private ACEBoard engineBoard;

	public AlphaBetaPruningAlgorithm(final AceConfiguration configuration) {
		this.evaluator = configuration.getEvaluator();
		this.quiesceMaxDepth = configuration.getQuiesceMaxDepth();
		this.depth = configuration.getSearchDepth();
	}

	public Move think(final ACEBoard engineBoard) {
		Preconditions.checkArgument(depth > 0);
		this.engineBoard = engineBoard;

		cutoffs = 0;
		nodesEvaluated = 0;
		hashHits = 0;

		Optional<Integer> priorityMove = Optional.empty();
		for (int depthNow = 1; depthNow <= depth; depthNow++) {
			final Integer bestMove = alphaBetaRoot(depthNow, priorityMove);
			if(bestMove == null) {
				return null;
			}
			priorityMove = Optional.of(bestMove);
		}
		final int unapplyableMove = priorityMove.get();
		final Optional<PieceType> promotionType = promotionType(unapplyableMove);
		return new Move(
				FieldUtils.coordinates(UnapplyableMove.fromIdx(unapplyableMove)),
				FieldUtils.coordinates(UnapplyableMove.targetIdx(unapplyableMove)),
				promotionType);
	}

	private Optional<PieceType> promotionType(final int unapplyableMove) {
		byte promotionPiece = UnapplyableMove.promotionPiece(unapplyableMove);
		if(promotionPiece == NO_PIECE) {
			return Optional.empty();
		}
		return Optional.of(ColoredPieceType.from(promotionPiece).getPieceType());
	}

	private Integer alphaBetaRoot(final int depth, final Optional<Integer> priorityMove) {
		final List<Integer> generatedMoves = engineBoard.generateMoves();
		reorder(generatedMoves, priorityMove);

		// TODO: Remove
		// TODO: Check for stalemate in the unit test
		Preconditions.checkState(generatedMoves.size() > 0);

		int alpha = OTHER_PLAYER_WINS;
		int beta = CURRENT_PLAYER_WINS;
		Integer bestMove = null;
		boolean white_king_or_rook_queen_side_moved = engineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = engineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = engineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = engineBoard.black_king_or_rook_king_side_moved;
		for(int move : generatedMoves) {
			// Do a recursive search
			engineBoard.apply(move);
//			int score = 0;
			final int score = -alphaBeta(-beta, -alpha, depth - 1);
			engineBoard.unapply(move, white_king_or_rook_queen_side_moved, white_king_or_rook_king_side_moved, black_king_or_rook_queen_side_moved, black_king_or_rook_king_side_moved);

			if (score >= beta) {
				cutoffs++;
				return move;
			}
			if (score > alpha) {
				alpha = score;
				bestMove = move;
			}
		}
		return bestMove;
	}

	private void reorder(final List<Integer> moves, final Optional<Integer> priorityMove) {
		priorityMove.flatMap(prioMove -> {
			Stream<Integer> range = IntStream.range(0, moves.size()).boxed();
			return range
				.flatMap((Integer i) -> findPrioPosition(moves, prioMove, i))
				.findFirst();
		})
		.ifPresent(pos -> swap(moves, 0, pos));
	}

	private Stream<Integer> findPrioPosition(final List<Integer> generatedMoves, final Integer prioMove, final Integer i) {
		if (generatedMoves.get(i).equals(prioMove)) {
			return Stream.of(i);
		}
		return Stream.empty();
	}

	private int alphaBeta(int alpha, final int beta, final int depth) {
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
			return quiesceSearch(alpha, beta, quiesceMaxDepth);
		}
		
		List<Integer> generatedMoves = engineBoard.generateMoves();

		Integer bestMove = null;
		boolean white_king_or_rook_queen_side_moved = engineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = engineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = engineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = engineBoard.black_king_or_rook_king_side_moved;
		for(final Integer move : generatedMoves) {
			// Do a recursive search
			engineBoard.apply(move);
			int score = -alphaBeta(-beta, -alpha, depth-1);
			engineBoard.unapply(move, white_king_or_rook_queen_side_moved, white_king_or_rook_king_side_moved, black_king_or_rook_queen_side_moved, black_king_or_rook_king_side_moved);

			if (score >= beta) {
				cutoffs++;
				transpositionTable.set(depth, score, hashfBETA, move, zobristHash);
				return beta;
			}
			if (score > alpha) {
				bestMove = move;
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

	private int quiesceSearch(int alpha, final int beta, final int depth) {
		int stand_pat = calculateScore(engineBoard);
		if(depth == 0 || !quiesceEnabled) {
			return stand_pat;
		}

		if(engineBoard.hasNoKing()) {
			return OTHER_PLAYER_WINS;
		}
		if (stand_pat >= beta) {
			return beta;
		}
		if( alpha < stand_pat )
			alpha = stand_pat;

//		// IF blackCheck OR whiteCheck : depth ++, extended = true. Else:
//
//

		final List<Integer> takeMoves = engineBoard.generateTakeMoves();
		boolean white_king_or_rook_queen_side_moved = engineBoard.white_king_or_rook_queen_side_moved;
		boolean white_king_or_rook_king_side_moved = engineBoard.white_king_or_rook_king_side_moved;
		boolean black_king_or_rook_queen_side_moved = engineBoard.black_king_or_rook_queen_side_moved;
		boolean black_king_or_rook_king_side_moved = engineBoard.black_king_or_rook_king_side_moved;
		for(Integer takeMove : takeMoves) {
			engineBoard.apply(takeMove);
			final int score = -quiesceSearch(-beta, -alpha, depth-1);
			engineBoard.unapply(takeMove,
					white_king_or_rook_queen_side_moved,
					white_king_or_rook_king_side_moved,
					black_king_or_rook_queen_side_moved,
					black_king_or_rook_king_side_moved);
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

		if (board.getToMove() == ColorUtils.BLACK) {
			return -score;
		}
		return score;
	}

	public void setDepth(final int depth) {
		this.depth = depth;
	}

	public void disableQuesce() {
		this.quiesceEnabled = false;
	}

	public void useSimplePieceEvaluator() {
		this.evaluator = new SimplePieceEvaluator();
	}
}
