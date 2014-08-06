package nl.arthurvlug.chess.engine.alphabeta;

import java.util.PriorityQueue;

import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.engine.ace.ACEBoard;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.EvaluatedMove;
import nl.arthurvlug.chess.engine.customEngine.Evaluation;
import nl.arthurvlug.chess.engine.movegeneration.MoveGenerator;

public class ABPruning {
	public Move think(final ACEBoard board, final int toMove, final int depth, final BoardEvaluator evaluator, final int alpha, final int beta) {
		PriorityQueue<EvaluatedMove> sortedMoves = new PriorityQueue<>();
		ACEBoard engineBoard = new ACEBoard(board);
		for(Move move : MoveGenerator.generateMoves(engineBoard)) {
			ACEBoard movedBoard = engineBoard.move(move);
			if(movedBoard.inCheck(toMove)) {
				continue;
			}
			Evaluation evaluation = evaluator.evaluate(movedBoard);
			sortedMoves.add(new EvaluatedMove(move, evaluation));
		}
		EvaluatedMove bestMove = sortedMoves.peek();
		ACEBoard bestMoveBoard = board.move(bestMove.getMove());
		return think(bestMoveBoard, toMove, depth-1, evaluator, -beta, -alpha);
	}
}
