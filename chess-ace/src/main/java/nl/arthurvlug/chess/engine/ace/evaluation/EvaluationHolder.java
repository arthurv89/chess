package nl.arthurvlug.chess.engine.ace.evaluation;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;

public class EvaluationHolder {
	private int whiteBishopCount;
	private int blackBishopCount;

	public int calculate(final ACEBoard engineBoard) {
		int score = 0;
		
		long occupiedBoard = engineBoard.occupied_board;
		while(occupiedBoard != 0L) {
			final int fieldIdx = Long.numberOfTrailingZeros(occupiedBoard);
			
			final ColoredPiece coloredPiece = engineBoard.pieceAt(fieldIdx);
			score += score(fieldIdx, coloredPiece);
			
			occupiedBoard ^= 1L << fieldIdx;
		}
		return score;
	}


	private int score(final int fieldIdx, final ColoredPiece coloredPiece) {
		int totalScore = 0;
		
		int pieceValue = ACEConstants.pieceValue(coloredPiece.getPieceType());
		totalScore += pieceValue;
		
		int extraScore = extraScore(fieldIdx, coloredPiece);
		totalScore += extraScore;
		
		return coloredPiece.getColor().isWhite()
				? totalScore
				: -totalScore;
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

	private int queenBonus(int fieldIdx, Color color) {
		return 0;
	}


	private int rookBonus(int fieldIdx, Color color) {
		int score = 0;
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

	
	private int positionScore(final int fieldIdx, final int[] pawnPositionBonus, final Color color) {
		return color == Color.WHITE
				? pawnPositionBonus[mirrorredFieldIndex(fieldIdx)]
				: pawnPositionBonus[fieldIdx];
	}

	private int mirrorredFieldIndex(final int fieldIdx) {
		return 64 - 8 - 8*(fieldIdx/8) + (fieldIdx%8);
	}
}
