package nl.arthurvlug.chess.engine.ace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import nl.arthurvlug.chess.engine.customEngine.ThinkingParams;
import nl.arthurvlug.chess.utils.game.Move;

@ToString
@AllArgsConstructor
@Getter
public class IncomingState {
	private final Move move;
	private final ThinkingParams thinkingParams;
}
