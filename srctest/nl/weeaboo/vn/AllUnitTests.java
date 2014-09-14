package nl.weeaboo.vn;

import nl.weeaboo.vn.math.MathTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	BaseEntityTest.class,
	MathTest.class,
	ScreenshotTest.class,
	ScreenTest.class})
public class AllUnitTests {

	static {
		TestUtil.configureLogger();
	}

}
