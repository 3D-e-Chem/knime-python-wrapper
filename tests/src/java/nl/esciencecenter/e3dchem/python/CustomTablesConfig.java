package nl.esciencecenter.e3dchem.python;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class CustomTablesConfig extends PythonWrapperNodeConfig {
	public CustomTablesConfig() {
		super(new String[] {}, new String[] { "output_table1", "output_table2" });
	}

	@Override
	public void saveTo(NodeSettingsWO settings) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}
}
