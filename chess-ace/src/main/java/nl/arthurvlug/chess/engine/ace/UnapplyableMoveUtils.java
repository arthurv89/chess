package nl.arthurvlug.chess.engine.ace;

import java.util.Optional;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.board.pieces.PieceUtils;

import static nl.arthurvlug.chess.utils.MoveUtils.toField;

public class UnapplyableMoveUtils {
	public static UnapplyableMove toMove(final String sMove, final ACEBoard aceBoard) {
		Coordinates from = toField(sMove.substring(0, 2));
		Coordinates to = toField(sMove.substring(2, 4));
		Optional<PieceType> promotionPiece = sMove.length() == 5
				? PieceUtils.fromChar(sMove.charAt(4), PieceUtils.pieceToCharacterConverter)
				: Optional.empty();
		return new UnapplyableMove(from, to, promotionPiece, aceBoard.pieceAt(to));
	}

	public static UnapplyableMove toMove(final Coordinates fromCoordinate, final Coordinates toCoordinate, final Optional<PieceType> promotionPiece, final ACEBoard engineBoard) {
		return new UnapplyableMove(fromCoordinate, toCoordinate, promotionPiece, engineBoard.pieceAt(toCoordinate));
	}
}
