package nl.arthurvlug.chess.engine.ace.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.evaluation.AceEvaluator;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.ChessEngineConfiguration;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AceConfiguration implements ChessEngineConfiguration<ACEBoard, Integer> {
	public static final int DEFAULT_SEARCH_DEPTH = 2;
	public static final BoardEvaluator<ACEBoard, Integer> DEFAULT_EVALUATOR = new AceEvaluator();
	public static final int DEFAULT_QUIESCE_MAX_DEPTH = 6;

	private BoardEvaluator<ACEBoard, Integer> evaluator = DEFAULT_EVALUATOR;
	private int searchDepth = DEFAULT_SEARCH_DEPTH;
	private int quiesceMaxDepth = DEFAULT_QUIESCE_MAX_DEPTH;

	public AceConfiguration() {

	}
}
