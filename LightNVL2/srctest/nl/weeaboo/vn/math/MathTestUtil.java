package nl.weeaboo.vn.math;

import org.junit.Assert;

final class MathTestUtil {

    public static final double EPSILON = 0.0001;

    private MathTestUtil() {
    }

    public static void assertEquals(AbstractMatrix alpha, AbstractMatrix beta) {
        assertEquals(alpha, beta, EPSILON);
    }
    public static void assertEquals(AbstractMatrix alpha, AbstractMatrix beta, double epsilon) {
        Assert.assertTrue(alpha + " != " + beta, alpha.equals(beta, epsilon));
    }

}
