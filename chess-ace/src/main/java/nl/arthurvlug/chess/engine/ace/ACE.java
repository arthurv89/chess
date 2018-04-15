package nl.arthurvlug.chess.engine.ace;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.CustomEngine;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.game.Move;

import static nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration.*;

@Slf4j
public class ACE extends CustomEngine {
	private AlphaBetaPruningAlgorithm searchAlgorithm;

	public ACE() {
		this(DEFAULT_SEARCH_DEPTH, DEFAULT_EVALUATOR, DEFAULT_QUIESCE_MAX_DEPTH);
	}

	public ACE(final int depth) {
		this(depth, DEFAULT_EVALUATOR, DEFAULT_QUIESCE_MAX_DEPTH);
	}

	ACE(final int depth, final BoardEvaluator<ACEBoard, Integer> evaluator, final int quiesceMaxDepth) {
		this(builder()
				.searchDepth(depth)
				.evaluator(evaluator)
				.quiesceMaxDepth(quiesceMaxDepth)
				.build());
	}

	private ACE(final AceConfiguration configuration) {
		this(new AlphaBetaPruningAlgorithm(configuration));
	}

	private ACE(final AlphaBetaPruningAlgorithm algorithm) {
		searchAlgorithm = algorithm;
	}

	@Override
	public String getName() {
		return "ACE";
	}

	@Override
	public Move think(List<String> moveList, ThinkingParams thinkingParams) {
		ACEBoard engineBoard = InitialACEBoard.createInitialACEBoard();
		engineBoard.apply(moveList);
		log.debug("\n{}", engineBoard.string());

		Move think = searchAlgorithm.think(engineBoard);

		log.debug("Nodes evaluated: {}", searchAlgorithm.getNodesEvaluated());
		log.debug("Cut-offs: {}", searchAlgorithm.getCutoffs());
		log.debug("Cache hits: {}", searchAlgorithm.getHashHits());
		return think;
	}
	
	public int getNodesSearched() {
		return searchAlgorithm.getNodesEvaluated();
	}
}
