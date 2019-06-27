package nl.esciencecenter.e3dchem.python;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeModelWarningListener;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.python2.kernel.PythonCancelable;
import org.knime.python2.kernel.PythonExecutionMonitorCancelable;
import org.knime.python2.kernel.PythonKernel;

public class PythonWrapperNodeModelTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@BeforeClass
	public static void setUp() throws MalformedURLException, IOException {
		PythonWrapperTestUtils.materializeKNIMEPythonUtils();
	}

	public class JsonNodeModel extends PythonWrapperNodeModel<PythonWrapperNodeConfig> {
		public JsonNodeModel() {
			super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });
			python_code_filename = "json_test.py";
		}

		@Override
		protected PythonWrapperNodeConfig createConfig() {
			PythonWrapperNodeConfig config = new PythonWrapperNodeConfig();
			config.addRequiredModule("json");
			return config;
		}
	}

	public class FoobarNodeModel extends PythonWrapperNodeModel<PythonWrapperNodeConfig> {

		public FoobarNodeModel() {
			super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });
			python_code_filename = "foobar_test.py";
		}

		@Override
		protected PythonWrapperNodeConfig createConfig() {
			PythonWrapperNodeConfig config = new PythonWrapperNodeConfig();
			config.addRequiredModule("foobar");
			return config;
		}

	}

	public class NoReqsNodeModel extends PythonWrapperNodeModel<PythonWrapperNodeConfig> {
		public NoReqsNodeModel() {
			super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });
			python_code_filename = "noreqs_test.py";
		}

		@Override
		protected PythonWrapperNodeConfig createConfig() {
			return new PythonWrapperNodeConfig();
		}
	}

	@Test
	public void getPythonCode() throws IOException {
		ConcreteNodeModel model = new ConcreteNodeModel();

		String code = model.getPythonCode();
		Assert.assertThat(code, containsString("options["));
		Assert.assertThat(code, containsString("input_table["));
		Assert.assertThat(code, containsString("output_table ="));
	}

	@Test
	public void getPythonCode_missingpyfile() throws IOException {
		FoobarNodeModel model = new FoobarNodeModel();
		thrown.expect(FileNotFoundException.class);
		thrown.expectMessage("foobar_test.py");

		model.getPythonCode();
	}

	@Test
	public void testExecuteKernel() throws Exception {
		ConcreteNodeModel model = new ConcreteNodeModel();
		BufferedDataTable[] inData = { mock(BufferedDataTable.class) };
		ExecutionContext exec = mock(ExecutionContext.class);
		PythonKernel kernel = mock(PythonKernel.class);
		String[] externalOutput = { "stdout", "stderr" };
		when(kernel.execute(anyString(), any(PythonExecutionMonitorCancelable.class))).thenReturn(externalOutput);
		ExecutionMonitor monitor = mock(ExecutionMonitor.class);
		when(exec.createSubProgress(anyDouble())).thenReturn(monitor);

		model.executeKernel(inData, exec, kernel);

		verify(kernel).putFlowVariables(eq("flow_variables"), any());
		Set<FlowVariable> options = new HashSet<FlowVariable>();
		verify(kernel).putFlowVariables(eq("options"), eq(options));
		verify(kernel, times(1)).putDataTable(eq("input_table"), eq(inData[0]), eq(monitor));
		verify(kernel).execute(anyString(), any(PythonExecutionMonitorCancelable.class));
		verify(kernel, times(1)).getDataTable(eq("output_table"), eq(exec), eq(monitor));
	}

	@Test
	public void testExecuteKernel__warningMessageSet() throws Exception {
		// Mimic when in Python following line is present
		// flow_variables['warning_message'] = "Some warning"
		Collection<FlowVariable> variables = new HashSet<FlowVariable>();
		variables.add(new FlowVariable("warning_message", "some warning"));

		ConcreteNodeModel model = new ConcreteNodeModel();
		BufferedDataTable[] inData = { mock(BufferedDataTable.class) };
		ExecutionContext exec = mock(ExecutionContext.class);
		PythonKernel kernel = mock(PythonKernel.class);
		String[] externalOutput = { "stdout", "stderr" };
		when(kernel.execute(anyString(), any(PythonExecutionMonitorCancelable.class))).thenReturn(externalOutput);
		ExecutionMonitor monitor = mock(ExecutionMonitor.class);
		when(exec.createSubProgress(anyDouble())).thenReturn(monitor);
		when(kernel.getFlowVariables(eq("flow_variables"))).thenReturn(variables);
		NodeModelWarningListener listener = mock(NodeModelWarningListener.class);
		model.addWarningListener(listener);

		model.executeKernel(inData, exec, kernel);

		verify(listener, times(1)).warningChanged(eq("some warning"));
		assertEquals(variables, model.getVariables());
	}

	@Test
	public void testGetPythonCode() throws FileNotFoundException {
		ConcreteNodeModel model = new ConcreteNodeModel();
		String code = model.getPythonCode();

		assertTrue(code.contains("input_table"));
	}

	@Test(expected = FileNotFoundException.class)
	public void testGetPythonCode_missingFile() throws FileNotFoundException {
		JsonNodeModel model = new JsonNodeModel();
		model.getPythonCode();
	}
}
