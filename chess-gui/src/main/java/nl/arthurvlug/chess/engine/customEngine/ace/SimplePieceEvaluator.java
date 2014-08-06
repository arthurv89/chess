package nl.arthurvlug.chess.engine.customEngine.ace;

import java.util.Map;

import nl.arthurvlug.chess.domain.board.Board;
import nl.arthurvlug.chess.domain.board.Field;
import nl.arthurvlug.chess.domain.board.pieces.Color;
import nl.arthurvlug.chess.domain.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.domain.board.pieces.PieceType;
import nl.arthurvlug.chess.engine.customEngine.BoardEvaluator;
import nl.arthurvlug.chess.engine.customEngine.NormalScore;
import nl.arthurvlug.chess.engine.customEngine.Evaluation;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class SimplePieceEvaluator implements BoardEvaluator {
	private final static Map<PieceType, Integer> pieceValues = ACEConstants.pieceValues();
	
	private Function<Board, Evaluation> scoreFunction = new Function<Board, Evaluation>() {
		public Evaluation apply(Board board) {
			boolean foundWhiteKing = false;
			boolean foundBlackKing = false;
			
			int score = 0;
			for(Field b : board.getFields()) {
				if(b.getPiece().isDefined()) {
					ColoredPiece coloredPiece = b.getPiece().get();
					if(coloredPiece.getColor() == Color.WHITE) {
						if(coloredPiece.getPieceType() == PieceType.KING) {
							foundWhiteKing = true;
						}
						score += pieceValue(coloredPiece.getPieceType());
					} else {
						score -= pieceValue(coloredPiece.getPieceType());
						
						if(coloredPiece.getPieceType() == PieceType.KING) {
							foundBlackKing = true;
						}
					}
				}
			}
			
			Preconditions.checkState(foundWhiteKing && foundBlackKing, "Not all kings present: " + foundWhiteKing + " " + foundBlackKing);
			
			return new NormalScore(score);
		}
	};

	@Override
	public Evaluation evaluate(Board board) {
		return scoreFunction.apply(board);
	}

	private int pieceValue(PieceType pieceType) {
		return pieceValues.get(pieceType);
	}
}
