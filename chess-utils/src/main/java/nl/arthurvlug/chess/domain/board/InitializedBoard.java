package nl.arthurvlug.chess.domain.board;

import nl.arthurvlug.chess.domain.board.pieces.Color;
import nl.arthurvlug.chess.domain.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.domain.board.pieces.PieceType;

import com.atlassian.fugue.Option;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

public class InitializedBoard extends Board {
	public InitializedBoard(ImmutableMap<Coordinates, ColoredPiece> occupiedFields) {
		super(initializeFields(occupiedFields));
	}

	private static ImmutableList<Field> initializeFields(ImmutableMap<Coordinates, ColoredPiece> occupiedFields) {
		Builder<Field> boardFieldsBuilder = new ImmutableList.Builder<Field>();
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				Coordinates coordinates = new Coordinates(x, y);
				Option<ColoredPiece> coloredPiece = Option.<ColoredPiece> option(occupiedFields.get(coordinates));
				boardFieldsBuilder.add(new Field(coloredPiece));
			}
		}
		return boardFieldsBuilder.build();
	}

	/**
	 * Creates a {@link ColoredPiece}
	 * @param pieceType
	 * @param color
	 * @return
	 */
	public static ColoredPiece p(PieceType pieceType, Color color) {
		return new ColoredPiece(pieceType, color);
	}

	/**
	 * Creates a {@link Coordinates}
	 * @param x
	 * @param y
	 * @return
	 */
	public static Coordinates c(int x, int y) {
		return new Coordinates(x, y);
	}
}
