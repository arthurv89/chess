package nl.arthurvlug.chess.engine.ace.movegeneration;

import nl.arthurvlug.chess.engine.customEngine.EngineMove;

public class UnapplyableMove extends EngineMove {
	public static final byte NO_PROMOTION = 1;
	private static final int targetMask = ((byte) -1) << 8;
	private static final int takePieceMask = ((byte) -1) << 16;
	private static final int movingPieceMask = ((byte) -1) << 24;

//	private byte from;
//	private byte target;
//	private byte takePiece;
//	private byte promotionPiece;

	private UnapplyableMove() {
		throw new RuntimeException("Can't create this");
	}

	public static byte fromIdx(final int move) {
		return (byte) move;
	}

	public static byte targetIdx(final int move) {
		return (byte) ((move & targetMask) >> 8);
	}

	public static byte takePiece(final int move) {
		return (byte) ((move & takePieceMask) >> 16);
	}

	public static byte movingPiece(final int move) {
		return (byte) ((move & movingPieceMask) >> 24);
	}

	public static int create(final byte fromIdx, final byte targetIdx, final byte movingPiece, final byte takePiece, final byte promotionPiece) {
		return fromIdx | targetIdx << 8 | takePiece << 16 | movingPiece << 24;
	}

	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}

}
