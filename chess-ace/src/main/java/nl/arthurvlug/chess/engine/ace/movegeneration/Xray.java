package nl.arthurvlug.chess.engine.ace.movegeneration;

import java.util.function.Function;

import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;
import nl.arthurvlug.chess.utils.board.Coordinates;

import com.atlassian.fugue.Option;

public class Xray {
	private static Function<Coordinates, Long> kingXrayFunc = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			return 0L
				| board(locate(coordinates, -1, -1))
				| board(locate(coordinates, -1, 0))
				| board(locate(coordinates, -1, 1))
				| board(locate(coordinates, 0, -1))
				| board(locate(coordinates, 0, 1))
				| board(locate(coordinates, 1, -1))
				| board(locate(coordinates, 1, 0))
				| board(locate(coordinates, 1, 1));
		}
	};

	private static Function<Coordinates, Long> queenXRayFunc = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			int i = BitboardUtils.fieldIdx(coordinates);
			return 0L
				| rook_xray[i]
				| bishop_xray[i];
		}
	};

	private static Function<Coordinates, Long> rookXRayFunc = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			long rookMovePositions = 0L;
			for (int j = 1; j < 8; j++) {
				rookMovePositions |= board(locate(coordinates, -j, 0));
				rookMovePositions |= board(locate(coordinates, 0, -j));
				rookMovePositions |= board(locate(coordinates, 0, j));
				rookMovePositions |= board(locate(coordinates, j, 0));
			}
			return rookMovePositions;
		}
	};

	private static Function<Coordinates, Long> bishopXRayFunc = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			long bishopMovePositions = 0L;
			for (int j = 1; j < 8; j++) {
				bishopMovePositions |= board(locate(coordinates, -j, -j));
				bishopMovePositions |= board(locate(coordinates, -j, j));
				bishopMovePositions |= board(locate(coordinates, j, -j));
				bishopMovePositions |= board(locate(coordinates, j, j));
			}
			return bishopMovePositions;
		}
	};

	private static Function<Coordinates, Long> knightXRayFunc = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			return 0L
				| board(locate(coordinates, -2, -1))
				| board(locate(coordinates, -1, -2))
				| board(locate(coordinates, -1, 2))
				| board(locate(coordinates, -2, 1))
				| board(locate(coordinates, 2, -1))
				| board(locate(coordinates, 1, -2))
				| board(locate(coordinates, 2, 1))
				| board(locate(coordinates, 1, 2));
		}
	};

	private static Function<Coordinates, Long> pawnXRayWhiteFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			if(coordinates.getY() == 0) {
				return 0L;
			}
			
			long pawnMovePositions = board(locate(coordinates, 0, 1));
			if(coordinates.getY() == 1) {
				pawnMovePositions |= board(locate(coordinates, 0, 2));
			}
			return pawnMovePositions;
		}
	};

	private static Function<Coordinates, Long> pawnXRayWhiteCaptureFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			if(coordinates.getY() == 0) {
				return 0L;
			}
			return 0L
				| board(locate(coordinates, -1, 1))
				| board(locate(coordinates, 1, 1));
		}
	};

	private static Function<Coordinates, Long> pawnXRayBlackFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			if(coordinates.getY() == 7) {
				return 0L;
			}
			
			long pawnMovePositions = board(locate(coordinates, 0, -1));
			if(coordinates.getY() == 6) {
				pawnMovePositions |= board(locate(coordinates, 0, -2));
			}
			return pawnMovePositions;
		}
	};

	private static Function<Coordinates, Long> pawnXRayBlackCaptureFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			if(coordinates.getY() == 7) {
				return 0L;
			}
			return 0L
				| board(locate(coordinates, -1, -1))
				| board(locate(coordinates, 1, -1));
		}
	};

	private static Function<Coordinates, Long> leftBoardFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			long leftPositions = 0L;
			for (int i = 1; i <= 7; i++) {
				leftPositions |= board(locate(coordinates, -i, 0));
			}
			return leftPositions;
		}
	};

	private static Function<Coordinates, Long> rightBoardFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			long rightPositions = 0L;
			for (int i = 1; i <= 7; i++) {
				rightPositions |= board(locate(coordinates, i, 0));
			}
			return rightPositions;
		}
	};

	private static Function<Coordinates, Long> upBoardFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			long upPositions = 0L;
			for (int i = 1; i <= 7; i++) {
				upPositions |= board(locate(coordinates, 0, i));
			}
			return upPositions;
		}
	};

	private static Function<Coordinates, Long> downBoardFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			long upPositions = 0L;
			for (int i = 1; i <= 7; i++) {
				upPositions |= board(locate(coordinates, 0, -i));
			}
			return upPositions;
		}
	};

	private static Function<Coordinates, Long> deg45BoardFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			long deg45Positions = 0L;
			for (int i = 1; i <= 7; i++) {
				deg45Positions |= board(locate(coordinates, i, i));
			}
			return deg45Positions;
		}
	};

	private static Function<Coordinates, Long> deg135BoardFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			long deg135Positions = 0L;
			for (int i = 1; i <= 7; i++) {
				deg135Positions |= board(locate(coordinates, -i, i));
			}
			return deg135Positions;
		}
	};

	private static Function<Coordinates, Long> deg225BoardFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			long deg225Positions = 0L;
			for (int i = 1; i <= 7; i++) {
				deg225Positions |= board(locate(coordinates, i, -i));
			}
			return deg225Positions;
		}
	};

	private static Function<Coordinates, Long> deg315BoardFunction = new Function<Coordinates, Long>() {
		@Override
		public Long apply(Coordinates coordinates) {
			long deg3155Positions = 0L;
			for (int i = 1; i <= 7; i++) {
				deg3155Positions |= board(locate(coordinates, -i, -i));
			}
			return deg3155Positions;
		}
	};

	final static long[] left_board = xRay(leftBoardFunction);
	final static long[] right_board = xRay(rightBoardFunction);
	final static long[] up_board = xRay(upBoardFunction);
	final static long[] down_board = xRay(downBoardFunction);

	final static long[] deg45_board = xRay(deg45BoardFunction);
	final static long[] deg135_board = xRay(deg135BoardFunction);
	final static long[] deg225_board = xRay(deg225BoardFunction);
	final static long[] deg315_board = xRay(deg315BoardFunction);
	
	

	
	final static long[] king_xray = xRay(kingXrayFunc);
	final static long[] knight_xray = xRay(knightXRayFunc);
	final static long[] bishop_xray = xRay(bishopXRayFunc);
	final static long[] rook_xray = xRay(rookXRayFunc);
	final static long[] queen_xray = xRay(queenXRayFunc);
	final static long[] pawn_xray_white = xRay(pawnXRayWhiteFunction);
	final static long[] pawn_xray_white_capture = xRay(pawnXRayWhiteCaptureFunction);
	final static long[] pawn_xray_black = xRay(pawnXRayBlackFunction);
	final static long[] pawn_xray_black_capture = xRay(pawnXRayBlackCaptureFunction);
	
	
	
	


	private static long[] xRay(Function<Coordinates, Long> function) {
		long[] xray = new long[64];
		for (int i = 0; i < 64; i++) {
			xray[i] = function.apply(BitboardUtils.coordinates(i));
		}
		return xray;
	}

	private static long board(Option<Coordinates> coordinate) {
		if(coordinate.isEmpty()) {
			return 0;
		} else {
			return 1L << BitboardUtils.fieldIdx(coordinate.get());
		}
	}

	private static Option<Coordinates> locate(final Coordinates coordinates, final int xDiff, final int yDiff) {
		int newX = coordinates.getX() + xDiff;
		int newY = coordinates.getY() + yDiff;
		return (newX >= 0 && newX <= 7 && newY >= 0 && newY <= 7)
				? Option.some(new Coordinates(newX, newY))
				: Option.<Coordinates> none();
	}
}
