package nl.esciencecenter.e3dchem.python;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;
import org.knime.python2.PythonCommand;
import org.knime.python2.PythonVersion;
import org.knime.python2.config.PythonEnvironmentType;
import org.knime.python2.prefs.PythonPreferences;
import org.osgi.service.prefs.BackingStoreException;

public class PythonWrapperTestUtilsTest {

	@Test
	public void testMaterializeKNIMEPythonUtils() throws IOException {
		File pyDir = PythonWrapperTestUtils.materializeKNIMEPythonUtils();

		assertTrue(pyDir.canRead());
		assertTrue(pyDir.isDirectory());
	}

	@Test
	public void testInit() throws MalformedURLException, BackingStoreException, IOException {
		PythonWrapperTestUtils.init();

		PythonEnvironmentType envtype = PythonPreferences.getEnvironmentTypePreference();
		assertEquals(PythonEnvironmentType.MANUAL, envtype);
		PythonVersion version = PythonPreferences.getPythonVersionPreference();
		assertEquals(PythonVersion.PYTHON3, version);
		PythonCommand cmd = PythonPreferences.getPython3CommandPreference();
		assertEquals("python3", cmd.toString());
	}
}
