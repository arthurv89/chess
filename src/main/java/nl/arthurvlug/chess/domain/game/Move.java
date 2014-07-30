package nl.arthurvlug.chess.domain.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.domain.pieces.Piece;
import nl.arthurvlug.chess.engine.MoveUtils;

import com.atlassian.fugue.Option;

@AllArgsConstructor
@Getter
public class Move {
	private Coordinates from;
	private Coordinates to;
	private Option<Piece> promotionPiece;
	
	@Override
	public String toString() {
		return MoveUtils.fieldToString(from) + MoveUtils.fieldToString(to) + MoveUtils.promotionToString(promotionPiece);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
}
