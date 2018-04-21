package nl.arthurvlug.chess.engine.ace.evaluation;

import com.google.common.collect.LinkedHashMultimap;
import java.util.List;
import java.util.Set;
import nl.arthurvlug.chess.engine.ace.PieceUtils;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.game.Move;

import static nl.arthurvlug.chess.engine.ace.ColoredPieceType.*;

public class AceEvaluator extends BoardEvaluator {
	private int whiteBishopCount;
	private int blackBishopCount;

	@Override
	public Integer evaluate(final ACEBoard aceBoard) {
//		final List<Integer> moves = AceMoveGenerator.generateMoves(aceBoard);
//		LinkedHashMultimap<Byte, Integer> byFromPositionMap = byFromPosition(moves);
		int score = 0;

		long occupiedBoard = aceBoard.occupied_board;
		while(occupiedBoard != 0L) {
			final byte fieldIdx = (byte) Long.numberOfTrailingZeros(occupiedBoard);

			final short coloredPiece = aceBoard.coloredPiece(fieldIdx);
			score += pieceScore(fieldIdx, coloredPiece);
			occupiedBoard ^= 1L << fieldIdx;
		}
		return score;
	}


	// TODO: Make more efficient by using an array with in there the pieces?
	private LinkedHashMultimap<Byte, Integer> byFromPosition(final List<Integer> moves) {
		final LinkedHashMultimap<Byte, Integer> multiMap = LinkedHashMultimap.create();
		for (final Integer move : moves) {
			final byte fromIdx = UnapplyableMove.fromIdx(move);
			multiMap.put(fromIdx, move);
		}
		return multiMap;
	}


	private int pieceScore(final int fieldIdx, final short coloredPiece) {
		int totalScore = 0;

		final int pieceValue = ACEConstants.pieceValues[coloredPiece];
		totalScore += pieceValue;

		final int extraScore = extraScore(fieldIdx, coloredPiece);
		totalScore += extraScore;

//		int mobilityScore = mobilityScore(moves);
//		totalScore += mobilityScore;

		return PieceUtils.isWhitePiece(coloredPiece)
				? totalScore
				: -totalScore;
	}


	private int mobilityScore(Set<Move> moves) {
		return moves.size();
	}


	private int extraScore(final int fieldIdx, final short coloredPiece) {
		switch (coloredPiece) {
			case WHITE_PAWN_BYTE:   return pawnBonus(fieldIdx, Color.WHITE);
			case WHITE_KNIGHT_BYTE: return knightBonus(fieldIdx, Color.WHITE);
			case WHITE_BISHOP_BYTE: return bishopBonus(fieldIdx, Color.WHITE);
			case WHITE_ROOK_BYTE:   return rookBonus(fieldIdx, Color.WHITE);
			case WHITE_QUEEN_BYTE:  return queenBonus(fieldIdx, Color.WHITE);
			case WHITE_KING_BYTE:   return kingBonus(fieldIdx, Color.WHITE);

			case BLACK_PAWN_BYTE:   return pawnBonus(fieldIdx, Color.BLACK);
			case BLACK_KNIGHT_BYTE: return knightBonus(fieldIdx, Color.BLACK);
			case BLACK_BISHOP_BYTE: return bishopBonus(fieldIdx, Color.BLACK);
			case BLACK_ROOK_BYTE:   return rookBonus(fieldIdx, Color.BLACK);
			case BLACK_QUEEN_BYTE:  return queenBonus(fieldIdx, Color.BLACK);
			case BLACK_KING_BYTE:   return kingBonus(fieldIdx, Color.BLACK);
		}
		throw new RuntimeException("Not yet implemented");
	}

	private int queenBonus(final int fieldIdx, final Color color) {
		return 0;
	}


	private int rookBonus(final int fieldIdx, final Color color) {
		final int score = 0;
//		if (square.Piece.Moved && castled == false) {
//			score -= 10;
//		}
		return score;
	}


	private int pawnBonus(final int fieldIdx, final Color color) {
		return positionScore(fieldIdx, ACEConstants.pawnPositionBonus, color);
	}

	private int bishopBonus(final int fieldIdx, final Color color) {
		int bonus = 0;
		if(color == Color.WHITE) {
			whiteBishopCount++;
			if (whiteBishopCount >= 2) {
				// 2 Bishops receive a bonus
				bonus += 10;
			}
		} else {
			blackBishopCount++;
			if (blackBishopCount >= 2) {
				// 2 Bishops receive a bonus
				bonus += 10;
			}
		}
		bonus += positionScore(fieldIdx, ACEConstants.bishopTable, color);
		return bonus;
	}

	private int knightBonus(final int fieldIdx, final Color color) {
		return positionScore(fieldIdx, ACEConstants.knightTable, color);
	}

	private int kingBonus(final int fieldIdx, final Color color) {
		return positionScore(fieldIdx, ACEConstants.kingTable, color);
	}


	private int positionScore(final int fieldIdx, final int[] positionBonusMap, final Color color) {
		return color == Color.WHITE
				? positionBonusMap[mirrorredFieldIndex(fieldIdx)]
				: positionBonusMap[fieldIdx];
	}

	private int mirrorredFieldIndex(final int fieldIdx) {
		return 64 - 8 - 8*(fieldIdx/8) + (fieldIdx%8);
	}
}
