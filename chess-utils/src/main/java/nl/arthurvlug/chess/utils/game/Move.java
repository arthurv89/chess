package nl.arthurvlug.chess.utils.game;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

import com.atlassian.fugue.Option;

@AllArgsConstructor
@Getter
public class Move {
	// TODO: Change from, to and promotionPiece into byte, byte and @Nullable PieceType
	private Coordinates from;
	private Coordinates to;
	private Optional<PieceType> promotionPiece;
	
	@Override
	public String toString() {
		return FieldUtils.fieldToString(from) + FieldUtils.fieldToString(to) + MoveUtils.promotionToString(promotionPiece);
	}
	
	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
}
