package nl.arthurvlug.chess.engine.ace;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceStringUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.NO_PIECE;

public class UnapplyableMoveUtils {
	public static int createMove(final String sMove, final ACEBoard aceBoard) {
		byte fromIdx = FieldUtils.fieldIdx(sMove.substring(0, 2));
		byte targetIdx = FieldUtils.fieldIdx(sMove.substring(2, 4));
		byte promotionPiece = NO_PIECE;
		if(sMove.length() > 4) {
			final char c = sMove.charAt(4);
			final PieceType pieceType = PieceStringUtils.fromChar(c, PieceStringUtils.pieceToCharacterConverter).get();
			promotionPiece = ColoredPieceType.getColoredByte(pieceType, aceBoard.toMove);
		}
		return createMove(fromIdx, targetIdx, promotionPiece, aceBoard);
	}

	public static int createMove(final byte fromIdx,
								 final byte targetIdx,
								 final byte promotionPiece,
								 final ACEBoard aceBoard) {
		// TODO: Change to pieceType(..)
		int movingPiece = aceBoard.coloredPiece(fromIdx);
		int takePiece = aceBoard.coloredPiece(targetIdx);
		return UnapplyableMove.create(fromIdx, targetIdx, movingPiece, takePiece, promotionPiece);
	}

	public static String toString(final int move) {
		byte fromIdx = UnapplyableMove.fromIdx(move);
		byte toIdx = UnapplyableMove.targetIdx(move);
		byte movingPiece = UnapplyableMove.movingPiece(move);
		byte takePiece = UnapplyableMove.takePiece(move);
		byte promotionPiece = UnapplyableMove.promotionPiece(move);
		return String.format("%s%s%s%s (took: %s)",
				PieceUtils.type(movingPiece),
				FieldUtils.fieldToString(fromIdx),
				FieldUtils.fieldToString(toIdx),
				PieceUtils.type(promotionPiece),
				PieceUtils.type(takePiece));
	}
}
