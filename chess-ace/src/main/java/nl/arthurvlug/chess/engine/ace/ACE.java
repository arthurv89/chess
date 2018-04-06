package nl.arthurvlug.chess.engine.ace;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;
import nl.arthurvlug.chess.engine.customEngine.CustomEngine;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.game.Move;

@Slf4j
public class ACE extends CustomEngine {
	private AlphaBetaPruningAlgorithm searchAlgorithm;

	public ACE() {
		searchAlgorithm = new AlphaBetaPruningAlgorithm<ACEBoard>(new AceConfiguration());
	}

	public ACE(final int depth) {
		final AceConfiguration configuration = new AceConfiguration();
		configuration.setSearchDepth(depth);
		searchAlgorithm = new AlphaBetaPruningAlgorithm<ACEBoard>(configuration);
	}

	@Override
	public String getName() {
		return "ACE";
	}

	@Override
	public Move think(List<String> moveList, ThinkingParams thinkingParams) {
		ACEBoard engineBoard = new InitialEngineBoard();
		engineBoard.finalizeBitboards();
		engineBoard.apply(moveList);
		log.debug("\n{}", engineBoard);

		Move think = searchAlgorithm.think(engineBoard);
		return think;
	}
	
	public int getNodesSearched() {
		return searchAlgorithm.getNodesEvaluated();
	}
}
