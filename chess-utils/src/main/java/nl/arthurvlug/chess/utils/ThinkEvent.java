package nl.arthurvlug.chess.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class ThinkEvent {
	private int nodesEvaluated;
}
