package nl.arthurvlug.chess.utils.board.pieces;

import java.util.Map;

import com.atlassian.fugue.Option;

public abstract class PieceConverter<T> {
	Option<PieceType> fromChar(char character) {
		return com.atlassian.fugue.Iterables.findFirst(getMap().keySet(), k -> {
			T symbol = getMap().get(k);
			return pred(symbol, character);
		});
	}

	abstract boolean pred(T t, char character);

	abstract char convert(PieceType pieceType, Color color);
	abstract Map<PieceType, T> getMap();
}
