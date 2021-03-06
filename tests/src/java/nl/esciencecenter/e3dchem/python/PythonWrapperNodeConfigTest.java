package nl.esciencecenter.e3dchem.python;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.python2.PythonModuleSpec;
import org.knime.python2.kernel.PythonKernelOptions;

public class PythonWrapperNodeConfigTest {
	private PythonWrapperNodeConfig config;

	@Before
	public void setUp() {
		config = new PythonWrapperNodeConfig();
	}

	@Test
	public void testGetOptionsName() {
		assertEquals("options", config.getOptionsName());
	}

	@Test
	public void testGetOptionsValues() {
		Set<FlowVariable> result = config.getOptionsValues();

		Set<FlowVariable> expected = new HashSet<FlowVariable>();
		assertEquals(expected, result);
	}

	@Test
	public void testInputTables() {
		assertArrayEquals(new String[] { "input_table" }, config.getVariableNames().getInputTables());
	}

	@Test
	public void testOutputTables() {
		assertArrayEquals(new String[] { "output_table" }, config.getVariableNames().getOutputTables());
	}

	@Test
	public void testCustomInputTables() {
		CustomTablesConfig config1 = new CustomTablesConfig();
		assertArrayEquals(new String[0], config1.getVariableNames().getInputTables());
	}

	@Test
	public void testCustomOutputTables() {
		CustomTablesConfig config1 = new CustomTablesConfig();
		assertArrayEquals(new String[] { "output_table1", "output_table2" },
				config1.getVariableNames().getOutputTables());
	}

	@Test
	public void testSaveTo() throws InvalidSettingsException {
		NodeSettings settings = new NodeSettings("python-wrapper");

		config.saveTo(settings);

		assertEquals(500000, settings.getInt("chunkSize"));
	}

	@Test
	public void testLoadFrom() throws InvalidSettingsException {
		NodeSettings settings = new NodeSettings("python-wrapper");
		settings.addInt("chunkSize", 42);

		config.loadFrom(settings);

		assertEquals(42, config.getChunkSize());
	}

	@Test
	public void testLoadFromInDialog() throws InvalidSettingsException {
		NodeSettings settings = new NodeSettings("python-wrapper");
		settings.addInt("chunkSize", 42);

		config.loadFromInDialog(settings);

		assertEquals(42, config.getChunkSize());
	}

	@Test
	public void testAddRequiredModule() {
		config.addRequiredModule("json");

		PythonKernelOptions kernelOptions = config.getKernelOptions();
		Set<PythonModuleSpec> additionalRequiredModules = kernelOptions.getAdditionalRequiredModules();
		Set<PythonModuleSpec> expected = Collections.singleton(new PythonModuleSpec("json"));
		assertEquals(expected, additionalRequiredModules);
	}
}
