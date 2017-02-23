package nl.esciencecenter.e3dchem.python;

import java.util.HashSet;
import java.util.Set;

import org.knime.code.generic.VariableNames;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.workflow.FlowVariable;

/**
 * Configuration for {@link PythonWrapperNodeModel}.
 *
 * The variables returned by {@link #getOptionsValues()} will be made available
 * as `options` dict variable inside the Python script.
 *
 */
public abstract class PythonWrapperNodeConfig {
	protected final String pythonOptions = "options";
	protected final String flowVariables = "flow_variables";
	protected final String warningMessageFlowVariable = "warning_message";
	protected final VariableNames variableNames;

	public PythonWrapperNodeConfig() {
		this(new String[] { "input_table" }, new String[] { "output_table" });
	}

	public PythonWrapperNodeConfig(String[] inputTables, String[] outputTables) {
		this.variableNames = new VariableNames(flowVariables, inputTables, outputTables, null, null, null);
	}

	public VariableNames getVariableNames() {
		return variableNames;
	}

	/**
	 * Variable name inside Python script which is set to a dictionary filled by
	 * {@link #getOptionsValues()}.
	 *
	 * @return options name
	 */
	public String getOptionsName() {
		return pythonOptions;
	}

	/**
	 * Key inside Python script `flow_variables` dictionary of which the value
	 * is used to set the warning message of the node. If key is not present in
	 * `flow_variables` dictionary then no warning message will be set.
	 *
	 * @return key of flow_variables used as warning message
	 */
	public String getWarningMessageFlowVariable() {
		return warningMessageFlowVariable;
	}

	/**
	 * Save configuration to the given node settings.
	 *
	 * @param settings
	 *            The settings to save to
	 */
	public abstract void saveTo(final NodeSettingsWO settings);

	/**
	 * Load configuration from the given node settings.
	 *
	 * @param settings
	 *            The settings to load from
	 * @throws InvalidSettingsException
	 *             If the settings are invalid
	 */
	public abstract void loadFrom(final NodeSettingsRO settings) throws InvalidSettingsException;

	/**
	 * Set of key/value pairs which inside Python script will be a dictionary
	 * named by {@link #getOptionsName()}.
	 *
	 * @return options values
	 */
	public Set<FlowVariable> getOptionsValues() {
		Set<FlowVariable> variables = new HashSet<FlowVariable>();
		return variables;
	}
}
