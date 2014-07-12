package nl.arthurvlug.chess.domain.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import nl.arthurvlug.chess.domain.board.Board;

@AllArgsConstructor
@Value
@Getter
public class BoardInitializedEvent {
	private final Board board;
	private final Player toMove;
}
