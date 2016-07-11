package nl.esciencecenter.e3dchem.python;

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
        assertEquals("options", PythonWrapperNodeConfig.getOptionsName());
    }

    @Test
    public void testGetOptionsValues() {
        Set<FlowVariable> result = config.getOptionsValues();

        Set<FlowVariable> expected = new HashSet<FlowVariable>();
        assertEquals(expected, result);
    }

}
