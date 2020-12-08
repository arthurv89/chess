package nl.arthurvlug.chess.engine.ace.alphabeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;

class PrincipalVariation {
	static final int NO_MOVE = -9;

	private final int[] rawLine = new int[100];

	@Getter
	private int lineElementCount = 0;

	PrincipalVariation() {
		Arrays.fill(rawLine, NO_MOVE);
	}

	public static PrincipalVariation singleton(int move) {
		PrincipalVariation pv = new PrincipalVariation();
		pv.rawLine[0] = move;
		pv.lineElementCount = 1;
		return pv;
	}

	Integer getMoveAtHeight(final int height) {
		if (height < rawLine.length) {
			return rawLine[height];
		}
		throw new RuntimeException("Can't get this move");
	}

	int getPvHead() {
		return rawLine[0];
	}

	int[] getRawLineCopy() {
		return Arrays.copyOf(rawLine, lineElementCount);
	}

	static void copyElements(PrincipalVariation src_pv, int srcPos, PrincipalVariation dest_pv, int destPos) {
		System.arraycopy(
				src_pv.rawLine, srcPos,
				dest_pv.rawLine, destPos,
				src_pv.rawLine.length - 1);
		dest_pv.lineElementCount = src_pv.lineElementCount + destPos;
	}

	@Override
	public String toString() {
		List<String> moves = Arrays.stream(getRawLineCopy())
				.boxed()
				.filter(x -> x != NO_MOVE)
				.map(x -> UnapplyableMoveUtils.toShortString(x))
				.collect(Collectors.toList());
		return String.valueOf(moves);
	}

	public void replace(Integer move, PrincipalVariation pline) {
		rawLine[0] = move;
		copyElements(pline, 0, this, 1);
	}
}
