package nl.esciencecenter.e3dchem.python;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.python2.PythonCommand;
import org.knime.python2.PythonVersion;
import org.knime.python2.config.PythonSourceCodeOptionsPanel.EnforcePythonVersion;
import org.knime.python2.extensions.serializationlibrary.SentinelOption;
import org.knime.python2.extensions.serializationlibrary.SerializationOptions;

/**
 * Panel containing Python options
 * 
 * Copy of org.knime.python2.config.PythonSourceCodeOptionsPanel minus the
 * SourceCode
 * 
 */
public class PythonOptionsPanel<Config extends PythonWrapperNodeConfig> extends JPanel {
	private static final long serialVersionUID = -1088132215218301785L;
    private final EnforcePythonVersion m_enforcedVersion;

    private JPanel m_versionPanel;

    private JRadioButton m_python2;

    private JRadioButton m_python3;

    private JCheckBox m_convertToPython;

    private JCheckBox m_convertFromPython;

    private JRadioButton m_minVal;

    private JRadioButton m_maxVal;

    private JRadioButton m_sentinelOptionUserInput;

    private JTextField m_sentinelInput;

    private JLabel m_missingWarningLabel;

    private int m_sentinelValue;

    private JSpinner m_chunkSize;

    private PythonCommand m_python2Command;

    private PythonCommand m_python3Command;

