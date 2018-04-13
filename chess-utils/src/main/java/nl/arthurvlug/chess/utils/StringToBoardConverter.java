package nl.arthurvlug.chess.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.sun.tools.javac.util.ArrayUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import nl.arthurvlug.chess.utils.board.Board;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import nl.arthurvlug.chess.utils.board.InitializedBoard;
import nl.arthurvlug.chess.utils.board.pieces.Color;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.board.pieces.PieceSymbol;
import nl.arthurvlug.chess.utils.board.pieces.PieceToChessSymbolConverter;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;

public class StringToBoardConverter {
	private static PieceToChessSymbolConverter pieceToChessSymbolConverter = new PieceToChessSymbolConverter();

	public static Board convert(final String board) {
		final ImmutableMap.Builder<Coordinates, ColoredPiece> map = ImmutableMap.builder();
		conv(board, (coordinates, piece) -> {
			map.put(coordinates, piece);
			return null;
		});
		return new InitializedBoard(map.build());
	}

	public static void conv(final String board, BiFunction<Coordinates, ColoredPiece, Void> func) {
		final List<String> rows = Arrays.asList(board.trim().split("\n"));
		Collections.reverse(rows);


		final char[] fieldChars = Joiner.on("").join(rows).toCharArray();
		int fieldIndex = 0;
		for (int i = 0; i < fieldChars.length; i++) {
			final char c = fieldChars[i];
			if(c == '\n') {
				continue;
			}
			final Optional<ColoredPiece> coloredPiece = fromPieceSymbol(c);
			final int finalFieldIndex = fieldIndex;
			coloredPiece.ifPresent(piece -> {
				final Coordinates coordinates = FieldUtils.coordinates(finalFieldIndex);
				func.apply(coordinates, piece);
			});
			fieldIndex++;
		}
	}

	private static Optional<ColoredPiece> fromPieceSymbol(final char c) {
		for(Map.Entry<PieceType, PieceSymbol> e : pieceToChessSymbolConverter.getMap().entrySet()) {
			if(e.getValue().getWhite() == c) {
				return Optional.of(new ColoredPiece(e.getKey(), Color.WHITE));
			}
			if(e.getValue().getBlack() == c) {
				return Optional.of(new ColoredPiece(e.getKey(), Color.BLACK));
			}
		}
		return Optional.empty();
	}
}
