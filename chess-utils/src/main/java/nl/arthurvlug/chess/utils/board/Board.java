package nl.arthurvlug.chess.utils.board;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import lombok.Getter;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.board.pieces.PieceType;
import nl.arthurvlug.chess.utils.game.Move;

public class Board {
	private static final Function<ColoredPiece, Character> PIECE_TO_CHAR = input -> input.getCharacter();
	
	@Getter
	private ImmutableList<Field> fields;

	public Board(ImmutableList<Field> fields) {
		this.fields = fields;
	}

	public void move(Move move) {
		this.fields = newFieldsAfterMove(move);
	}

	private ImmutableList<Field> newFieldsAfterMove(Move move) {
		ArrayList<Field> newFields = new ArrayList<>(fields);
		move(newFields, move);
		return ImmutableList.<Field> copyOf(newFields);
	}

	public static Board cloneWithoutPiece(final Board board, final Coordinates coordinates) {
		final Board clonedBoard = new Board(board.fields);
		clonedBoard.removePiece(coordinates);
		return clonedBoard;
	}

	private void removePiece(final Coordinates coordinates) {
		final List<Field> newFields = new ArrayList<>(fields);
		newFields.set(fieldIndex(coordinates), new Field(Optional.empty()));
		this.fields = ImmutableList.copyOf(newFields);
	}

	private void move(ArrayList<Field> fields, Move move) {
		ColoredPiece coloredPiece = Preconditions.checkNotNull(getPiece(move.getFrom()).orElse(null), "The piece that the player wants to move is empty. Move: " + move.toString());
		
		if(containsPiece(move.getFrom(), PieceType.KING)) {
			if(Math.abs(move.getTo().getX() - move.getFrom().getX()) == 2) {
				if(move.getTo().getX() == 6) {
					// Castle kingside
					moveRookAfterCastling(fields, move.getTo().getY(), 7, 5);
				} else if(move.getTo().getX() == 2) {
					// Castle queenside
					moveRookAfterCastling(fields, move.getTo().getY(), 0, 3);
				}
			}
		}
		
		if(move.getPromotionPiece().isPresent()) {
			// Promotion
			PieceType promotionPieceType = move.getPromotionPiece().get();
			fields.set(fieldIndex(move.getTo()), new Field(Optional.of(new ColoredPiece(promotionPieceType, coloredPiece.getColor()))));
		} else {
			Optional<ColoredPiece> piece = fields.get(fieldIndex(move.getFrom())).getPiece();
			fields.set(fieldIndex(move.getTo()), new Field(piece));
		}

		fields.set(fieldIndex(move.getFrom()), new Field(Optional.empty()));
	}

	private void moveRookAfterCastling(ArrayList<Field> fields, int y, int fromX, int toX) {
		Coordinates rookFrom = new Coordinates(fromX, y);
		Coordinates rookTo = new Coordinates(toX, y);
		move(fields, new Move(rookFrom, rookTo, Optional.empty()));
	}

	private int fieldIndex(Coordinates coordinates) {
		return fieldIndex(coordinates.getX(), coordinates.getY());
	}

	public Optional<ColoredPiece> getPiece(int x, int y) {
		return getField(x, y).getPiece();
	}

	public Field getField(int x, int y) {
		return getField(fieldIndex(x, y));
	}

	private int fieldIndex(int x, int y) {
		return y*8 + x;
	}

	private boolean containsPiece(Coordinates coordinates, final PieceType pieceType) {
		return getPiece(coordinates)
				.map(cp -> cp.getPieceType() == pieceType)
				.orElse(false);
	}

	private Optional<ColoredPiece> getPiece(Coordinates coordinates) {
		return getField(coordinates).getPiece();
	}

	private Field getField(Coordinates coordinates) {
		return getField(coordinates.getX(), coordinates.getY());
	}

	public Field getField(int fieldNo) {
		return fields.get(fieldNo);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fields.size(); i++) {
			sb.append(fields.get(i).getPiece().map(PIECE_TO_CHAR).orElse(' '));
			
			if((i+1)%8 == 0 && i < 62) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
