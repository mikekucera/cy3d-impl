package org.baderlab.cy3d.internal.cytoscape.view;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.view.model.VisualProperty;

public class Cy3DEdgeView extends VisualPropertyKeeper<CyEdge> {

	private CyEdge edge;
	private Long suid;
	private DefaultValueVault defaultValueVault;
	
	public Cy3DEdgeView(DefaultValueVault defaultValueVault, CyEdge edge) {
		this.edge = edge;
		this.suid = SUIDFactory.getNextSUID();	
		this.defaultValueVault = defaultValueVault;
	}
	
	@Override
	public Long getSUID() {
		return suid;
	}

	@Override
	public CyEdge getModel() {
		return edge;
	}
	
	@Override
	public <T> T getVisualProperty(VisualProperty<T> visualProperty) {
		T value = super.getVisualProperty(visualProperty);
		
		if (value != null) {
			// If we were given an explicit value, return it
			return value;
		} else {
			// Otherwise, return the default value
			return defaultValueVault.getDefaultValue(visualProperty);
		}
	}

	
}