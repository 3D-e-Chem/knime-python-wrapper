package nl.esciencecenter.e3dchem.python;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.knime.base.node.util.exttool.ExtToolOutputNodeModel;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.knime.python.kernel.PythonKernel;

/**
 * Implements a {@link NodeModel} for nodes that launch external Python script.
 *
 * @param <C>
 *            Configuration
 */
public abstract class PythonWrapperNodeModel<C extends PythonWrapperNodeConfig> extends ExtToolOutputNodeModel {
	protected C m_config = createConfig();
	/**
	 * Python script filename, relative to {@link NodeModel}
	 */
	protected String python_code_filename;
	/**
	 * Packages that the Python script depends on can be checked in the
	 * {@link #configure()} method.
	 */
	protected List<String> required_python_packages = Arrays.asList();

	public PythonWrapperNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
	}

	protected abstract C createConfig();

	protected final C getConfig() {
		return m_config;
	}

	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		// Below has been copied from Knime Python node source code and
		// adjusted.
		PythonKernel kernel = new PythonKernel();
		try {
			return executeKernel(inData, exec, kernel);
		} finally {
			kernel.close();
		}
	}

	public BufferedDataTable[] executeKernel(BufferedDataTable[] inData, ExecutionContext exec, PythonKernel kernel)
			throws IOException, Exception {
		C config = getConfig();
		BufferedDataTable[] outData = new BufferedDataTable[config.getVariableNames().getOutputTables().length];
		kernel.putFlowVariables(config.getVariableNames().getFlowVariables(), getAvailableFlowVariables().values());
		for (int i = 0; i < config.getVariableNames().getInputTables().length; i++) {
			kernel.putDataTable(config.getVariableNames().getInputTables()[i], inData[i], exec.createSubProgress(0.3));
		}
		// Make the options from the node dialog via the PythonWrapperNodeConfig
		// instance available in the Python script
		kernel.putFlowVariables(config.getOptionsName(), m_config.getOptionsValues());
		String[] output = kernel.execute(getPythonCode(), exec);
		setExternalOutput(new LinkedList<String>(Arrays.asList(output[0].split("\n"))));
		setExternalErrorOutput(new LinkedList<String>(Arrays.asList(output[1].split("\n"))));
		exec.createSubProgress(0.4).setProgress(1);
		Collection<FlowVariable> variables = kernel.getFlowVariables(config.getVariableNames().getFlowVariables());
		for (FlowVariable flowVariable : variables) {
			if (flowVariable.getName().equals(config.getWarningMessageFlowVariable())) {
				setWarningMessage(flowVariable.getStringValue());
			}
		}
		addNewVariables(variables);
		for (int i = 0; i < config.getVariableNames().getOutputTables().length; i++) {
			outData[i] = kernel.getDataTable(config.getVariableNames().getOutputTables()[i], exec,
					exec.createSubProgress(0.3));
		}
		return outData;
	}

	/**
	 * Push new variables to the stack.
	 *
	 * Only pushes new variables to the stack if they are new or changed in type
	 * or value.
	 *
	 * @param newVariables
	 *            The flow variables to push
	 */
	protected void addNewVariables(Collection<FlowVariable> newVariables) {
		// Below has been copied from Knime Python node source code and
		// adjusted.
		Map<String, FlowVariable> flowVariables = getAvailableFlowVariables();
		for (FlowVariable variable : newVariables) {
			// Only push if variable is new or has changed type or value
			boolean push = true;
			if (flowVariables.containsKey(variable.getName())) {
				// Old variable with the name exists
				FlowVariable oldVariable = flowVariables.get(variable.getName());
				if (oldVariable.getType().equals(variable.getType())) {
					// Old variable has the same type
					if (variable.getType().equals(Type.INTEGER)
							&& oldVariable.getIntValue() == variable.getIntValue()) {
						// Old variable has the same value
						push = false;
					} else if (variable.getType().equals(Type.DOUBLE)
							&& Double.valueOf(oldVariable.getDoubleValue()).equals(Double.valueOf(variable.getDoubleValue()))) {
						// Old variable has the same value
						push = false;
					} else if (variable.getType().equals(Type.STRING)
							&& oldVariable.getStringValue().equals(variable.getStringValue())) {
						// Old variable has the same value
						push = false;
					}
				}

			}
			if (push) {
				if (variable.getType().equals(Type.INTEGER)) {
					pushFlowVariableInt(variable.getName(), variable.getIntValue());
				} else if (variable.getType().equals(Type.DOUBLE)) {
					pushFlowVariableDouble(variable.getName(), variable.getDoubleValue());
				} else if (variable.getType().equals(Type.STRING)) {
					pushFlowVariableString(variable.getName(), variable.getStringValue());
				}
			}
		}
	}

	public String getPythonCode() throws FileNotFoundException {
		try {
			InputStream inputStream = getClass().getResourceAsStream(python_code_filename);
			String result = new BufferedReader(new InputStreamReader(inputStream)).lines()
					.collect(Collectors.joining("\n"));
			return result;
		} catch (NullPointerException e) {
			throw new FileNotFoundException(python_code_filename);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_config.saveTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		C config = createConfig();
		config.loadFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		C config = createConfig();
		config.loadFrom(settings);
		m_config = config;
	}

	/**
	 * The output spec returned by the configure() method.
	 *
	 * @param inSpecs
	 * @return
	 */
	protected DataTableSpec[] getOutputSpecs(DataTableSpec[] inSpecs) {
		return new DataTableSpec[] { null };
	}

	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		validPython();
		return getOutputSpecs(inSpecs);
	}

	/**
	 * Checks if Python packages in {@link #required_python_packages
	 * required_python_packages} field can be imported.
	 *
	 * @throws InvalidSettingsException
	 */
	public void validPython() throws InvalidSettingsException {
		if (required_python_packages.isEmpty()) {
			return;
		}
		String pythonCommand = org.knime.python.Activator.getPythonCommand();
		String program = required_python_packages.stream().map(p -> "import " + p).collect(Collectors.joining(";"));
		ProcessBuilder pb = new ProcessBuilder(pythonCommand, "-c", program);
		try {
			Process process = pb.start();
			int exitValue = process.waitFor();
			if (exitValue != 0) {
				String msg = "Missing ";
				msg += required_python_packages.stream().map(p -> "'" + p + "'").collect(Collectors.joining(" or "));
				msg += " Python package(s), please install or correct Python executable";
				throw new InvalidSettingsException(msg);
			}
		} catch (IOException | InterruptedException e) {
			throw new InvalidSettingsException("Failed to check required Python packages", e);
		}
	}
}
