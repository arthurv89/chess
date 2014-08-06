package nl.arthurvlug.chess.engine.ace.alphabeta;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Position;

import nl.arthurvlug.chess.engine.EngineUtils;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.MoveGenerator;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.CheckmateScore;
import nl.arthurvlug.chess.engine.customEngine.Evaluation;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.game.Move;

public class AlphaBetaPruningAlgorithm {
	private static final int WHITE_WINS = Integer.MAX_VALUE;
	private static final int BLACK_WINS = -Integer.MAX_VALUE;
	private int nodesSearched = 0;
	
	private BoardEvaluator evaluator;

	public AlphaBetaPruningAlgorithm(BoardEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public Move think(ACEBoard engineBoard) {
		nodesSearched = 0;
		
		int depth = 1;
		Move move = alphaBetaRoot(engineBoard, depth);
		return move;
	}
	
	private Move alphaBetaRoot(final ACEBoard board, final int depth) {
		int alpha = BLACK_WINS;
		final int beta = WHITE_WINS;
		
		ACEBoard bestEngineBoard = new ACEBoard(Integer.MIN_VALUE);
		List<ACEBoard> succ = new ArrayList<>();
		
		int succColor = EngineUtils.otherToMove(board.toMove);

		List<Move> copyEngineBoardMoves = MoveGenerator.generateMoves(board);
		for(Move move : copyEngineBoardMoves) {
			ColoredPiece piece = move.get();
			
			for(Move move : copyEngineBoardMoves) {
				ACEBoard copyEngineBoard = new ACEBoard(board).move(move);
				
				if(EngineUtils.isWhite(succColor) && whiteCheck(board, copyEngineBoardMoves)) {
					continue;
				}
				if(EngineUtils.isBlack(succColor) && blackCheck(board, copyEngineBoardMoves)) {
					continue;
				}
				
				Evaluation evaluation = evaluator.evaluate(copyEngineBoard);
				sideToMoveScore(evaluation, succColor);
				
				succ.add(board);
			}
		}
		
		succ.sort(scoreComparator);
		
		for(ACEBoard engineBoard : succ) {
			Score value = -alphaBeta(engineBoard, succColor, 1, -beta, -alpha);
			
			if(value instanceof CheckmateScore) {
				return engineBoard.lastMove();
			}
		}
		
		int plyDepthReached = modifyDepth(depth-1, succ.positions.count);
		
		int currentEngineBoard = 0;
		alpha = BLACK_WINS;
		succ.positions.sort();
		
		for(ACEBoard engineBoard : succ) {
			currentEngineBoard++;
			int value = alphaBetaRoot(pos, plyDepthReached, -beta, -alpha);
			pos.score = value;
			if(value > alpha) {
				alpha = value;
				bestEngineBoard = new ACEBoard(engineBoard);
			}
		}
		return bestEngineBoard.lastMove();
	}

	private int alphaBeta(ACEBoard engineBoard, int succColor, int depth, int alpha, int beta) {
		nodesSearched++;
		
		if(engineBoard.fiftyMove >= 50 || engineBoard.repeatedMove >= 3) {
			return 0;
		}
		
		if(depth == 0) {
			Evaluation score = evaluator.evaluate(engineBoard);
			return sideToMoveScore(score, succColor);
		}
		
		List<Position> positions = evaluateMoves(engineBoard);
		if(engineBoard.whiteCheck() || engineBoard.blackCheck() || positions.size > 0) {
			if(isMate(succColor, engineBoard, engineBoard.whiteCheck() || engineBoard.blackCheck(), engineBoard.stalemate())) {
				if(engineBoard.blackMate()) {
					if(succColor.isBlack()) {
						return BLACK_WINS - depth;
					} else {
						return WHITE_WINS + depth;
					}
				}
				
				if(engineBoard.whiteMate()) {
					if(succColor.isBlack()) {
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
			ACEBoard copyEngineBoard = engineBoard.copy(move);
			generateMoves(engineBoard);
			
			if(engineBoard.blackCheck() && succColor.isBlack()) {
				continue; // Invalid move
			}
			
			if(engineBoard.whiteCheck() && succColor.isWhite()) {
				continue; // Invalid move
			}
			
			int value = -alphaBeta(copyEngineBoard, succColor.other(), depth-1, -beta, -alpha);
			
			if(value >= beta) {
				// Beta cut-off
				return beta;
			} else if(value > alpha) {
				alpha = value;
			}
		}
		return alpha;		
	}
}
