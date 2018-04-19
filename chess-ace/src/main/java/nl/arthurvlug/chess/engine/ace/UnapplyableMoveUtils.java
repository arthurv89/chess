package nl.arthurvlug.chess.engine.ace;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.utils.board.FieldUtils;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.NO_PIECE;

public class UnapplyableMoveUtils {
	public static int createMove(final String sMove, final ACEBoard aceBoard) {
		byte fromIdx = FieldUtils.fieldIdx(sMove.substring(0, 2));
		byte targetIdx = FieldUtils.fieldIdx(sMove.substring(2, 4));
		// TODO: Put the promotion piece correctly
		return createMove(fromIdx, targetIdx, NO_PIECE, aceBoard);
	}

	public static int createMove(final byte fromIdx,
								 final byte targetIdx,
								 final byte promotionPiece,
								 final ACEBoard aceBoard) {
		// TODO: Change to pieceType(..)
		int movingPiece = aceBoard.coloredPiece(fromIdx);
		int takePiece = aceBoard.coloredPiece(targetIdx);
		int move = UnapplyableMove.create(fromIdx, targetIdx, movingPiece, takePiece, promotionPiece);
		return move;
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
