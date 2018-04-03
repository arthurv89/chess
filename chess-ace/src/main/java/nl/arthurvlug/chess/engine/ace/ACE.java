package nl.arthurvlug.chess.engine.ace;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialEngineBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.AceEvaluator;
import nl.arthurvlug.chess.engine.customEngine.CustomEngine;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.game.Move;

@Slf4j
public class ACE extends CustomEngine {
	private static final AlphaBetaPruningAlgorithm searchAlgorithm = new AlphaBetaPruningAlgorithm(new AceEvaluator());
	int depth = 4;

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
		
		AceMove aceMove = searchAlgorithm.think(engineBoard, depth);
		return aceMove.toMove();
	}
	
	public int getNodesSearched() {
		return searchAlgorithm.getNodesEvaluated();
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
}
