package nl.arthurvlug.chess.engine.ace.movegeneration;

import nl.arthurvlug.chess.engine.customEngine.EngineMove;

public class UnapplyableMove extends EngineMove {
	private static final int FROM_IDX_BITS = 6;
	private static final int TARGET_IDX_BITS = 6;
	private static final int TAKE_PIECE_IDX_BITS = 4;
	private static final int MOVING_PIECE_IDX_BITS = 4;
	private static final int PROMOTION_PIECE_IDX_BITS = 4;

	private static final byte FROM_MASK = createMask(FROM_IDX_BITS);
	private static final byte TARGET_MASK = createMask(TARGET_IDX_BITS);
	private static final byte TAKE_PIECE_MASK = createMask(TAKE_PIECE_IDX_BITS);
	private static final byte MOVING_PIECE_MASK = createMask(MOVING_PIECE_IDX_BITS);
	private static final byte PROMOTION_PIECE_MASK = createMask(PROMOTION_PIECE_IDX_BITS);

	private static final int TARGET_BIT_SHIFT = FROM_IDX_BITS;
	private static final int TAKE_PIECE_BIT_SHIFT = TARGET_BIT_SHIFT + TARGET_IDX_BITS;
	private static final int MOVING_PIECE_BIT_SHIFT = TAKE_PIECE_BIT_SHIFT + TAKE_PIECE_IDX_BITS;
	private static final int PROMOTION_PIECE_BIT_SHIFT = MOVING_PIECE_BIT_SHIFT + MOVING_PIECE_IDX_BITS;

	private UnapplyableMove() {
		throw new RuntimeException("Can't create this");
	}

	public static byte fromIdx(final int move) {
		return (byte) (move & FROM_MASK);
	}

	public static byte targetIdx(final int move) {
		return (byte) ((move >> TARGET_BIT_SHIFT) & TARGET_MASK);
	}

	public static byte takePiece(final int move) {
		return (byte) ((move >> TAKE_PIECE_BIT_SHIFT) & TAKE_PIECE_MASK);
	}

	public static byte coloredMovingPiece(final int move) {
		return (byte) ((move >> MOVING_PIECE_BIT_SHIFT) & MOVING_PIECE_MASK);
	}

	public static byte promotionPiece(final int move) {
		return (byte) ((move >> PROMOTION_PIECE_BIT_SHIFT) & PROMOTION_PIECE_MASK);
	}

	public static int create(final int fromIdx, final int targetIdx, final int movingPiece, final int takePiece, final int promotionPiece) {
		return fromIdx
				| targetIdx << TARGET_BIT_SHIFT
				| takePiece << TAKE_PIECE_BIT_SHIFT
				| movingPiece << MOVING_PIECE_BIT_SHIFT
				| promotionPiece << PROMOTION_PIECE_BIT_SHIFT;
	}

	private static byte createMask(final int fromIdxBits) {
		return (byte) ((1 << fromIdxBits) - 1);
	}

	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
}
