package nl.arthurvlug.chess.engine.ace.movegeneration;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.arthurvlug.chess.engine.customEngine.EngineMove;
import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

@Getter
@AllArgsConstructor
public class UnapplyableMove extends EngineMove {
	// TODO: Change from, to and promotionPiece into byte, byte and @Nullable PieceType
	private Coordinates from;
	private Coordinates to;
	private Optional<PieceType> promotionPiece;

	@Nullable
	private ColoredPiece takePiece;

	@Override
	public String toString() {
		return FieldUtils.fieldToString(from) + FieldUtils.fieldToString(to) + MoveUtils.promotionToString(promotionPiece);
	}

	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
}
