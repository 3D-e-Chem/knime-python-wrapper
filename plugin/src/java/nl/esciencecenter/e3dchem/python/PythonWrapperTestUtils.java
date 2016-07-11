package nl.esciencecenter.e3dchem.python;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.knime.python.Activator;

public class PythonWrapperTestUtils {
    /**
     * The Python utilities in the py/ directory of org.knime.python plugin, are not available as files, they inside the jar of
     * the plugin. During tests these files are required. Force Knime to copy the files to a temporary location by fetching the
     * root directory.
     * 
     * @return File Directory with utility scripts
     *
     * @throws MalformedURLException
     * @throws IOException
     */
    public static File materializeKNIMEPythonUtils() throws MalformedURLException, IOException {
        return Activator.getFile("org.knime.python", "py");
    }
}
