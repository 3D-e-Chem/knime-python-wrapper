package nl.esciencecenter.e3dchem.python;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModelWarningListener;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.python2.extensions.serializationlibrary.SentinelOption;
import org.knime.python2.kernel.PythonKernel;
import org.knime.python2.kernel.PythonKernelOptions.PythonVersionOption;

public class PythonWrapperNodeModelTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public class JsonNodeModel extends PythonWrapperNodeModel<ConcreteNodeConfig> {

		public JsonNodeModel() {
			super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });
			python_code_filename = "json_test.py";
			required_python_packages = Arrays.asList("json");
		}

		@Override
		protected ConcreteNodeConfig createConfig() {
			return new ConcreteNodeConfig();
		}

	}

	public class FoobarNodeModel extends PythonWrapperNodeModel<ConcreteNodeConfig> {

		public FoobarNodeModel() {
			super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });
			python_code_filename = "foobar_test.py";
			required_python_packages = Arrays.asList("foobar");
		}

		@Override
		protected ConcreteNodeConfig createConfig() {
			return new ConcreteNodeConfig();
		}

	}

	public class NoReqsNodeModel extends PythonWrapperNodeModel<ConcreteNodeConfig> {

		public NoReqsNodeModel() {
			super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });
			python_code_filename = "noreqs_test.py";
			required_python_packages = new ArrayList<String>();
		}

		@Override
		protected ConcreteNodeConfig createConfig() {
			return new ConcreteNodeConfig();
		}

	}

	public class NoReqsNodeModelPy3 extends NoReqsNodeModel {
		@Override
		protected ConcreteNodeConfig createConfig() {
			ConcreteNodeConfig config = new ConcreteNodeConfig();
			config.setKernelOptions(PythonVersionOption.PYTHON3, false, false, SentinelOption.MAX_VAL, 0, 100);
			return config;
		}
	}

	@Test
	public void testValidPython_JsonPackage_PackageInstalled() throws InvalidSettingsException {
		JsonNodeModel model = new JsonNodeModel();

		model.validPython();
	}

	@Test
	public void testValidPython_FoobarPackage_PackageMissing() throws InvalidSettingsException {
		thrown.expect(InvalidSettingsException.class);
		thrown.expectMessage("foobar");
		FoobarNodeModel model = new FoobarNodeModel();

		model.validPython();
	}

	@Test
	public void testValidPython_NoPackageequired() throws InvalidSettingsException {
		NoReqsNodeModel model = new NoReqsNodeModel();

		model.validPython();
	}

	@Test
	public void testValidPython_Python3() throws InvalidSettingsException {
		Assume.assumeFalse(isWindows());
		NoReqsNodeModelPy3 model = new NoReqsNodeModelPy3();

		model.validPython();
	}

	private boolean isWindows() {
		return System.getProperty("os.name").contains("win");
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
		when(kernel.execute(anyString(), eq(exec))).thenReturn(externalOutput);
		ExecutionMonitor monitor = mock(ExecutionMonitor.class);
		when(exec.createSubProgress(anyDouble())).thenReturn(monitor);

		model.executeKernel(inData, exec, kernel);

		verify(kernel).putFlowVariables(eq("flow_variables"), any());
		Set<FlowVariable> options = new HashSet<FlowVariable>();
		options.add(new FlowVariable("column_name", "column1"));
		verify(kernel).putFlowVariables(eq("options"), eq(options));
		verify(kernel, times(1)).putDataTable(eq("input_table"), eq(inData[0]), eq(monitor));
		verify(kernel).execute(anyString(), eq(exec));
		verify(kernel, times(1)).getDataTable(eq("output_table"), eq(exec), eq(monitor));
	}

	@Test
	public void testExecuteKernel__warningMessageSet() throws Exception {
		ConcreteNodeModel model = new ConcreteNodeModel();
		BufferedDataTable[] inData = { mock(BufferedDataTable.class) };
		ExecutionContext exec = mock(ExecutionContext.class);
		PythonKernel kernel = mock(PythonKernel.class);
		String[] externalOutput = { "stdout", "stderr" };
		when(kernel.execute(anyString(), eq(exec))).thenReturn(externalOutput);
		ExecutionMonitor monitor = mock(ExecutionMonitor.class);
		when(exec.createSubProgress(anyDouble())).thenReturn(monitor);
		Collection<FlowVariable> variables = new HashSet<FlowVariable>();
		variables.add(new FlowVariable("warning_message", "some warning"));
		when(kernel.getFlowVariables(eq("flow_variables"))).thenReturn(variables);
		NodeModelWarningListener listener = mock(NodeModelWarningListener.class);
		model.addWarningListener(listener);

		model.executeKernel(inData, exec, kernel);

		verify(listener, times(1)).warningChanged(eq("some warning"));
		assertEquals(variables, model.getVariables());
	}
}
