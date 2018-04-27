package nl.arthurvlug.chess.engine.ace;

import java.util.List;
import java.util.stream.Collectors;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceStringUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.BLACK_KING_BYTE;
import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.NO_PIECE;
import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.WHITE_KING_BYTE;

public class UnapplyableMoveUtils {
	public static int createMove(final String sMove, final ACEBoard engineBoard) {
		byte fromIdx = FieldUtils.fieldIdx(sMove.substring(0, 2));
		byte targetIdx = FieldUtils.fieldIdx(sMove.substring(2, 4));
		byte promotionPiece = NO_PIECE;
		if(sMove.length() > 4) {
			final char c = sMove.charAt(4);
			final PieceType pieceType = PieceStringUtils.fromChar(c, PieceStringUtils.pieceToCharacterConverter).get();
			promotionPiece = ColoredPieceType.getColoredByte(pieceType, engineBoard.toMove);
		}
		try {
			return createMove(fromIdx, targetIdx, promotionPiece, engineBoard);
		} catch (KingEatingException e) {
			throw new RuntimeException(e);
		}
	}

	public static int createMove(final byte fromIdx,
								 final byte targetIdx,
								 final byte promotionPiece,
								 final ACEBoard engineBoard) throws KingEatingException {
		// TODO: Change to pieceType(..)
		int coloredMovingPiece = engineBoard.coloredPiece(fromIdx);
		int takePiece = engineBoard.coloredPiece(targetIdx);
		int move = UnapplyableMove.create(fromIdx, targetIdx, coloredMovingPiece, takePiece, promotionPiece);
		if(takePiece == BLACK_KING_BYTE || takePiece == WHITE_KING_BYTE) {
			throw new KingEatingException(move);
		}
		return move;
	}

	public static String toString(final int move) {
		byte fromIdx = UnapplyableMove.fromIdx(move);
		byte toIdx = UnapplyableMove.targetIdx(move);
		byte movingPiece = UnapplyableMove.coloredMovingPiece(move);
		byte takePiece = UnapplyableMove.takePiece(move);
		byte promotionPiece = UnapplyableMove.promotionPiece(move);
		return String.format("%s%s%s%s (took: %s)",
				PieceUtils.type(movingPiece),
				FieldUtils.fieldToString(fromIdx),
				FieldUtils.fieldToString(toIdx),
				PieceUtils.type(promotionPiece),
				PieceUtils.type(takePiece));
	}

	public static String toShortString(final int move) {
		byte fromIdx = UnapplyableMove.fromIdx(move);
		byte toIdx = UnapplyableMove.targetIdx(move);
		byte promotionPiece = UnapplyableMove.promotionPiece(move);
		return String.format("%s%s%s",
				FieldUtils.fieldToString(fromIdx),
				FieldUtils.fieldToString(toIdx),
				PieceUtils.type(promotionPiece));
	}

	public static List<String> listToString(final List<Integer> moves) {
		return moves.stream().map(m -> UnapplyableMoveUtils.toString(m)).collect(Collectors.toList());
	}
}
