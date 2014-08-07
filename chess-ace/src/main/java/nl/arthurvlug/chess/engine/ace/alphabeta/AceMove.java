package nl.arthurvlug.chess.engine.ace.alphabeta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;

import com.atlassian.fugue.Option;

@Getter
@AllArgsConstructor
public class AceMove {
	private PieceType movingPiece;
	private int toMove;
	private Coordinates fromCoordinate;
	private Coordinates toCoordinate;
	private Option<PieceType> promotionPiece;
	
	public Move toMove() {
		return new Move(fromCoordinate, toCoordinate, promotionPiece);
	}
	
	@Override
	public String toString() {
		return toMove().toString();
	}
}
