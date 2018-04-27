package nl.arthurvlug.chess.engine.ace;

import com.google.common.eventbus.EventBus;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.engine.ace.alphabeta.AlphaBetaPruningAlgorithm;
import nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration;
import nl.arthurvlug.chess.engine.ace.evaluation.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.CustomEngine;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.game.Move;
import rx.Observable;

import static nl.arthurvlug.chess.engine.ace.configuration.AceConfiguration.*;

@Slf4j
public class ACE extends CustomEngine {
	private final int initialClockTime;
	private AlphaBetaPruningAlgorithm searchAlgorithm;
	private final EventBus eventBus;

	public ACE(final int depth, final int initialClockTime, final String name, EventBus eventBus) {
		this(depth, DEFAULT_EVALUATOR, DEFAULT_QUIESCE_MAX_DEPTH, initialClockTime, name, eventBus);
	}

	public ACE(final int depth, final BoardEvaluator evaluator, final int quiesceMaxDepth, int initialClockTime, final String name, final EventBus eventBus) {
		this(builder()
				.searchDepth(depth)
				.evaluator(evaluator)
				.quiesceMaxDepth(quiesceMaxDepth)
				.build(), initialClockTime, name, eventBus);
	}

	private ACE(final AceConfiguration configuration, final int initialClockTime, final String name, EventBus eventBus) {
		this(new AlphaBetaPruningAlgorithm(configuration, initialClockTime), initialClockTime, name, eventBus);
	}

	private ACE(final AlphaBetaPruningAlgorithm algorithm, final int initialClockTime, final String name, EventBus eventBus) {
		this.initialClockTime = initialClockTime;
		this.searchAlgorithm = algorithm;
		this.eventBus = eventBus;
		searchAlgorithm.setName(name);
		searchAlgorithm.setEventBus(eventBus);
	}

	@Override
	public String getName() {
		return "ACE";
	}

	@Override
	public Observable<Move> startThinking() {
		return searchAlgorithm.startThinking();
	}

	@Override
	public void go(final List<String> moveStringList, final ThinkingParams thinkingParams) {
		final Move move = moveStringList.size() > 0
				? MoveUtils.toMove(moveStringList.get(moveStringList.size()-1))
				: null;
		System.out.println("Go. New move: " + move);
		searchAlgorithm.getIncomingMoves().onNext(new IncomingState(move, thinkingParams));
	}
	
	public int getNodesSearched() {
		return searchAlgorithm.getNodesEvaluated();
	}
}
