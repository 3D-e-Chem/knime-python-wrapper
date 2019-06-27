package nl.esciencecenter.e3dchem.python;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.knime.python2.DefaultPythonCommand;
import org.knime.python2.PythonCommand;
import org.knime.python2.PythonModuleSpec;
import org.knime.python2.PythonVersion;
import org.knime.python2.extensions.serializationlibrary.SentinelOption;
import org.knime.python2.extensions.serializationlibrary.SerializationOptions;
import org.knime.python2.generic.VariableNames;
import org.knime.python2.kernel.PythonKernelOptions;
import org.knime.python2.prefs.PythonPreferences;
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
    static final String CFG_PYTHON2COMMAND = "python2Command";
    static final String CFG_PYTHON3COMMAND = "python3Command";
	private static final String CFG_CONVERT_MISSING_TO_PYTHON = "convertMissingToPython";
	private static final String CFG_CONVERT_MISSING_FROM_PYTHON = "convertMissingFromPython";
	private static final String CFG_SENTINEL_OPTION = "sentinelOption";
	private static final String CFG_SENTINEL_VALUE = "sentinelValue";
	private static final String CFG_CHUNK_SIZE = "chunkSize";

    /**
     * {@link #m_python2Command} and {@link #m_python3Command} are special in that they currently aren't configurable
     * via a Python scripting node's dialog but only using flow variables. If no respective flow variables are set,
     * their value is retrieved from the Python preferences.
     */
    private static final String INDICATE_FALLBACK_TO_PREFERENCES_COMMAND_VALUE = "";

    private PythonVersion m_pythonVersion = PythonPreferences.getPythonVersionPreference();

    /**
     * {@code null} means to fall back to {@link PythonPreferences#getPython2CommandPreference()}.
     */
    private PythonCommand m_python2Command = null;

    /**
     * {@code null} means to fall back to {@link PythonPreferences#getPython2CommandPreference()}.
     */
    private PythonCommand m_python3Command = null;

    private int m_chunkSize = SerializationOptions.DEFAULT_CHUNK_SIZE;

    private boolean m_convertMissingToPython = SerializationOptions.DEFAULT_CONVERT_MISSING_TO_PYTHON;

    private boolean m_convertMissingFromPython = SerializationOptions.DEFAULT_CONVERT_MISSING_FROM_PYTHON;

    private SentinelOption m_sentinelOption = SerializationOptions.DEFAULT_SENTINEL_OPTION;

    private int m_sentinelValue = SerializationOptions.DEFAULT_SENTINEL_VALUE;
    
	protected final String pythonOptions = "options";
	protected final String flowVariables = "flow_variables";
	protected final String warningMessageFlowVariable = "warning_message";
	protected final VariableNames variableNames;
	private String externalCustomPath = "";
	private Set<PythonModuleSpec> additionalRequiredModules = new HashSet<PythonModuleSpec>();

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
     * Indicates whether Python 3 shall be used by Python scripting nodes that are configured by this config instance.
     *
     * @return {@code true} if Python 3 shall be used, {@code false} if Python 2 shall be used.
     */
    public boolean getUsePython3() {
        return m_pythonVersion.equals(PythonVersion.PYTHON3);
    }

    /**
     * @return The configured Python version.
     */
    public PythonVersion getPythonVersion() {
        return m_pythonVersion;
    }

    /**
     * @param pythonVersion The configured Python version.
     */
    public void setPythonVersion(final PythonVersion pythonVersion) {
        m_pythonVersion = pythonVersion;
    }

    /**
     * @return The Python 2 command to use. May be {@code null} in which case no specific Python 2 command is configured
     *         and one has to resort to - e.g., - the {@link PythonPreferences#getPython2CommandPreference() global
     *         preferences}.
     */
    public PythonCommand getPython2Command() {
        return m_python2Command;
    }

    /**
     * @param python2Command The Python 2 command to use. May be {@code null} in which case no specific Python 2 command
     *            is configured and one has to resort to - e.g., - the
     *            {@link PythonPreferences#getPython2CommandPreference() global preferences}.
     */
    public void setPython2Command(final PythonCommand python2Command) {
        m_python2Command = python2Command;
    }

    /**
     * @return The Python 3 command to use. May be {@code null} in which case no specific Python 3 command is configured
     *         and one has to resort to - e.g., - the {@link PythonPreferences#getPython3CommandPreference() global
     *         preferences}.
     */
    public PythonCommand getPython3Command() {
        return m_python3Command;
    }

    /**
     * @param python3Command The Python 3 command to use. May be {@code null} in which case no specific Python 3 command
     *            is configured and one has to resort to - e.g., - the
     *            {@link PythonPreferences#getPython3CommandPreference() global preferences}.
     */
    public void setPython3Command(final PythonCommand python3Command) {
        m_python3Command = python3Command;
    }

    /**
    *
    * @return The configured number of rows to transfer to/from Python per chunk of an input/output table.
    */
   public int getChunkSize() {
       return m_chunkSize;
   }

   /**
    *
    * @param chunkSize The configured number of rows to transfer to/from Python per chunk of an input/output table.
    */
   public void setChunkSize(final int chunkSize) {
       m_chunkSize = chunkSize;
   }

   /**
    * @return {@true} if missing values shall be converted to sentinel values on the way to Python. {@code false} if
    *         they shall remain missing.
    */
   public boolean getConvertMissingToPython() {
       return m_convertMissingToPython;
   }

   /**
    * @param convertMissingToPython {@true} to configure that missing values shall be converted to sentinel values on
    *            the way to Python. {@code false} if they shall remain missing.
    */
   public void setConvertMissingToPython(final boolean convertMissingToPython) {
       m_convertMissingToPython = convertMissingToPython;
   }

   /**
    * @return {@true} if missing values shall be converted to sentinel values on the way back from Python.
    *         {@code false} if they shall remain missing.
    */
   public boolean getConvertMissingFromPython() {
       return m_convertMissingFromPython;
   }

   /**
    * @param convertMissingFromPython {@true} to configure that missing values shall be converted to sentinel values on
    *            the way back from Python. {@code false} if they shall remain missing.
    */
   public void setConvertMissingFromPython(final boolean convertMissingFromPython) {
       m_convertMissingFromPython = convertMissingFromPython;
   }

   /**
    * @return The configured sentinel options to use (if applicable; see {@link #getConvertMissingToPython()} and
    *         {@link #getConvertMissingFromPython()}).
    */
   public SentinelOption getSentinelOption() {
       return m_sentinelOption;
   }

   /**
    * @param sentinelOption The configured sentinel options to use (if applicable; see
    *            {@link #getConvertMissingToPython()} and {@link #getConvertMissingFromPython()}).
    */
   public void setSentinelOption(final SentinelOption sentinelOption) {
       m_sentinelOption = sentinelOption;
   }

   /**
    * @return The configured sentinel value to use (only used if {@link #getSentinelOption()} is
    *         {@link SentinelOption#CUSTOM}).
    */
   public int getSentinelValue() {
       return m_sentinelValue;
   }

   /**
    * @param sentinelValue The configured sentinel value to use (only used if {@link #getSentinelOption()} is
    *            {@link SentinelOption#CUSTOM}).
    */
   public void setSentinelValue(final int sentinelValue) {
       m_sentinelValue = sentinelValue;
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
		saveToInDialog(settings);
	}

	/**
	 * Save configuration to the given node settings.
	 * 
	 * @param settings
	 *            The settings to save to
	 */
	public void saveToInDialog(NodeSettingsWO settings) {
        settings.addString(CFG_PYTHON_VERSION_OPTION, getPythonVersion().getId());
        settings.addString(CFG_PYTHON2COMMAND, commandToString(getPython2Command()));
        settings.addString(CFG_PYTHON3COMMAND, commandToString(getPython3Command()));
        settings.addInt(CFG_CHUNK_SIZE, getChunkSize());
        settings.addBoolean(CFG_CONVERT_MISSING_TO_PYTHON, getConvertMissingToPython());
        settings.addBoolean(CFG_CONVERT_MISSING_FROM_PYTHON, getConvertMissingFromPython());
        settings.addString(CFG_SENTINEL_OPTION, getSentinelOption().name());
        settings.addInt(CFG_SENTINEL_VALUE, getSentinelValue());
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
		loadFromInDialog(settings);
	}

	/**
	 * Load configuration from the given node settings (using defaults if
	 * necessary).
	 *
	 * @param settings
	 *            The settings to load from
	 */
	public void loadFromInDialog(final NodeSettingsRO settings) {
		final String pythonVersionString = settings.getString(CFG_PYTHON_VERSION_OPTION, getPythonVersion().getId());
        // Backward compatibility: old saved versions may be all upper case.
        setPythonVersion(PythonVersion.fromId(pythonVersionString.toLowerCase(Locale.ROOT)));
        final String python2CommandString =
            settings.getString(CFG_PYTHON2COMMAND, commandToString(getPython2Command()));
        setPython2Command(commandFromString(python2CommandString));
        final String python3CommandString =
            settings.getString(CFG_PYTHON3COMMAND, commandToString(getPython3Command()));
        setPython3Command(commandFromString(python3CommandString));
        setChunkSize(settings.getInt(CFG_CHUNK_SIZE, getChunkSize()));
        setConvertMissingToPython(settings.getBoolean(CFG_CONVERT_MISSING_TO_PYTHON, getConvertMissingToPython()));
        setConvertMissingFromPython(
            settings.getBoolean(CFG_CONVERT_MISSING_FROM_PYTHON, getConvertMissingFromPython()));
        setSentinelOption(SentinelOption.valueOf(settings.getString(CFG_SENTINEL_OPTION, getSentinelOption().name())));
        setSentinelValue(settings.getInt(CFG_SENTINEL_VALUE, getSentinelValue()));
	}

    private static String commandToString(final PythonCommand command) {
        return command != null //
            ? command.toString() //
            : INDICATE_FALLBACK_TO_PREFERENCES_COMMAND_VALUE;
    }

    private static PythonCommand commandFromString(final String commandString) {
        return Objects.equals(commandString, INDICATE_FALLBACK_TO_PREFERENCES_COMMAND_VALUE) //
            ? null //
            // TODO: This only works for ordinary paths ("manual configuration"), not for Conda directory + environment
            // name ("Conda configuration").
            : new DefaultPythonCommand(commandString);
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
     * Creates and returns a new {@link PythonKernelOptions} instance from the values stored in this config.
     *
     * @return The Python kernel options.
     */
    public PythonKernelOptions getKernelOptions() {
        final SerializationOptions serializationOptions = new SerializationOptions(getChunkSize(),
            getConvertMissingToPython(), getConvertMissingFromPython(), getSentinelOption(), getSentinelValue());
		PythonKernelOptions opt = new PythonKernelOptions(getPythonVersion(), getPython2Command(), getPython3Command(),
            serializationOptions);
		return opt.forAddedAdditionalRequiredModules(additionalRequiredModules).forExternalCustomPath(externalCustomPath);
    }

	/**
	 * Add an additional required module. A check for that module is performed
	 * on node execution.
	 *
	 * @param name
	 */
	public void addRequiredModule(String name) {
		addRequiredModule(new PythonModuleSpec(name));
	}

	/**
	 * Add an additional required module. A check for that module is performed
	 * on node execution.
	 * @param spec 
	 */
	public void addRequiredModule(PythonModuleSpec spec) {
		additionalRequiredModules.add(spec);
	}
	
	/**
	 * 
	 * @param externalCustomPath The external custom path to set. It will be appended to the Python kernel's PYTHONPATH.
	 */
	public void setExternalCustomPath(String externalCustomPath) {
		this.externalCustomPath = externalCustomPath;
	}
}
