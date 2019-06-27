package nl.esciencecenter.e3dchem.python;

import org.junit.Test;

public class PythonOptionsPanelTest {

	@Test
	public void testSaveSettingsTo() {
		PythonWrapperNodeConfig srcConfig = new PythonWrapperNodeConfig();
		
		PythonOptionsPanel<PythonWrapperNodeConfig> panel = new PythonOptionsPanel<PythonWrapperNodeConfig>();
		panel.loadSettingsFrom(srcConfig);
		
		PythonWrapperNodeConfig destConfig = new PythonWrapperNodeConfig();
		panel.saveSettingsTo(destConfig);
	}

}
