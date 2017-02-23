package nl.esciencecenter.e3dchem.python;

import java.util.Collection;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.FlowVariable;

public class ConcreteNodeModel extends PythonWrapperNodeModel<ConcreteNodeConfig> {
	public Collection<FlowVariable> variables;

	public ConcreteNodeModel(PortType[] inPortTypes, PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
		python_code_filename = "concrete.py";

	}

	public ConcreteNodeModel() {
		this(new PortType[] { BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });
	};

	@Override
	protected void addNewVariables(Collection<FlowVariable> newVariables) {
		// unable to use super in tests,
		// because private data structures of superclass can not be setup
		// so override it
		this.variables = newVariables;
	}

	@Override
	protected ConcreteNodeConfig createConfig() {
		return new ConcreteNodeConfig();
	}
}
