package nl.arthurvlug.chess.engine.ace;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.utils.board.FieldUtils;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.NO_PIECE;
import static nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove.fromIdx;
import static nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove.targetIdx;

public class UnapplyableMoveUtils {
	public static int createMove(final String sMove, final ACEBoard aceBoard) {
		byte fromIdx = FieldUtils.fieldIdx(sMove.substring(0, 2));
		byte targetIdx = FieldUtils.fieldIdx(sMove.substring(2, 4));
		return UnapplyableMove.create(fromIdx, targetIdx, aceBoard.coloredPiece(fromIdx), aceBoard.coloredPiece(targetIdx), NO_PIECE);
	}

	public static int createMove(final byte fromIdx,
								 final byte targetIdx,
								 final byte promotionPiece,
								 final ACEBoard aceBoard) {
		return UnapplyableMove.create(fromIdx, targetIdx, aceBoard.coloredPiece(fromIdx), aceBoard.coloredPiece(targetIdx), promotionPiece);
	}

	public static String toString(final int move) {
		byte fromIdx = fromIdx(move);
		byte toIdx = targetIdx(move);
		return FieldUtils.fieldToString(fromIdx) + FieldUtils.fieldToString(toIdx);
	}
}
