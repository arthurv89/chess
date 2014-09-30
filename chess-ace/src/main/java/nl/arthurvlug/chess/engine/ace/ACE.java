package nl.arthurvlug.chess.engine.ace;

import java.util.List;

import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.SimplePieceEvaluator;
import nl.arthurvlug.chess.engine.customEngine.CustomEngine;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.game.Move;

public class ACE extends CustomEngine {
	private static final AlphaBetaPruningAlgorithm ALPHA_BETA_PRUNING_ALGORITHM = new AlphaBetaPruningAlgorithm(new SimplePieceEvaluator());
	int depth = 2;

	@Override
	public String getName() {
		return "ACE";
	}

	@Override
	public Move think(List<String> moveList, ThinkingParams thinkingParams) {
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.apply(moveList);
		AceMove aceMove = ALPHA_BETA_PRUNING_ALGORITHM.think(engineBoard, depth);
		return aceMove.toMove();
	}
	
	public int getNodesSearched() {
		return ALPHA_BETA_PRUNING_ALGORITHM.getNodesSearched();
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
}
