package nl.arthurvlug.chess.engine.ace.movegeneration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import nl.arthurvlug.chess.engine.ace.KingEatingException;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

import static nl.arthurvlug.chess.engine.ColorUtils.opponent;
import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.NO_PIECE;
import static nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator.promotionTypes;
import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldIdx;

public class AceTakeMoveGenerator {
	// TODO: Replace with inverted Xrays.
	// For each occupied field by the current player,it should calculate which fields can attack them
	public static List<Integer> generateTakeMoves(final ACEBoard engineBoard, AceMoveGenerator aceMoveGenerator) throws KingEatingException {
		return aceMoveGenerator.generateMoves(engineBoard)
				.stream()
				.filter(move -> {
					byte idx = UnapplyableMove.targetIdx(move);
					return engineBoard.getPieces()[idx] != NO_PIECE;
				})
				.collect(Collectors.toList());
	}

	public static List<Integer> generateTakeMovesBla(final ACEBoard engineBoard) throws KingEatingException {
		List<Integer> moves = new ArrayList<>();
		long enemy_board = engineBoard.occupiedSquares[opponent(engineBoard.toMove)];
		while(enemy_board != 0L) {
			byte sq = (byte) Long.numberOfTrailingZeros(enemy_board);

			final long attackingPiecesBitboard = attackingPiecesBitboard(sq, engineBoard);
			boolean isPromotionMove = AceMoveGenerator.pawnMoveIsPromotionMove(sq);
			moves.addAll(createAttackMoves(sq, attackingPiecesBitboard, engineBoard, isPromotionMove));

			enemy_board -= bitboardFromFieldIdx(sq);
		}
		return moves;
	}

	private static List<Integer> createAttackMoves(byte fromIdx, long bitboard, final ACEBoard engineBoard, final boolean isPromotionMove) throws KingEatingException {
		List<Integer> moves = new ArrayList<>();

		while(bitboard != 0L) {
			byte targetIdx = (byte) Long.numberOfTrailingZeros(bitboard);
			// TODO: Change into array
			// TODO: Move this log to the pawn move so it's only executed there
			if(isPromotionMove) {
				for(final byte pieceType : promotionTypes[engineBoard.toMove]) {
					Integer move = UnapplyableMoveUtils.createMove(fromIdx, targetIdx, pieceType, engineBoard);
					moves.add(move);
				}
			} else {
				Integer move = UnapplyableMoveUtils.createMove(fromIdx, targetIdx, NO_PIECE, engineBoard);
				moves.add(move);
			}

			bitboard -= bitboardFromFieldIdx(targetIdx);
		}
		return moves;
	}

	private static long attackingPiecesBitboard(final byte sq, final ACEBoard engineBoard) {
		// TODO: Change this also in an array
		final long[][] xray = Xray.attacking_xray[engineBoard.toMove];
		long result = 0L;
		for (PieceType pieceType : PieceType.values()) {
			final long attackBitboard = xray[pieceType.getPieceByte()][sq];
			result |= (attackBitboard & piecesBitboard(engineBoard, pieceType));
		}
		return result;
	}

	// TODO: Change into an array in ACEBoard
	private static long piecesBitboard(final ACEBoard engineBoard, final PieceType pieceType) {
		if(engineBoard.toMove == 0) {
			switch (pieceType) {
				case PAWN: return engineBoard.white_pawns;
				case KNIGHT: return engineBoard.white_knights;
				case BISHOP: return engineBoard.white_bishops;
				case ROOK: return engineBoard.white_rooks;
				case QUEEN: return engineBoard.white_queens;
				case KING: return engineBoard.white_kings;
			}
		} else {
			switch (pieceType) {
				case PAWN: return engineBoard.black_pawns;
				case KNIGHT: return engineBoard.black_knights;
				case BISHOP: return engineBoard.black_bishops;
				case ROOK: return engineBoard.black_rooks;
				case QUEEN: return engineBoard.black_queens;
				case KING: return engineBoard.black_kings;
			}
		}
		throw new RuntimeException("Piece not supported " + pieceType);
	}
}
