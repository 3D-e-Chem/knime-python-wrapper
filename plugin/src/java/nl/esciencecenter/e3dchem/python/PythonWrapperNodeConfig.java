package nl.esciencecenter.e3dchem.python;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knime.python2.extensions.serializationlibrary.SentinelOption;
import org.knime.python2.extensions.serializationlibrary.SerializationOptions;
import org.knime.python2.generic.VariableNames;
import org.knime.python2.kernel.PythonKernelOptions;
import org.knime.python2.kernel.PythonKernelOptions.PythonVersionOption;
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
public class PythonWrapperNodeConfig {
	private static final String CFG_PYTHON_VERSION_OPTION = "pythonVersionOption";
	private static final String CFG_CONVERT_MISSING_TO_PYTHON = "convertMissingToPython";
	private static final String CFG_CONVERT_MISSING_FROM_PYTHON = "convertMissingFromPython";
	private static final String CFG_SENTINEL_OPTION = "sentinelOption";
	private static final String CFG_SENTINEL_VALUE = "sentinelValue";
	private static final String CFG_CHUNK_SIZE = "chunkSize";

	protected final String pythonOptions = "options";
	protected final String flowVariables = "flow_variables";
	protected final String warningMessageFlowVariable = "warning_message";
	protected final VariableNames variableNames;
	private PythonKernelOptions kernelOptions = new PythonKernelOptions();

	/**
	 * The variable names of the dataframes in the python workspace holding the input tables is set to `input_table`.
	 * The variable names of the dataframes in the python workspace holding the output tables is set to `output_table`.
	 */
	public PythonWrapperNodeConfig() {
		this(new String[] { "input_table" }, new String[] { "output_table" });
	}

	/**
	 * @param inputTables the variable names of the dataframes in the python workspace holding the input tables
	 * @param outputTables the variable names of the dataframes in the python workspace holding the output tables
	 */
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
	public void saveTo(final NodeSettingsWO settings) {
		settings.addString(CFG_PYTHON_VERSION_OPTION, kernelOptions.getPythonVersionOption().name());
		settings.addBoolean(CFG_CONVERT_MISSING_TO_PYTHON, kernelOptions.getConvertMissingToPython());
		settings.addBoolean(CFG_CONVERT_MISSING_FROM_PYTHON, kernelOptions.getConvertMissingFromPython());
		settings.addString(CFG_SENTINEL_OPTION, kernelOptions.getSentinelOption().name());
		settings.addInt(CFG_SENTINEL_VALUE, kernelOptions.getSentinelValue());
		settings.addInt(CFG_CHUNK_SIZE, kernelOptions.getChunkSize());
	}

	/**
	 * Load configuration from the given node settings.
	 *
	 * @param settings
	 *            The settings to load from
	 * @throws InvalidSettingsException
	 *             If the settings are invalid
	 */
	public void loadFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		kernelOptions.setPythonVersionOption(PythonVersionOption.valueOf(
				settings.getString(CFG_PYTHON_VERSION_OPTION, kernelOptions.getPreferencePythonVersion().name())));
		kernelOptions.setConvertMissingToPython(settings.getBoolean(CFG_CONVERT_MISSING_TO_PYTHON,
				SerializationOptions.DEFAULT_CONVERT_MISSING_TO_PYTHON));
		kernelOptions.setConvertMissingFromPython(settings.getBoolean(CFG_CONVERT_MISSING_FROM_PYTHON,
				SerializationOptions.DEFAULT_CONVERT_MISSING_FROM_PYTHON));
		kernelOptions.setSentinelOption(SentinelOption
				.valueOf(settings.getString(CFG_SENTINEL_OPTION, SerializationOptions.DEFAULT_SENTINEL_OPTION.name())));
		kernelOptions
				.setSentinelValue(settings.getInt(CFG_SENTINEL_VALUE, SerializationOptions.DEFAULT_SENTINEL_VALUE));
		kernelOptions.setChunkSize(settings.getInt(CFG_CHUNK_SIZE, PythonKernelOptions.DEFAULT_CHUNK_SIZE));
	}

	/**
	 * Load configuration from the given node settings (using defaults if
	 * necessary).
	 *
	 * @param settings
	 *            The settings to load from
	 */
	public void loadFromInDialog(final NodeSettingsRO settings) {
		kernelOptions.setPythonVersionOption(PythonVersionOption.valueOf(
				settings.getString(CFG_PYTHON_VERSION_OPTION, kernelOptions.getPreferencePythonVersion().name())));
		kernelOptions.setConvertMissingToPython(settings.getBoolean(CFG_CONVERT_MISSING_TO_PYTHON,
				SerializationOptions.DEFAULT_CONVERT_MISSING_TO_PYTHON));
		kernelOptions.setConvertMissingFromPython(settings.getBoolean(CFG_CONVERT_MISSING_FROM_PYTHON,
				SerializationOptions.DEFAULT_CONVERT_MISSING_FROM_PYTHON));
		kernelOptions.setSentinelOption(SentinelOption
				.valueOf(settings.getString(CFG_SENTINEL_OPTION, SerializationOptions.DEFAULT_SENTINEL_OPTION.name())));
		kernelOptions
				.setSentinelValue(settings.getInt(CFG_SENTINEL_VALUE, SerializationOptions.DEFAULT_SENTINEL_VALUE));
		kernelOptions.setChunkSize(settings.getInt(CFG_CHUNK_SIZE, PythonKernelOptions.DEFAULT_CHUNK_SIZE));
	}

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

	/**
	 * Sets the internal {@link PythonKernelOptions} to a new object created
	 * using the specified parameters.
	 *
	 * @param versionOption
	 *            the version options
	 * @param convertToPython
	 *            convert missing values to sentinel on the way to python
	 * @param convertFromPython
	 *            convert sentinel to missing values on the way from python to
	 *            KNIME
	 * @param sentinelOption
	 *            the sentinel option
	 * @param sentinelValue
	 *            the sentinel value (only used if sentinelOption is CUSTOM)
	 * @param chunkSize
	 *            the number of rows to transfer per chunk
	 */
	public void setKernelOptions(final PythonVersionOption versionOption, final boolean convertToPython,
			final boolean convertFromPython, final SentinelOption sentinelOption, final int sentinelValue,
			final int chunkSize) {
		List<String> requiredModules = kernelOptions.getAdditionalRequiredModules();
		kernelOptions = new PythonKernelOptions(versionOption, convertToPython, convertFromPython, sentinelOption,
				sentinelValue, chunkSize);
		for (String requiredModule : requiredModules) {
			kernelOptions.addRequiredModule(requiredModule);
		}
	}

	/**
	 * Gets the python kernel options.
	 *
	 * @return the python kernel options
	 */
	public PythonKernelOptions getKernelOptions() {
		return new PythonKernelOptions(kernelOptions);
	}

	/**
	 * Add an additional required module. A check for that module is performed
	 * on node execution.
	 *
	 * @param name
	 */
	public void addRequiredModule(String name) {
		kernelOptions.addRequiredModule(name);
	}
}
