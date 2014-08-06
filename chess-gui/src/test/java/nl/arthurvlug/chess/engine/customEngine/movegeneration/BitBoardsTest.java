package nl.arthurvlug.chess.engine.customEngine.movegeneration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BitBoardsTest {
	@Test
	public void testKingXRay() {
		long[] xray = Xray.king_xray;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("b1 a2 b2");
		assertEquals(a1, xray[0]);
		assertEquals(0x302L, xray[0]);
		
		long b1 = bitboard("a1 c1 a2 b2 c2");
		assertEquals(b1, xray[1]);
		
		long b2 = bitboard("a1 b1 c1 a2 c2 a3 b3 c3");
		assertEquals(b2, xray[9]);
		
		long h8 = bitboard("g7 h7 g8");
		assertEquals(h8, xray[63]);
	}

	@Test
	public void testKnightXRay() {
		long[] xray = Xray.knight_xray;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("b3 c2");
		assertEquals(a1, xray[0]);
		
		long b1 = bitboard("a3 c3 d2");
		assertEquals(b1, xray[1]);
		
		long c3 = bitboard("b1 a2 a4 b5 d5 e4 e2 d1");
		assertEquals(c3, xray[18]);
		
		long h8 = bitboard("g6 f7");
		assertEquals(h8, xray[63]);
	}

	@Test
	public void testBishopXRay() {
		long[] xray = Xray.bishop_xray;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("b2 c3 d4 e5 f6 g7 h8");
		assertEquals(a1, xray[0]);
		
		long c2 = bitboard("b1 d1 b3 a4 d3 e4 f5 g6 h7");
		assertEquals(c2, xray[10]);
		
		long h8 = bitboard("a1 b2 c3 d4 e5 f6 g7");
		assertEquals(h8, xray[63]);
	}

	@Test
	public void testRookXRay() {
		long[] xray = Xray.rook_xray;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("a2 a3 a4 a5 a6 a7 a8 b1 c1 d1 e1 f1 g1 h1");
		assertEquals(a1, xray[0]);
		
		long c2 = bitboard("c1 c3 c4 c5 c6 c7 c8 a2 b2 d2 e2 f2 g2 h2");
		assertEquals(c2, xray[10]);
		
		long h8 = bitboard("a8 b8 c8 d8 e8 f8 g8 h7 h6 h5 h4 h3 h2 h1");
		assertEquals(h8, xray[63]);
	}

	@Test
	public void testQueenXRay() {
		long[] xray = Xray.queen_xray;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("b2 c3 d4 e5 f6 g7 h8 a2 a3 a4 a5 a6 a7 a8 b1 c1 d1 e1 f1 g1 h1");
		assertEquals(a1, xray[0]);
		
		long c2 = bitboard("b1 d1 b3 a4 d3 e4 f5 g6 h7 c1 c3 c4 c5 c6 c7 c8 a2 b2 d2 e2 f2 g2 h2");
		assertEquals(c2, xray[10]);
		
		long h8 = bitboard("a1 b2 c3 d4 e5 f6 g7 a8 b8 c8 d8 e8 f8 g8 h7 h6 h5 h4 h3 h2 h1");
		assertEquals(h8, xray[63]);
	}

	@Test
	public void testWhitePawnXRay() {
		long[] xray = Xray.pawn_xray_white;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("");
		assertEquals(a1, xray[0]);
		
		long c2 = bitboard("c3 c4");
		assertEquals(c2, xray[10]);
		
		long c3 = bitboard("c4");
		assertEquals(c3, xray[18]);
		
		long h7 = bitboard("h8");
		assertEquals(h7, xray[55]);
		
		long h8 = bitboard("");
		assertEquals(h8, xray[63]);
	}
	
	@Test
	public void testWhitePawnCaptureXRay() {
		long[] xray = Xray.pawn_xray_white_capture;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("");
		assertEquals(a1, xray[0]);
		
		long a2 = bitboard("b3");
		assertEquals(a2, xray[8]);
		
		long c3 = bitboard("b4 d4");
		assertEquals(c3, xray[18]);
		
		long h7 = bitboard("g8");
		assertEquals(h7, xray[55]);
		
		long h8 = bitboard("");
		assertEquals(h8, xray[63]);
	}

	@Test
	public void testBlackPawnXRay() {
		long[] xray = Xray.pawn_xray_black;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("");
		assertEquals(a1, xray[0]);
		
		long c7 = bitboard("c6 c5");
		assertEquals(c7, xray[50]);
		
		long c6 = bitboard("c5");
		assertEquals(c6, xray[42]);
		
		long h2 = bitboard("h1");
		assertEquals(h2, xray[15]);
		
		long h8 = bitboard("");
		assertEquals(h8, xray[63]);
	}
	
	@Test
	public void testBlackPawnCaptureXRay() {
		long[] xray = Xray.pawn_xray_black_capture;
		assertEquals(64, xray.length);
		
		long a1 = bitboard("");
		assertEquals(a1, xray[0]);
		
		long a7 = bitboard("b6");
		assertEquals(a7, xray[48]);
		
		long c6 = bitboard("b5 d5");
		assertEquals(c6, xray[42]);
		
		long h2 = bitboard("g1");
		assertEquals(h2, xray[15]);
		
		long h1 = bitboard("");
		assertEquals(h1, xray[63]);
	}
	
	

	private long bitboard(String fields) {
		if(fields.isEmpty()) {
			return 0L;
		}
		String[] splittedFields = fields.split(" ");
		long bitboard = 0L;
		for(String splittedField : splittedFields) {
			int x = splittedField.charAt(0) - 'a';
			int y = splittedField.charAt(1) - 1;
			int fieldIndex = 8*y + x;
			bitboard |= 1L << fieldIndex;
		}
		return bitboard;
	}
	
	private void compare(long board1, long board2) {
		System.out.println(toBitboardString(board1));
		System.out.println(toBitboardString(board2));
	}

	private String toBitboardString(long h8) {
		return Long.toBinaryString(h8);
	}
}
