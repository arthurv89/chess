package nl.arthurvlug.chess.engine.ace.alphabeta;

import java.util.PriorityQueue;

import nl.arthurvlug.chess.engine.ace.AceMove;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.MoveGenerator;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.EvaluatedMove;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;
import nl.arthurvlug.chess.utils.game.Move;

public class ABPruning {
	public Move think(final ACEBoard board, final int toMove, final int depth, final BoardEvaluator evaluator, final int alpha, final int beta) {
		PriorityQueue<EvaluatedMove> sortedMoves = new PriorityQueue<>();
		for(AceMove move : MoveGenerator.generateMoves(board)) {
			ACEBoard movedBoard = new ACEBoard(board);
			movedBoard.finalizeBitboards();
			movedBoard.apply(move);
			if(movedBoard.currentPlayerInCheck) {
				continue;
			}
			
			// TODO: CheckmateScore?
			NormalScore evaluation = (NormalScore) evaluator.evaluate(movedBoard);
			sortedMoves.add(new EvaluatedMove(move, evaluation));
		}
		EvaluatedMove bestMove = sortedMoves.peek();
		
		ACEBoard bestBoard = new ACEBoard(board);
		bestBoard.apply(bestMove.getMove());
		return think(bestBoard, toMove, depth-1, evaluator, -beta, -alpha);
	}
}
