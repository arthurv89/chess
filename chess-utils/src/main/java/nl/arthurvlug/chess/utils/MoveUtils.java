package nl.arthurvlug.chess.utils;

import com.google.common.base.Joiner;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.board.pieces.PieceUtils;
import nl.arthurvlug.chess.utils.game.Move;

public class MoveUtils {
	private static final Function<Move, String> TO_ENGINE_MOVES = move -> toEngineMove(move);
	private static final Function<PieceType, String> TO_CHARACTER = pieceType -> {
		return PieceUtils.toCharacterString(pieceType, Color.BLACK, PieceUtils.pieceToCharacterConverter); // Lowercase
	};
	
	public static String toEngineMoves(List<Move> moves) {
		return Joiner.on(' ').join(moves.stream().map(TO_ENGINE_MOVES).collect(Collectors.toList()));
	}
	
	public static String toEngineMove(Move move) {
		String from = fieldToString(move.getFrom());
		String to = fieldToString(move.getTo());
		String piece = promotionToString(move.getPromotionPiece());
		return from + to + piece;
	}

	public static String promotionToString(Optional<PieceType> promotionPiece) {
		return promotionPiece.map(TO_CHARACTER).orElse("");
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
		Optional<PieceType> promotionPiece = sMove.length() == 5
				? PieceUtils.fromChar(sMove.charAt(4), PieceUtils.pieceToCharacterConverter)
				: Optional.<PieceType> empty();
		return new Move(from, to, promotionPiece);
	}
	
	private static Coordinates toField(String substring) {
		int x = substring.charAt(0) - 'a';
		int y = substring.charAt(1) - '1';
		return new Coordinates(x, y);
	}
}