	public PythonOptionsPanel(final EnforcePythonVersion version) {
        final ButtonGroup pythonVersion = new ButtonGroup();
        m_python2 = new JRadioButton("Python 2");
        m_python3 = new JRadioButton("Python 3");
        pythonVersion.add(m_python2);
        pythonVersion.add(m_python3);
		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		m_enforcedVersion = version;
		if (m_enforcedVersion != EnforcePythonVersion.NONE) {
			m_versionPanel.setVisible(false);
		}
		final JPanel panel = new JPanel(new GridBagLayout());
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		m_versionPanel = new JPanel(new FlowLayout());
        m_versionPanel.setBorder(BorderFactory.createTitledBorder("Use Python Version"));
        m_versionPanel.add(m_python2);
        m_versionPanel.add(m_python3);
        panel.add(m_versionPanel, gbc);
        // Missing value handling for int and long.
        final JPanel missingPanel = new JPanel(new GridLayout(0, 1));
        missingPanel.setBorder(BorderFactory.createTitledBorder("Missing Values (Int, Long)"));
        m_convertToPython = new JCheckBox("convert missing values to sentinel value (to python)");
        missingPanel.add(m_convertToPython);
        m_convertFromPython = new JCheckBox("convert sentinel values to missing value (from python)");
        missingPanel.add(m_convertFromPython);
        final JPanel sentinelPanel = new JPanel(new FlowLayout());
        final JLabel sentinelLabel = new JLabel("Sentinel value: ");
        final ButtonGroup sentinelValueGroup = new ButtonGroup();
        m_minVal = new JRadioButton("MIN_VAL");
        m_maxVal = new JRadioButton("MAX_VAL");
        m_sentinelOptionUserInput = new JRadioButton("");
        sentinelValueGroup.add(m_minVal);
        sentinelValueGroup.add(m_maxVal);
        sentinelValueGroup.add(m_sentinelOptionUserInput);
        m_minVal.setSelected(true);
        // TODO: Enable only if radio button is enabled.
        m_sentinelValue = 0;
        m_sentinelInput = new JTextField("0");
        m_sentinelInput.setPreferredSize(new Dimension(70, m_sentinelInput.getPreferredSize().height));
        m_sentinelInput.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(final DocumentEvent e) {
                updateSentinelValue();
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                removeUpdate(e);
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                // Does not get fired.
            }
        });
        sentinelPanel.add(sentinelLabel);
        sentinelPanel.add(m_minVal);
        sentinelPanel.add(m_maxVal);
        sentinelPanel.add(m_sentinelOptionUserInput);
        sentinelPanel.add(m_sentinelInput);
        missingPanel.add(sentinelPanel);
        m_missingWarningLabel = new JLabel("");
        missingPanel.add(m_missingWarningLabel);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(missingPanel, gbc);
        // Give user control over the number of rows to transfer per chunk.
        final JPanel chunkingPanel = new JPanel(new FlowLayout());
        chunkingPanel.setBorder(BorderFactory.createTitledBorder("Chunking"));
        chunkingPanel.add(new JLabel("Rows per chunk: "));
        m_chunkSize =
            new JSpinner(new SpinnerNumberModel(SerializationOptions.DEFAULT_CHUNK_SIZE, 1, Integer.MAX_VALUE, 1));
        chunkingPanel.add(m_chunkSize);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(chunkingPanel, gbc);
		add(panel);
	}

	public PythonOptionsPanel() {
		this(EnforcePythonVersion.NONE);
	}

	/**
	 * Save current settings into the given config.
	 *
	 * @param config
	 *            The config
	 */
	public void saveSettingsTo(final Config config) {
        config.setPythonVersion(getSelectedPythonVersion());
        config.setConvertMissingToPython(m_convertToPython.isSelected());
        config.setConvertMissingFromPython(m_convertFromPython.isSelected());
        config.setSentinelOption(getSelectedSentinelOption());
        config.setSentinelValue(m_sentinelValue);
        config.setChunkSize(((Integer)m_chunkSize.getValue()).intValue());
        config.setPython2Command(m_python2Command);
        config.setPython3Command(m_python3Command);
	}

	/**
	 * Load settings from the given config.
	 *
	 * @param config
	 *            The config
	 */
	public void loadSettingsFrom(final Config config) {
        if (config.getPythonVersion() == PythonVersion.PYTHON3) {
            m_python3.setSelected(true);
        } else {
            m_python2.setSelected(true);
        }
        // Missing value handling.
        m_convertToPython.setSelected(config.getConvertMissingToPython());
        m_convertFromPython.setSelected(config.getConvertMissingFromPython());
        if (config.getSentinelOption() == SentinelOption.MIN_VAL) {
            m_minVal.setSelected(true);
        } else if (config.getSentinelOption() == SentinelOption.MAX_VAL) {
            m_maxVal.setSelected(true);
        } else {
            m_sentinelOptionUserInput.setSelected(true);
        }
        m_sentinelInput.setText(config.getSentinelValue() + "");
        m_sentinelValue = config.getSentinelValue();
        m_chunkSize.setValue(config.getChunkSize());
        m_python2Command = config.getPython2Command();
        m_python3Command = config.getPython3Command();
	}

	/**
	 * Read the sentinel value from its input component. Show warning if it
	 * cannot be parsed.
	 */
	private void updateSentinelValue() {
		try {
			m_sentinelValue = Integer.parseInt(m_sentinelInput.getText());
			m_missingWarningLabel.setText("");
		} catch (final NumberFormatException ex) {
			m_sentinelValue = 0;
			m_missingWarningLabel.setText(
					"<html><font color=\"red\"><b>Sentinel value cannot be parsed. <br /> Default value 0 is used instead!</b></font></html>");
		}
	}

	/**
	 * @return the {@link SentinelOption} associated with the current radio
	 *         button selection
	 */

	private SentinelOption getSelectedSentinelOption() {
		SentinelOption so = SentinelOption.MIN_VAL;
        if (m_minVal.isSelected()) {
            so = SentinelOption.MIN_VAL;
        } else if (m_maxVal.isSelected()) {
            so = SentinelOption.MAX_VAL;
        } else if (m_sentinelOptionUserInput.isSelected()) {
            so = SentinelOption.CUSTOM;
        }
        return so;
	}

	/**
	 * Get the python version to use based on the EnforePythonVersion option or
	 * the user selection.
	 * 
	 * @return the {@link PythonVersion} associated with the current
	 *         EnforePythonVersion or radio button selection
	 */
	private PythonVersion getSelectedPythonVersion() {
		if (m_enforcedVersion == EnforcePythonVersion.PYTHON2) {
            return PythonVersion.PYTHON2;
        } else if (m_enforcedVersion == EnforcePythonVersion.PYTHON3) {
            return PythonVersion.PYTHON3;
        } else {
            if (m_python2.isSelected()) {
                return PythonVersion.PYTHON2;
            } else {
                return PythonVersion.PYTHON3;
            }
        }
	}
}