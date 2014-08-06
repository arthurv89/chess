package nl.arthurvlug.chess.engine.movegeneration;

import static org.junit.Assert.assertEquals;
import nl.arthurvlug.chess.domain.board.Coordinates;
import nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils;

import org.junit.Test;

public class XRayTest {
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
	
	

	private long xRayBitboard(long[] xray, String fieldName) {
		int sq = BitboardUtils.toIndex(BitboardUtils.coordinates(fieldName));
		return xray[sq];
	}

	private long bitboard(String fields) {
		if(fields.isEmpty()) {
			return 0L;
		}
		String[] splittedFields = fields.split(" ");
		long bitboard = 0L;
		for(String splittedField : splittedFields) {
			Coordinates coordinates = BitboardUtils.coordinates(splittedField);
			int fieldIndex = BitboardUtils.toIndex(coordinates);
			bitboard |= 1L << fieldIndex;
		}
		return bitboard;
	}
	
	private void compare(long board1, long board2) {
		System.out.println(BitboardUtils.toBitboardString(board1));
		System.out.println(BitboardUtils.toBitboardString(board2));
	}
}
