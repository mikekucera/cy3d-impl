package org.baderlab.cy3d.internal.command;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class GetDefaultRendererCommandTaskFactory extends AbstractTaskFactory {

	private final CyApplicationManager applicationManager;
	
	public GetDefaultRendererCommandTaskFactory(CyApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new GetDefaultRendererCommandTask(applicationManager));
	}

}
