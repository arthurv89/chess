package nl.arthurvlug.chess.utils;

import com.google.common.base.Joiner;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.PieceStringUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;

import static nl.arthurvlug.chess.utils.board.FieldUtils.fieldToString;

public class MoveUtils {
	private static final Function<Move, String> TO_ENGINE_MOVES = move -> toEngineMove(move);
	private static final Function<PieceType, String> TO_CHARACTER = pieceType -> {
		return PieceStringUtils.toCharacterString(pieceType, Color.BLACK, PieceStringUtils.pieceToCharacterConverter); // Lowercase
	};
	public static boolean DEBUG = false;

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

	public static Move toMove(String sMove) {
		Coordinates from = FieldUtils.coordinates(sMove.substring(0, 2));
		Coordinates to = FieldUtils.coordinates(sMove.substring(2, 4));
		Optional<PieceType> promotionPiece = sMove.length() == 5
				? PieceStringUtils.fromChar(sMove.charAt(4), PieceStringUtils.pieceToCharacterConverter)
				: Optional.empty();
		return new Move(from, to, promotionPiece);
	}
}
