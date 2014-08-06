package nl.arthurvlug.chess.engine.customEngine.alphabeta;

import java.util.PriorityQueue;

import nl.arthurvlug.chess.domain.board.Board;
import nl.arthurvlug.chess.domain.board.pieces.Color;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.EvaluatedMove;
import nl.arthurvlug.chess.engine.customEngine.Evaluation;

public class ABPruning {
	public Move think(Board board, Color toMove, int depth, BoardEvaluator evaluator, int alpha, int beta) {
		PriorityQueue<EvaluatedMove> sortedMoves = new PriorityQueue<>();
		for(Move move : possibleMoves()) {
			Board movedBoard = board.move(move);
			if(movedBoard.inCheck(toMove)) {
				continue;
			}
			Evaluation evaluation = evaluator.evaluate(movedBoard);
			sortedMoves.add(new EvaluatedMove(move, evaluation));
		}
		EvaluatedMove bestMove = sortedMoves.peek();
		Board bestMoveBoard = board.move(bestMove.getMove());
		return think(bestMoveBoard, toMove, depth-1, evaluator, -beta, -alpha);
	}
}
