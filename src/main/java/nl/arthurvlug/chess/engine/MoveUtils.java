package nl.arthurvlug.chess.engine;

import java.util.List;

import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.game.Move;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class MoveUtils {
	private static final Function<Move, String> TO_ENGINE_MOVES = new Function<Move, String>() {
		public String apply(Move move) {
			return toEngineMove(move);
		}
	};
	
	public static String toEngineMoves(List<Move> moves) {
		return Joiner.on(' ').join(Lists.transform(moves, TO_ENGINE_MOVES));
	}
	
	private static String toEngineMove(Move move) {
		String from = toReadableField(move.getFrom());
		String to = toReadableField(move.getTo());
		return from + to;
	}

	private static String toReadableField(Coordinates from) {
		return Character.toString((char) (from.getX() + 'a'))
				+ Integer.toString(from.getY() + 1);
	}
}
