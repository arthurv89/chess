package nl.arthurvlug.chess.engine.ace.movegeneration;

import nl.arthurvlug.chess.utils.MoveUtils;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.FieldUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XRayTest {
	@Before
	public void before() {
		MoveUtils.DEBUG = false;
	}

	@Test
	public void testKingXRay() {
		long[] xray = Xray.king_xray;
		assertEquals(64, xray.length);

		long a1 = bitboard("b1 a2 b2");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		assertEquals(0x302L, xRayBitboard(xray, "a1"));

		long b1 = bitboard("a1 c1 a2 b2 c2");
		assertEquals(b1, xRayBitboard(xray, "b1"));

		long b2 = bitboard("a1 b1 c1 a2 c2 a3 b3 c3");
		assertEquals(b2, xRayBitboard(xray, "b2"));

		long h8 = bitboard("g7 h7 g8");
		assertEquals(h8, xRayBitboard(xray, "h8"));
	}

	@Test
	public void testCastlingXRay() {
		long[][] xray = Xray.castling_xray;
		assertEquals(2, xray.length);

		assertEquals(2, xray[0].length);
		assertEquals(bitboard("d1 c1"), xray[0][0]);
		assertEquals(bitboard("f1 g1"), xray[0][1]);

		assertEquals(2, xray[1].length);
		assertEquals(bitboard("d8 c8"), xray[1][0]);
		assertEquals(bitboard("f8 g8"), xray[1][1]);
	}

	@Test
	public void testKnightXRay() {
		long[] xray = Xray.knight_xray;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("b3 c2");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long b1 = bitboard("a3 c3 d2");
		assertEquals(b1, xRayBitboard(xray, "b1"));
		
		long c3 = bitboard("b1 a2 a4 b5 d5 e4 e2 d1");
		assertEquals(c3, xRayBitboard(xray, "c3"));
		
		long h8 = bitboard("g6 f7");
		assertEquals(h8, xRayBitboard(xray, "h8"));
	}

	@Test
	public void testBishopXRay() {
		long[] xray = Xray.bishop_xray;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("b2 c3 d4 e5 f6 g7 h8");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long c2 = bitboard("b1 d1 b3 a4 d3 e4 f5 g6 h7");
		assertEquals(c2, xRayBitboard(xray, "c2"));
		
		long h8 = bitboard("a1 b2 c3 d4 e5 f6 g7");
		assertEquals(h8, xRayBitboard(xray, "h8"));
	}

	@Test
	public void testRookXRay() {
		long[] xray = Xray.rook_xray;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("a2 a3 a4 a5 a6 a7 a8 b1 c1 d1 e1 f1 g1 h1");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long c2 = bitboard("c1 c3 c4 c5 c6 c7 c8 a2 b2 d2 e2 f2 g2 h2");
		assertEquals(c2, xRayBitboard(xray, "c2"));
		
		long h8 = bitboard("a8 b8 c8 d8 e8 f8 g8 h7 h6 h5 h4 h3 h2 h1");
		assertEquals(h8, xRayBitboard(xray, "h8"));
	}

	@Test
	public void testQueenXRay() {
		long[] xray = Xray.queen_xray;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("b2 c3 d4 e5 f6 g7 h8 a2 a3 a4 a5 a6 a7 a8 b1 c1 d1 e1 f1 g1 h1");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long c2 = bitboard("b1 d1 b3 a4 d3 e4 f5 g6 h7 c1 c3 c4 c5 c6 c7 c8 a2 b2 d2 e2 f2 g2 h2");
		assertEquals(c2, xRayBitboard(xray, "c2"));
		
		long h8 = bitboard("a1 b2 c3 d4 e5 f6 g7 a8 b8 c8 d8 e8 f8 g8 h7 h6 h5 h4 h3 h2 h1");
		assertEquals(h8, xRayBitboard(xray, "h8"));
	}

	@Test
	public void testWhitePawnXRay() {
		long[] xray = Xray.pawn_xray_white;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long c2 = bitboard("c3 c4");
		assertEquals(c2, xRayBitboard(xray, "c2"));
		
		long c3 = bitboard("c4");
		assertEquals(c3, xRayBitboard(xray, "c3"));
		
		long h7 = bitboard("h8");
		assertEquals(h7, xRayBitboard(xray, "h7"));
		
		long h8 = bitboard("");
		assertEquals(h8, xRayBitboard(xray, "h8"));
	}
	
	@Test
	public void testWhitePawnCaptureXRay() {
		long[] xray = Xray.pawn_xray_white_capture;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long a2 = bitboard("b3");
		assertEquals(a2, xRayBitboard(xray, "a2"));
		
		long c3 = bitboard("b4 d4");
		assertEquals(c3, xRayBitboard(xray, "c3"));
		
		long h7 = bitboard("g8");
		assertEquals(h7, xRayBitboard(xray, "h7"));
		
		long h8 = bitboard("");
		assertEquals(h8, xRayBitboard(xray, "h8"));
	}

	@Test
	public void testBlackPawnXRay() {
		long[] xray = Xray.pawn_xray_black;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long c7 = bitboard("c6 c5");
		assertEquals(c7, xRayBitboard(xray, "c7"));
		
		long c6 = bitboard("c5");
		assertEquals(c6, xRayBitboard(xray, "c6"));
		
		long h2 = bitboard("h1");
		assertEquals(h2, xRayBitboard(xray, "h2"));
		
		long h8 = bitboard("");
		assertEquals(h8, xRayBitboard(xray, "h8"));
	}
	
	@Test
	public void testBlackPawnCaptureXRay() {
		long[] xray = Xray.pawn_xray_black_capture;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long a7 = bitboard("b6");
		assertEquals(a7, xRayBitboard(xray, "a7"));
		
		long c6 = bitboard("b5 d5");
		assertEquals(c6, xRayBitboard(xray, "c6"));
		
		long h2 = bitboard("g1");
		assertEquals(h2, xRayBitboard(xray, "h2"));
		
		long h1 = bitboard("");
		assertEquals(h1, xRayBitboard(xray, "h8"));
	}

	
	@Test
	public void testLeftBoardXRay() {
		long[] xray = Xray.left_board;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long a7 = bitboard("");
		assertEquals(a7, xRayBitboard(xray, "a7"));
		
		long c6 = bitboard("b6 a6");
		assertEquals(c6, xRayBitboard(xray, "c6"));
		
		long h2 = bitboard("g2 f2 e2 d2 c2 b2 a2");
		assertEquals(h2, xRayBitboard(xray, "h2"));
		
		long h1 = bitboard("a8 b8 c8 d8 e8 f8 g8");
		assertEquals(h1, xRayBitboard(xray, "h8"));
	}


	
	@Test
	public void testRightBoardXRay() {
		long[] xray = Xray.right_board;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("b1 c1 d1 e1 f1 g1 h1");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long a7 = bitboard("b7 c7 d7 e7 f7 g7 h7");
		assertEquals(a7, xRayBitboard(xray, "a7"));
		
		long c6 = bitboard("d6 e6 f6 g6 h6");
		assertEquals(c6, xRayBitboard(xray, "c6"));
		
		long h2 = bitboard("");
		assertEquals(h2, xRayBitboard(xray, "h2"));
		
		long h1 = bitboard("");
		assertEquals(h1, xRayBitboard(xray, "h8"));
	}


	
	@Test
	public void testUpBoardXRay() {
		long[] xray = Xray.up_board;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("a2 a3 a4 a5 a6 a7 a8");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long a7 = bitboard("a8");
		assertEquals(a7, xRayBitboard(xray, "a7"));
		
		long c6 = bitboard("c7 c8");
		assertEquals(c6, xRayBitboard(xray, "c6"));
		
		long h2 = bitboard("h3 h4 h5 h6 h7 h8");
		assertEquals(h2, xRayBitboard(xray, "h2"));
		
		long h8 = bitboard("");
		assertEquals(h8, xRayBitboard(xray, "h8"));
	}


	
	@Test
	public void testDownBoardXRay() {
		long[] xray = Xray.down_board;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("");
		assertEquals(a1, xRayBitboard(xray, "a1"));
		
		long a7 = bitboard("a6 a5 a4 a3 a2 a1");
		assertEquals(a7, xRayBitboard(xray, "a7"));
		
		long c6 = bitboard("c5 c4 c3 c2 c1");
		assertEquals(c6, xRayBitboard(xray, "c6"));
		
		long h2 = bitboard("h1");
		assertEquals(h2, xRayBitboard(xray, "h2"));
		
		long h1 = bitboard("");
		assertEquals(h1, xRayBitboard(xray, "h1"));
	}
	
	// TODO: Write for pawn createMoves
	@Test
	public void testWhiteOneFieldMove() {
		long[] xray = Xray.pawn_xray_white_one_field_move;
		assertEquals(64, xray.length);

		long c6 = bitboard("c7");
		assertEquals(c6, xRayBitboard(xray, "c6"));
	}

	@Test
	public void testBlackOneFieldMove() {
		long[] xray = Xray.pawn_xray_black_one_field_move;
		assertEquals(64, xray.length);

		long a7 = bitboard("a6");
		assertEquals(a7, xRayBitboard(xray, "a7"));
	}

	@Test
	public void testWhiteTwoFieldMove() {
		long[] xray = Xray.pawn_xray_black_two_field_move;
		assertEquals(64, xray.length);

		long a7 = bitboard("a5");
		assertEquals(a7, xRayBitboard(xray, "a7"));
		
		long a6 = bitboard("");
		assertEquals(a6, xRayBitboard(xray, "a6"));
	}
	
	@Test
	public void testBlackTwoFieldMove() {
		long[] xray = Xray.pawn_xray_white_two_field_move;
		assertEquals(64, xray.length);

		long a2 = bitboard("a4");
		assertEquals(a2, xRayBitboard(xray, "a2"));
		
		long a3 = bitboard("");
		assertEquals(a3, xRayBitboard(xray, "a3"));
	}
	
	@Test
	public void testWhiteTakeMove() {
		long[] xray = Xray.pawn_xray_white_take_field_move;
		assertEquals(64, xray.length);

		long a3 = bitboard("b4");
		assertEquals(a3, xRayBitboard(xray, "a3"));
		
		long e3 = bitboard("d4 f4");
		assertEquals(e3, xRayBitboard(xray, "e3"));
	}
	
	@Test
	public void testBlackTakeMove() {
		long[] xray = Xray.pawn_xray_black_take_field_move;
		assertEquals(64, xray.length);

		long b4 = bitboard("a3 c3");
		assertEquals(b4, xRayBitboard(xray, "b4"));
		
		long a3 = bitboard("b2");
		assertEquals(a3, xRayBitboard(xray, "a3"));
		
		long h3 = bitboard("g2");
		assertEquals(h3, xRayBitboard(xray, "h3"));
	}
	
	
	
	
	
	
	
	
	

	private long xRayBitboard(long[] xray, String fieldName) {
		int sq = FieldUtils.fieldIdx(FieldUtils.coordinates(fieldName));
		return xray[sq];
	}

	private long bitboard(String fields) {
		if(fields.isEmpty()) {
			return 0L;
		}
		String[] splittedFields = fields.split(" ");
		long bitboard = 0L;
		for(String splittedField : splittedFields) {
			Coordinates coordinates = FieldUtils.coordinates(splittedField);
			int fieldIndex = FieldUtils.fieldIdx(coordinates);
			bitboard |= 1L << fieldIndex;
		}
		return bitboard;
	}
}
