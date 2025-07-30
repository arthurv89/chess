package nl.arthurvlug.chess.engine.ace.board;

import nl.arthurvlug.chess.utils.board.FieldUtils;

import static nl.arthurvlug.chess.engine.customEngine.movegeneration.BitboardUtils.bitboardFromFieldName;

public class StaticAceBoard {

    // Black rook positions
    static final long a8Bitboard = bitboardFromFieldName("a8");
    static final long d8Bitboard = bitboardFromFieldName("d8");
    static final long f8Bitboard = bitboardFromFieldName("f8");
    static final long h8Bitboard = bitboardFromFieldName("h8");
    // King positions
    static final long g8Bitboard = bitboardFromFieldName("g8");
    static final long c8Bitboard = bitboardFromFieldName("c8");
    public static final long e8Bitboard = bitboardFromFieldName("e8");

    // White castling positions
    static final long a1Bitboard = bitboardFromFieldName("a1");
    static final long d1Bitboard = bitboardFromFieldName("d1");
    static final long f1Bitboard = bitboardFromFieldName("f1");
    static final long h1Bitboard = bitboardFromFieldName("h1");
    // King positions
    static final long g1Bitboard = bitboardFromFieldName("g1");
    static final long c1Bitboard = bitboardFromFieldName("c1");
    public static final long e1Bitboard = bitboardFromFieldName("e1");

    public final static long first_row = bitboardFromFieldName("a1 b1 c1 d1 e1 f1 g1 h1");
    public final static long last_row = bitboardFromFieldName("a8 b8 c8 d8 e8 f8 g8 h8");

    final static short a1FieldIdx = FieldUtils.fieldIdx("a1");
    final static short d1FieldIdx = FieldUtils.fieldIdx("d1");
    final static short f1FieldIdx = FieldUtils.fieldIdx("f1");
    final static short h1FieldIdx = FieldUtils.fieldIdx("h1");

    final static short d8FieldIdx = FieldUtils.fieldIdx("d8");
    final static short a8FieldIdx = FieldUtils.fieldIdx("a8");
    final static short f8FieldIdx = FieldUtils.fieldIdx("f8");
    final static short h8FieldIdx = FieldUtils.fieldIdx("h8");
    static int[][] zobristRandomTable = new int[64][13];
}
