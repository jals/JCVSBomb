package bomberman.test.unit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BombUnitTest.class, DoorUnitTest.class, HealthUnitTest.class, MoveUnitTest.class })
public class AllTests {

}
