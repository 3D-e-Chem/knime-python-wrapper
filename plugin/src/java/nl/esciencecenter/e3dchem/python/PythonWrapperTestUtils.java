package nl.esciencecenter.e3dchem.python;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.knime.python2.Activator;
import org.osgi.service.prefs.BackingStoreException;

public class PythonWrapperTestUtils {
	/**
	 * The Python utilities in the py/ directory of org.knime.python2 plugin, are
	 * not available as files, they inside the jar of the plugin. During tests these
	 * files are required. Force KNIME to copy the files to a temporary location by
	 * fetching the root directory.
	 *
	 * @return File Directory with utility scripts
	 *
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static File materializeKNIMEPythonUtils() throws MalformedURLException, IOException {
		File file = Activator.getFile("org.knime.python2", "py");
		Activator.getFile("org.knime.python2.serde.flatbuffers", "py");
		return file;
	}

	/**
	 * The org.knime.python2 plugin defaults to using a conda environment which is
	 * configured in the preferences. During testing the environment has not been
	 * configured causing the wrong Python to be used. By calling this function the
	 * org.knime.python2 plugin preferences are configured to use the `python3`
	 * executable in the current PATH.
	 *
	 * @throws BackingStoreException when preferences could not be flushed.
	 */
	public static void activateManualPython3() throws BackingStoreException {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("org.knime.python2");
		prefs.put("pythonEnvironmentType", "manual");
		prefs.put("python3Path", "python3");
		prefs.put("defaultPythonOption", "python3");
		prefs.flush();
	}

	/**
	 * Configures org.knime.python2 plugin so it can be used in test. Adds the
	 * KNIME-Python utility scripts to the Python path and configure it to use the
	 * `python3` executable in current PATH.
	 *
	 * @throws BackingStoreException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void init() throws BackingStoreException, MalformedURLException, IOException {
		activateManualPython3();
		materializeKNIMEPythonUtils();
	}
}
