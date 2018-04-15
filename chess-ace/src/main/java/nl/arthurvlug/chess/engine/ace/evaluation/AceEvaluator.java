package nl.arthurvlug.chess.engine.ace.evaluation;

import com.google.common.collect.LinkedHashMultimap;
import java.util.List;
import java.util.Set;
import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.movegeneration.AceMoveGenerator;
import nl.arthurvlug.chess.engine.ace.movegeneration.UnapplyableMove;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.game.Move;

public class AceEvaluator implements BoardEvaluator<ACEBoard, Integer> {
	private int whiteBishopCount;
	private int blackBishopCount;

	public Integer evaluate(final ACEBoard aceBoard) {
		final List<UnapplyableMove> moves = AceMoveGenerator.generateMoves(aceBoard);
		LinkedHashMultimap<Integer, UnapplyableMove> byFromPositionMap = byFromPosition(moves);
		int score = 0;

		long occupiedBoard = aceBoard.occupied_board;
		while(occupiedBoard != 0L) {
			final int fieldIdx = Long.numberOfTrailingZeros(occupiedBoard);

			final ColoredPiece coloredPiece = aceBoard.pieceAt(fieldIdx);
			score += pieceScore(fieldIdx, coloredPiece, byFromPositionMap.get(fieldIdx));
//			System.out.println(FieldUtils.coordinates(fieldIdx) + " -> " + pieceScore(fieldIdx, coloredPiece, byFromPositionMap.get(fieldIdx)));
			occupiedBoard ^= 1L << fieldIdx;
		}
		return score;
	}


	private LinkedHashMultimap<Integer, UnapplyableMove> byFromPosition(final List<UnapplyableMove> moves) {
		final LinkedHashMultimap<Integer, UnapplyableMove> multiMap = LinkedHashMultimap.create();
		for (final UnapplyableMove move : moves) {
			final Coordinates from = move.getFrom();
			final Integer fromIdx = FieldUtils.fieldIdx(from);
			multiMap.put(fromIdx, move);
		}
		return multiMap;
	}


	private int pieceScore(final int fieldIdx, final ColoredPiece coloredPiece, final Set<UnapplyableMove> moves) {
		int totalScore = 0;

		final int pieceValue = ACEConstants.pieceValues[coloredPiece.getPieceType().ordinal()];
		totalScore += pieceValue;

		final int extraScore = extraScore(fieldIdx, coloredPiece);
		totalScore += extraScore;

//		int mobilityScore = mobilityScore(moves);
//		totalScore += mobilityScore;

		return coloredPiece.getColor().isWhite()
				? totalScore
				: -totalScore;
	}


	private int mobilityScore(Set<Move> moves) {
		return moves.size();
	}


	private int extraScore(final int fieldIdx, final ColoredPiece coloredPiece) {
		switch (coloredPiece.getPieceType()) {
			case PAWN: return pawnBonus(fieldIdx, coloredPiece.getColor());
			case KNIGHT: return knightBonus(fieldIdx, coloredPiece.getColor());
			case BISHOP: return bishopBonus(fieldIdx, coloredPiece.getColor());
			case ROOK: return rookBonus(fieldIdx, coloredPiece.getColor());
			case QUEEN: return queenBonus(fieldIdx, coloredPiece.getColor());
			case KING: return kingBonus(fieldIdx, coloredPiece.getColor());
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
