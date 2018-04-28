package nl.arthurvlug.chess.utils.board.pieces;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static nl.arthurvlug.chess.utils.board.pieces.PieceTypeBytes.*;

@Getter
@AllArgsConstructor
public enum PieceType {
	PAWN(PAWN_BYTE),
	KNIGHT(KNIGHT_BYTE),
	BISHOP(BISHOP_BYTE),
	ROOK(ROOK_BYTE),
	QUEEN(QUEEN_BYTE),
	KING(KING_BYTE);

	private byte pieceByte;
}
