package nl.arthurvlug.chess.engine;

import java.util.List;

import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.pieces.Piece;

import com.atlassian.fugue.Option;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class MoveUtils {
	private static final Function<Move, String> TO_ENGINE_MOVES = new Function<Move, String>() {
		public String apply(Move move) {
			return toEngineMove(move);
		}
	};
	private static final Function<Piece, String> TO_CHARACTER = new Function<Piece, String>() {
		public String apply(Piece input) {
			return String.valueOf(input.getCharacter()).toLowerCase();
		}
	};
	
	public static String toEngineMoves(List<Move> moves) {
		return Joiner.on(' ').join(Lists.transform(moves, TO_ENGINE_MOVES));
	}
	
	private static String toEngineMove(Move move) {
		String from = fieldToString(move.getFrom());
		String to = fieldToString(move.getTo());
		String piece = promotionToString(move.getPromotionPiece());
		return from + to + piece;
	}

	public static String promotionToString(Option<Piece> promotionPiece) {
		return promotionPiece.map(TO_CHARACTER).getOrElse("");
	}

	private static String toReadableField(Coordinates from) {
		return Character.toString((char) (from.getX() + 'a'))
				+ Integer.toString(from.getY() + 1);
	}

	public static String fieldToString(Coordinates coordinates) {
		return toReadableField(coordinates);
	}
}
