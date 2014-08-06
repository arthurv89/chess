package nl.arthurvlug.chess.engine.customEngine.alphabeta;

import java.util.PriorityQueue;

import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.EngineBoard;
import nl.arthurvlug.chess.engine.customEngine.EvaluatedMove;
import nl.arthurvlug.chess.engine.customEngine.Evaluation;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.MoveGenerator;

public class ABPruning {
	public Move think(final EngineBoard board, final int toMove, final int depth, final BoardEvaluator evaluator, final int alpha, final int beta) {
		PriorityQueue<EvaluatedMove> sortedMoves = new PriorityQueue<>();
		EngineBoard engineBoard = new EngineBoard(board);
		for(Move move : MoveGenerator.generateMoves(engineBoard)) {
			EngineBoard movedBoard = engineBoard.move(move);
			if(movedBoard.inCheck(toMove)) {
				continue;
			}
			Evaluation evaluation = evaluator.evaluate(movedBoard);
			sortedMoves.add(new EvaluatedMove(move, evaluation));
		}
		EvaluatedMove bestMove = sortedMoves.peek();
		EngineBoard bestMoveBoard = board.move(bestMove.getMove());
		return think(bestMoveBoard, toMove, depth-1, evaluator, -beta, -alpha);
	}
}
