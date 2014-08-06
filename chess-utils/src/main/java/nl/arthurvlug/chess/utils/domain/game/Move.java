package nl.arthurvlug.chess.utils.domain.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.domain.board.Coordinates;
import nl.arthurvlug.chess.utils.domain.board.pieces.PieceType;

import com.atlassian.fugue.Option;

@AllArgsConstructor
@Getter
public class Move {
	private Coordinates from;
	private Coordinates to;
	private Option<PieceType> promotionPiece;
	
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
