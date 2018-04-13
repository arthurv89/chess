package nl.arthurvlug.chess.engine.utils;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.utils.StringToBoardConverter;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.Color;

public class ACEBoardUtils {
	public static ACEBoard initializedBoard(final Color toMoveColor, String board) {
		final ACEBoard startPositionBoard = new ACEBoard(toMove(toMoveColor), false);
		StringToBoardConverter.conv(board, ((coordinates, coloredPiece) -> {
			startPositionBoard.addPiece(toMove(coloredPiece.getColor()), coloredPiece.getPieceType(), FieldUtils.fieldIdx(coordinates));
			return null;
		}));
		startPositionBoard.finalizeBitboards();
		return startPositionBoard;
	}

	private static int toMove(final Color toMoveColor) {
		return toMoveColor.isWhite() ? 0 : 1;
	}
}
