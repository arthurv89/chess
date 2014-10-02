package nl.arthurvlug.chess.engine.ace.evaluation;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.customEngine.AbstractEngineBoard;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.Evaluation;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;

import com.google.common.base.Function;

public class AceEvaluator implements BoardEvaluator {
	private Function<ACEBoard, Evaluation> scoreFunction = new Function<ACEBoard, Evaluation>() {
		public Evaluation apply(ACEBoard engineBoard) {
			int score = 0;
			
			long occupiedBoard = engineBoard.occupied_board;
			while(occupiedBoard != 0L) {
				final int fieldIdx = Long.numberOfTrailingZeros(occupiedBoard);
				
				ColoredPiece coloredPiece = engineBoard.pieceAt(fieldIdx);
				score += score(fieldIdx, coloredPiece);
				
				occupiedBoard ^= 1L << fieldIdx;
			}
			
			return new NormalScore(score);
		}

		private int score(int fieldIdx, ColoredPiece coloredPiece) {
			int totalScore = 0;
			totalScore += ACEConstants.pieceValue(coloredPiece.getPieceType());
			totalScore += extraScore(fieldIdx, coloredPiece);
			
			return coloredPiece.getColor().isWhite() ? totalScore : -totalScore;
		}

		private int extraScore(int fieldIdx, ColoredPiece coloredPiece) {
			switch (coloredPiece.getPieceType()) {
				case PAWN: return pawnBonus(fieldIdx, coloredPiece.getColor());
			}
			return 0;
//			throw new RuntimeException("Not yet implemented");
		}

		private int pawnBonus(int fieldIdx, Color color) {
			return positionScore(fieldIdx, ACEConstants.pawnPositionBonus, color);
		}
		
		private int positionScore(int fieldIdx, int[] positionBonusses, Color color) {
			return color == Color.WHITE
						? ACEConstants.pawnPositionBonus[fieldIdx]
						: ACEConstants.pawnPositionBonus[mirrorredFieldIndex(fieldIdx)];
		}

		private int mirrorredFieldIndex(int fieldIdx) {
			return 64 - 8 - 8*(fieldIdx/8) + (fieldIdx%8);
		}
	};

	@Override
	public NormalScore evaluate(AbstractEngineBoard board) {
		return (NormalScore) scoreFunction.apply((ACEBoard) board);
	}
}
