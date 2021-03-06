package nl.arthurvlug.chess.utils.board.pieces;

import lombok.Getter;
import lombok.Value;

@Value
@Getter
public class ColoredPiece {
	private final PieceType pieceType;
	private final Color color;

	public ColoredPiece(final PieceType pieceType, final Color color) {
		this.pieceType = pieceType;
		this.color = color;
	}

	public String getCharacterString() {
		return PieceStringUtils.toCharacterString(this, PieceStringUtils.pieceToChessSymbolMap);
	}
}
