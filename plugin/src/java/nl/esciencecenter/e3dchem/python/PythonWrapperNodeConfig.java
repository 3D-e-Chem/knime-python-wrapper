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
 * The variables returned by {@link #getOptionsValues()} will be made available as `options` dict variable inside the Python
 * script.
 *
 */
public abstract class PythonWrapperNodeConfig {
    private static final String PYTHON_OPTIONS = "options";
    private static final VariableNames VARIABLE_NAMES = new VariableNames("flow_variables", new String[] { "input_table" },
            new String[] { "output_table" }, null, null, null);

    public static VariableNames getVariableNames() {
        return VARIABLE_NAMES;
    }

    /**
     * Variable name inside Python script which is set to a dictionary filled by {@link #getOptionsValues()}.
     *
     * @return options name
     */
    public static String getOptionsName() {
        return PYTHON_OPTIONS;
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
     * Set of key/value pairs which inside Python script will be a dictionary named by {@link #getOptionsName()}.
     *
     * @return options values
     */
    public Set<FlowVariable> getOptionsValues() {
        Set<FlowVariable> variables = new HashSet<FlowVariable>();
        return variables;
    }
}
