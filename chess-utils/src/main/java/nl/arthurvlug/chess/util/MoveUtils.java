package nl.arthurvlug.chess.util;

import java.util.List;

import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.board.pieces.Color;
import nl.arthurvlug.chess.domain.board.pieces.PieceType;
import nl.arthurvlug.chess.domain.board.pieces.PieceUtils;
import nl.arthurvlug.chess.domain.game.Move;

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
	private static final Function<PieceType, String> TO_CHARACTER = new Function<PieceType, String>() {
		public String apply(PieceType pieceType) {
			return PieceUtils.toCharacterString(pieceType, Color.BLACK); // Lowercase
		}
	};
	
	public static String toEngineMoves(List<Move> moves) {
		return Joiner.on(' ').join(Lists.transform(moves, TO_ENGINE_MOVES));
	}
	
	public static String toEngineMove(Move move) {
		String from = fieldToString(move.getFrom());
		String to = fieldToString(move.getTo());
		String piece = promotionToString(move.getPromotionPiece());
		return from + to + piece;
	}

	public static String promotionToString(Option<PieceType> promotionPiece) {
		return promotionPiece.map(TO_CHARACTER).getOrElse("");
	}

	private static String toReadableField(Coordinates from) {
		return Character.toString((char) (from.getX() + 'a'))
				+ Integer.toString(from.getY() + 1);
	}

	public static String fieldToString(Coordinates coordinates) {
		return toReadableField(coordinates);
	}

	public static Move toMove(String sMove) {
		Coordinates from = toField(sMove.substring(0, 2));
		Coordinates to = toField(sMove.substring(2, 4));
		Option<PieceType> promotionPiece = sMove.length() == 5
				? Option.<PieceType> some(PieceUtils.fromChar(sMove.charAt(4)))
				: Option.<PieceType> none();
		return new Move(from, to, promotionPiece);
	}
	
	private static Coordinates toField(String substring) {
		int x = substring.charAt(0) - 'a';
		int y = substring.charAt(1) - '1';
		return new Coordinates(x, y);
	}
}
