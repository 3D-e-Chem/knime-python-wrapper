package nl.esciencecenter.e3dchem.python;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortType;

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

    @Test
    public void testValidPython_JsonPackage_PackageInstalled() throws InvalidSettingsException {
        JsonNodeModel model = new JsonNodeModel();

        model.validPython();
    }

    @Test
    public void testValidPython_FoobarPackage_PackageMissing() throws InvalidSettingsException {
        thrown.expect(InvalidSettingsException.class);
        thrown.expectMessage("Missing 'foobar' Python package(s), please install or correct Python executable");
        FoobarNodeModel model = new FoobarNodeModel();

        model.validPython();
    }

    // TODO test execute() method, but hard because it 
    // - requires complex input arguments
    // - it is protected and test cant access itbecause its run as a eclipse plugin
}
