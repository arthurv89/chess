package nl.arthurvlug.chess.engine.ace;

import java.util.List;

import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.engine.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.customEngine.CustomEngine;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;

public class ACE extends CustomEngine {

	@Override
	public String getName() {
		return "ACE";
	}

	@Override
	protected Move think(List<String> moveList, ThinkingParams thinkingParams) {
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.apply(moveList);
		return new AlphaBetaPruningAlgorithm(new SimplePieceEvaluator()).think(engineBoard);
	}
}
