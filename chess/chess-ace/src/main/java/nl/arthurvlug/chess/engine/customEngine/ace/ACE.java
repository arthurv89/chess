package nl.arthurvlug.chess.engine.customEngine.ace;

import java.util.List;

import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.engine.customEngine.CustomEngine;
import nl.arthurvlug.chess.engine.customEngine.EngineBoard;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.engine.customEngine.alphabeta.AlphaBetaPruningAlgorithm;

public class ACE extends CustomEngine {

	@Override
	public String getName() {
		return "ACE";
	}

	@Override
	protected Move think(List<String> moveList, ThinkingParams thinkingParams) {
		EngineBoard engineBoard = new InitialEngineBoard();
		engineBoard.apply(moveList);
		return new AlphaBetaPruningAlgorithm(new SimplePieceEvaluator()).think(engineBoard);
	}
}
