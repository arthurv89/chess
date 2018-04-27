package nl.arthurvlug.chess.engine.ace.movegeneration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnapplyableMoveTest {
	private final byte fromIdx = 12;
	private final byte targetIdx = 23;
	private final byte movingPiece = 3;
	private final byte takePiece = 2;
	private final byte promotionPiece = 5;

	private final int move = UnapplyableMove.create(fromIdx, targetIdx, movingPiece, takePiece, promotionPiece);

	@Test
	public void testFromIdx() {
		assertThat(UnapplyableMove.fromIdx(move)).isEqualTo(fromIdx);
	}

	@Test
	public void testTargetIdx() {
		assertThat(UnapplyableMove.targetIdx(move)).isEqualTo(targetIdx);
	}

	@Test
	public void testMovingPiece() {
		assertThat(UnapplyableMove.coloredMovingPiece(move)).isEqualTo(movingPiece);
	}

	@Test
	public void testTakePiece() {
		assertThat(UnapplyableMove.takePiece(move)).isEqualTo(takePiece);
	}

	@Test
	public void testPromotionPiece() {
		assertThat(UnapplyableMove.promotionPiece(move)).isEqualTo(promotionPiece);
	}
}
