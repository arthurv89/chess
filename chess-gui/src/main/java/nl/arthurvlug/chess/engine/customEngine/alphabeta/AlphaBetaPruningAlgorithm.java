package nl.arthurvlug.chess.engine.customEngine.alphabeta;

import java.util.ArrayList;
import java.util.List;

import nl.arthurvlug.chess.domain.board.Board;
import nl.arthurvlug.chess.domain.board.Field;
import nl.arthurvlug.chess.domain.board.pieces.Color;
import nl.arthurvlug.chess.domain.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.CheckmateScore;
import nl.arthurvlug.chess.engine.customEngine.EngineBoard;
import nl.arthurvlug.chess.engine.customEngine.Evaluation;

public class AlphaBetaPruningAlgorithm {
	private static final int WHITE_WINS = Integer.MAX_VALUE;
	private static final int BLACK_WINS = -Integer.MAX_VALUE;
	private int nodesSearched = 0;
	
	private BoardEvaluator evaluator;

	public AlphaBetaPruningAlgorithm(BoardEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public Move think() {
		nodesSearched = 0;
		
		EngineBoard engineBoard = new EngineBoard(moveList);
		int depth = 1;
		Move move = alphaBetaRoot(engineBoard, depth);
		return move;
	}
	
	private Move alphaBetaRoot(final EngineBoard board, final int depth) {
		int alpha = BLACK_WINS;
		final int beta = WHITE_WINS;
		
		EngineBoard bestEngineBoard = new EngineBoard(Integer.MIN_VALUE);
		List<Board> succ = new ArrayList<>();
		
		Color succColor = board.toMove.other();
		
		for(int fieldNo=0; fieldNo<64; fieldNo++) {
			Field field = board.getField(fieldNo);
			if(field.getPiece().isEmpty()) {
				continue;
			}
			
			ColoredPiece piece = field.getPiece().get();
			if(piece.getColor() != board.toMove) {
				continue;
			}
			
			for(Move moves : pieceMoves(field, piece)) {
				EngineBoard copyBoard = new EngineBoard(board).move(move);
				List<Move> copyBoardMoves = generateValidMoves(copyBoard);
				
				if(succColor == Color.WHITE && whiteCheck(board, copyBoardMoves)) {
					continue;
				}
				if(succColor == Color.BLACK && blackCheck(board, copyBoardMoves)) {
					continue;
				}
				
				Evaluation evaluation = evaluator.evaluate(copyBoard);
				sideToMoveScore(evaluation, succColor);
				
				succ.add(board);
			}
		}
		
		succ.sort(scoreComparator);
		
		for(EngineBoard engineBoard : succ) {
			Score value = -alphaBeta(engineBoard, 1, -beta, -alpha);
			
			if(value instanceof CheckmateScore) {
				return engineBoard.lastMove();
			}
		}
		
		int plyDepthReached = modifyDepth(depth-1, succ.positions.count);
		
		int currentEngineBoard = 0;
		alpha = BLACK_WINS;
		succ.positions.sort();
		
		for(EngineBoard engineBoard : succ) {
			currentBoard++;
			int value = alphaBetaRoot(pos, plyDepthReached, -beta, -alpha);
			pos.score = value;
			if(value > alpha) {
				alpha = value;
				bestEngineBoard = new Board(engineBoard);
			}
		}
		return bestEngineBoard.lastMove();
	}

	private int alphaBeta(EngineBoard engineBoard, Color toMove, int depth, int alpha, int beta) {
		nodesSearched++;
		
		if(engineBoard.fiftyMove >= 50 || engineBoard.repeatedMove >= 3) {
			return 0;
		}
		
		if(depth == 0) {
			Evaluation score = evaluator.evaluate(engineBoard);
			return sideToMoveScore(score, toMove);
		}
		
		List<Position> positions = evaluateMoves(engineBoard);
		if(engineBoard.whiteCheck() || engineBoard.blackCheck() || positions.size > 0) {
			if(isMate(toMove, engineBoard, engineBoard.whiteCheck() || engineBoard.blackCheck(), engineBoard.stalemate())) {
				if(engineBoard.blackMate()) {
					if(toMove == Color.BLACK) {
						return BLACK_WINS - depth;
					} else {
						return WHITE_WINS + depth;
					}
				}
				
				if(engineBoard.whiteMate()) {
					if(toMove == Color.BLACK) {
						return WHITE_WINS + depth;
					} else {
						return BLACK_WINS - depth;
					}
				}
				
				return 0;
			}
		}
		
		sort(positions);
		for(Position move : positions) {
			EngineBoard copyEngineBoard = engineBoard.copy(move);
			generateMoves(engineBoard);
			
			if(engineBoard.blackCheck() && toMove == Color.BLACK) {
				continue; // Invalid move
			}
			
			if(engineBoard.whiteCheck() && toMove == Color.WHITE) {
				continue; // Invalid move
			}
			
			int value = -alphaBeta(copyBoard, toMove.other(), depth-1, -beta, -alpha);
			
			if(value >= beta) {
				// Beta cut-off
				return beta;
			} else if(value > alpha) {
				alpha = value;
			}
		}
		return alpha;		
	}
//
}
