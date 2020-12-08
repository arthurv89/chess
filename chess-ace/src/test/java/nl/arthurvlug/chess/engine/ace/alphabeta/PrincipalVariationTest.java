package nl.arthurvlug.chess.engine.ace.alphabeta;

import org.junit.Test;

import static nl.arthurvlug.chess.engine.ace.alphabeta.PrincipalVariation.NO_MOVE;
import static org.assertj.core.api.Assertions.assertThat;

public class PrincipalVariationTest {
    @Test
    public void testInit() {
        PrincipalVariation pv = new PrincipalVariation();

        assertThat(pv.getRawLineCopy()).hasSize(0);
        assertThat(pv.getLineElementCount()).isEqualTo(0);
        assertThat(pv.getMoveAtHeight(0)).isEqualTo(NO_MOVE);
        assertThat(pv.getPvHead()).isEqualTo(NO_MOVE);
        assertThat(pv.toString()).isEqualTo("[]");
    }

    @Test
    public void testSetFirstElement() {
        PrincipalVariation pv = PrincipalVariation.singleton(123);

        assertThat(pv.getRawLineCopy()).hasSize(1);
        assertThat(pv.getLineElementCount()).isEqualTo(1);
        assertThat(pv.getMoveAtHeight(0)).isEqualTo(123);
        assertThat(pv.getPvHead()).isEqualTo(123);
        assertThat(pv.toString()).isEqualTo("[d8b1]");
    }

    @Test
    public void testCopy0() {
        PrincipalVariation pv = PrincipalVariation.singleton(123);
        PrincipalVariation pv2 = new PrincipalVariation();
        PrincipalVariation.copyElements(pv, 0, pv2, 0);

        assertThat(pv2.getRawLineCopy()).hasSize(1);
        assertThat(pv2.getLineElementCount()).isEqualTo(1);
        assertThat(pv2.getMoveAtHeight(0)).isEqualTo(123);
        assertThat(pv2.getPvHead()).isEqualTo(123);
        assertThat(pv2.toString()).isEqualTo("[d8b1]");
    }

    @Test
    public void testCopy1() {
        PrincipalVariation pv = PrincipalVariation.singleton(123);
        PrincipalVariation pv2 = PrincipalVariation.singleton(456);
        PrincipalVariation.copyElements(pv, 0, pv2, 1);

        assertThat(pv2.getRawLineCopy()).isEqualTo(new int[] {456, 123});
        assertThat(pv2.getLineElementCount()).isEqualTo(2);
        assertThat(pv2.getMoveAtHeight(0)).isEqualTo(456);
        assertThat(pv2.getMoveAtHeight(1)).isEqualTo(123);
        assertThat(pv2.getPvHead()).isEqualTo(456);
        assertThat(pv2.toString()).isEqualTo("[a2h1, d8b1]");
    }
}
