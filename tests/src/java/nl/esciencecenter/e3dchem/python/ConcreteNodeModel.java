package nl.esciencecenter.e3dchem.python;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.port.PortType;

public class ConcreteNodeModel extends PythonWrapperNodeModel<ConcreteNodeConfig>{

	public ConcreteNodeModel(PortType[] inPortTypes, PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
		python_code_filename = "concrete.py";
	}

	public ConcreteNodeModel() {
		this(new PortType[] { BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });
	};
	
	@Override
	protected ConcreteNodeConfig createConfig() {
		return new ConcreteNodeConfig();
	}

}
