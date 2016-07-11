package nl.esciencecenter.e3dchem.python;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class PythonWrapperTestUtilsTest {

    @Test
    public void testMaterializeKNIMEPythonUtils() throws IOException {
        File pyDir = PythonWrapperTestUtils.materializeKNIMEPythonUtils();

        assertTrue(pyDir.canRead());
        assertTrue(pyDir.isDirectory());
    }

}
