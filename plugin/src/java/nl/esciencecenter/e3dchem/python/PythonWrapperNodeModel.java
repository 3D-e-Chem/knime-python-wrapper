package nl.esciencecenter.e3dchem.python;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
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
import org.knime.python2.PythonCommand;
import org.knime.python2.PythonModuleSpec;
import org.knime.python2.config.PythonFlowVariableOptions;
import org.knime.python2.kernel.PythonCancelable;
import org.knime.python2.kernel.PythonCanceledExecutionException;
import org.knime.python2.kernel.PythonExecutionMonitorCancelable;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernel;
import org.knime.python2.kernel.PythonKernelOptions;
import org.knime.python2.kernel.PythonKernelQueue;

/**
 * Implements a {@link NodeModel} for nodes that launch external Python script.
 *
 * @param <C> Configuration
 */
public abstract class PythonWrapperNodeModel<C extends PythonWrapperNodeConfig> extends ExtToolOutputNodeModel {
	protected C m_config = createConfig();
	/**
	 * Python script filename, relative to {@link NodeModel}
	 */
	protected String python_code_filename;

	public PythonWrapperNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
	}

	protected abstract C createConfig();

	protected final C getConfig() {
		return m_config;
	}

	/**
	 * Gets the kernel specific options.
	 *
	 * @return the kernel specific options
	 */
	protected PythonKernelOptions getKernelOptions() {
		final PythonKernelOptions options = getConfig().getKernelOptions();
		final String serializerId = new PythonFlowVariableOptions(getAvailableFlowVariables()).getSerializerId()
				.orElse(null);
		return options.forSerializationOptions(options.getSerializationOptions().forSerializerId(serializerId));
	}

	// Below has been copied from KNIME Python node code (https://github.com/knime/knime-python/blob/analytics-platform/4.3.0/org.knime.python2.nodes/src/org/knime/python2/nodes/PythonNodeModel.java) and
	// adjusted.
	protected PythonKernel getNextKernelFromQueue(final PythonCancelable cancelable)
			throws PythonCanceledExecutionException, PythonIOException {
		return getNextKernelFromQueue(Collections.emptySet(), Collections.emptySet(), cancelable);
	}

	protected PythonKernel getNextKernelFromQueue(final Set<PythonModuleSpec> requiredAdditionalModules,
			final PythonCancelable cancelable) throws PythonCanceledExecutionException, PythonIOException {
		return getNextKernelFromQueue(requiredAdditionalModules, Collections.emptySet(), cancelable);
	}

	protected PythonKernel getNextKernelFromQueue(final Set<PythonModuleSpec> requiredAdditionalModules,
			final Set<PythonModuleSpec> optionalAdditionalModules, final PythonCancelable cancelable)
			throws PythonCanceledExecutionException, PythonIOException {
		final PythonKernelOptions options = getKernelOptions();
		final PythonCommand command = options.getUsePython3() //
				? options.getPython3Command() //
				: options.getPython2Command();
		return PythonKernelQueue.getNextKernel(command, requiredAdditionalModules, optionalAdditionalModules, options,
				cancelable);
	}

	public BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		// Below has been copied from KNIME Python node source code (https://github.com/knime/knime-python/blob/analytics-platform/4.3.0/org.knime.python2.nodes/src/org/knime/python2/nodes/source/PythonSourceNodeModel.java) and
		// adjusted.
		final PythonExecutionMonitorCancelable cancelable = new PythonExecutionMonitorCancelable(exec);
		try (final PythonKernel kernel = getNextKernelFromQueue(cancelable)) {
			kernel.setOptions(getConfig().getKernelOptions());
			try {
				return executeKernel(inData, exec, kernel);
			} finally {
				kernel.close();
			}
		}
	}

	public BufferedDataTable[] executeKernel(BufferedDataTable[] inData, ExecutionContext exec, PythonKernel kernel)
			throws IOException, Exception {
		C config = getConfig();
		kernel.putFlowVariables(config.getVariableNames().getFlowVariables(), getAvailableFlowVariables().values());
		int nrInputTables = config.getVariableNames().getInputTables().length;
		double inputProgressStep = 0.3 / nrInputTables;
		for (int i = 0; i < nrInputTables; i++) {
			kernel.putDataTable(config.getVariableNames().getInputTables()[i], inData[i],
					exec.createSubProgress(inputProgressStep));
		}
		// Make the options from the node dialog via the PythonWrapperNodeConfig
		// instance available in the Python script
		kernel.putFlowVariables(config.getOptionsName(), config.getOptionsValues());
		String[] output = kernel.execute(getPythonCode(), new PythonExecutionMonitorCancelable(exec));
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
		int nrOutputTables = config.getVariableNames().getOutputTables().length;
		BufferedDataTable[] outData = new BufferedDataTable[nrOutputTables];
		double outProgressStep = 0.3 / nrOutputTables;
		for (int i = 0; i < nrOutputTables; i++) {
			outData[i] = kernel.getDataTable(config.getVariableNames().getOutputTables()[i], exec,
					exec.createSubProgress(outProgressStep));
		}
		return outData;
	}

	/**
	 * Push new variables to the stack.
	 *
	 * Only pushes new variables to the stack if they are new or changed in type or
	 * value.
	 *
	 * @param newVariables The flow variables to push
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
					} else if (variable.getType().equals(Type.DOUBLE) && Double.valueOf(oldVariable.getDoubleValue())
							.equals(Double.valueOf(variable.getDoubleValue()))) {
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
		return getOutputSpecs(inSpecs);
	}
}
