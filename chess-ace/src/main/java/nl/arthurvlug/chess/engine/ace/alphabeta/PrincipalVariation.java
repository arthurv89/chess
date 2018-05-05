package nl.arthurvlug.chess.engine.ace.alphabeta;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import nl.arthurvlug.chess.engine.ace.UnapplyableMoveUtils;

class PrincipalVariation {
	static final int NO_MOVE = -9;
	private int[] line = new int[100];
	@Getter
	private int lineElements = 0;

	PrincipalVariation() {
		Arrays.fill(line, NO_MOVE);
	}

	void setBest(final int height, final @Nullable Integer bestMove) {
		if(bestMove == null) {
			return;
		}
		this.lineElements = height+1;
		this.line[height] = bestMove;
	}

	Integer getMoveAtHeight(final int height) {
		if(height < line.length) {
			return line[height];
		}
		return null;
	}

	int getPvHead() {
		return line[0];
	}

	int[] getLine() {
		return Arrays.copyOf(line, lineElements);
	}

	@Override
	public String toString() {
		return "[" + lineElements + "] " + Arrays.stream(line).boxed().filter(x -> x != NO_MOVE).map(x -> UnapplyableMoveUtils.toShortString(x)).collect(Collectors.toList());
	}

	int[] getRawLine() {
		return line;
	}

	void setRawLine(final int[] line) {
		this.line = line;
	}

	public void setLineElements(final int lineElements) {
		this.lineElements = lineElements;
	}
}
