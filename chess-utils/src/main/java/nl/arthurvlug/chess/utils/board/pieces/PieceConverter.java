package nl.arthurvlug.chess.utils.board.pieces;

import java.util.Map;

import com.atlassian.fugue.Option;

public abstract class PieceConverter<T> {
	Option<PieceType> fromChar(final char character) {
		return com.atlassian.fugue.Iterables.findFirst(getMap().keySet(), k -> {
			final T symbol = getMap().get(k);
			return isPiece(symbol, character);
		});
	}
	
	public char convert(final PieceType pieceType, final Color color) {
		T t = getMap().get(pieceType);
		if(color == Color.WHITE) {
			return whiteChar(t);
		} else {
			return blackChar(t);
		}
	}

	abstract char whiteChar(T t);
	abstract char blackChar(T t);
	abstract boolean isPiece(final T t, final char character);
	abstract Map<PieceType, T> getMap();
}
