package nl.esciencecenter.e3dchem.python;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Concrete implementation of PythonWrapperNodeConfig so it's non-abstract methods can be tested.
 */
public class ConcreteNodeConfig extends PythonWrapperNodeConfig {

    @Override
    public void saveTo(NodeSettingsWO settings) {

    }

    @Override
    public void loadFrom(NodeSettingsRO settings) throws InvalidSettingsException {
    }
}
