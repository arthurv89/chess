package nl.arthurvlug.chess.domain.board;

import lombok.extern.slf4j.Slf4j;
import nl.arthurvlug.chess.domain.game.Move;
import nl.arthurvlug.chess.domain.pieces.ColoredPiece;
import nl.arthurvlug.chess.domain.pieces.King;
import nl.arthurvlug.chess.domain.pieces.Piece;

import com.atlassian.fugue.Option;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@Slf4j
public abstract class Board {
	private static final Function<ColoredPiece, String> PIECE_TO_STRING = new Function<ColoredPiece, String>() {
		@Override
		public String apply(ColoredPiece input) {
			return input.getCharacterString();
		}
	};
	private final ImmutableList<ImmutableList<Field>> fields;
	
	protected Board() {
		Builder<ImmutableList<Field>> boardBuilder = new ImmutableList.Builder<ImmutableList<Field>>();
		for (int y = 0; y < 8; y++) {
			Builder<Field> row = new ImmutableList.Builder<Field>();
			for (int x = 0; x < 8; x++) {
				row.add(new Field(x, y, Option.<ColoredPiece>none()));
			}
			boardBuilder.add(row.build());
		}
		fields = boardBuilder.build();
	}

	protected void setField(int x, int y, Option<ColoredPiece> option) {
		getField(x, y).setPiece(option);
	}

	public Option<ColoredPiece> getPiece(int x, int y) {
		return getField(x, y).getPiece();
	}

	public Field getField(int x, int y) {
		return fields.get(y).get(x);
	}

	public void move(Move move) {
		ColoredPiece coloredPiece = Preconditions.checkNotNull(getPiece(move.getFrom()).getOrNull());

		if(containsPiece(move.getFrom(), King.class)) {
			if(Math.abs(move.getTo().getX() - move.getFrom().getX()) == 2) {
				if(move.getTo().getX() == 6) {
					// Castle kingside
					moveRook(move.getTo().getY(), 7, 5);
				} else if(move.getTo().getX() == 2) {
					// Castle queenside
					moveRook(move.getTo().getY(), 0, 3);
				}
			}
		}
		
		if(move.getPromotionPiece().isDefined()) {
			// Promotion
			Piece promotionPiece = move.getPromotionPiece().get();
			getField(move.getTo()).setPiece(Option.some(new ColoredPiece(promotionPiece, coloredPiece.getColor())));
		} else {
			getField(move.getTo()).setPiece(Option.some(coloredPiece));
		}

		getField(move.getFrom()).setPiece(Option.<ColoredPiece>none());
	}

	private void moveRook(int y, int fromX, int toX) {
		Coordinates rookFrom = getField(fromX, y).getCoordinates();
		Coordinates rookTo = getField(toX, y).getCoordinates();
		move(new Move(rookFrom, rookTo, Option.<Piece> none()));
		log.debug("Castled");
	}

	private boolean containsPiece(Coordinates coordinates, Class<King> pieceType) {
		Predicate<ColoredPiece> containsPiece = new Predicate<ColoredPiece>() {
			@Override
			public boolean apply(ColoredPiece input) {
				return input.getPiece() instanceof King;
			}
		};
		return getPiece(coordinates).exists(containsPiece);
	}

	private Option<ColoredPiece> getPiece(Coordinates coordinates) {
		return getField(coordinates).getPiece();
	}

	private Field getField(Coordinates coordinates) {
		return getField(coordinates.getX(), coordinates.getY());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(" -------- ");
		sb.append("\n");
		for (ImmutableList<Field> row : fields.reverse()) {
			sb.append('|');
			for (Field field : row) {
				sb.append(field.getPiece().map(PIECE_TO_STRING).getOrElse(" "));
			}
			sb.append('|');
			sb.append("\n");
		}
		sb.append(" -------- ");
		return sb.toString();
	}
}
