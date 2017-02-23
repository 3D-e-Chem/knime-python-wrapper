package nl.esciencecenter.e3dchem.python;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.knime.core.node.workflow.FlowVariable;

public class PythonWrapperNodeConfigTest {
	private ConcreteNodeConfig config;

	@Before
	public void setUp() {
		config = new ConcreteNodeConfig();
	}

	@Test
	public void testGetOptionsName() {
		assertEquals("options", config.getOptionsName());
	}

	@Test
	public void testGetOptionsValues() {
		Set<FlowVariable> result = config.getOptionsValues();

		Set<FlowVariable> expected = new HashSet<FlowVariable>();
		expected.add(new FlowVariable("column_name", "column1"));
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
}
