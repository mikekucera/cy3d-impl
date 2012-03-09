package org.cytoscape.paperwing.internal.task;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.event.CyListener;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.work.TaskFactory;

/**
 * This class is responsible for keeping track of all the current view {@link TaskFactory} objects,
 * which are used in situations such as needing to execute a certain task via the right-click menu.
 */
public class TaskFactoryListener implements CyListener {
	private Map<NodeViewTaskFactory, Map<String, String>> nodeViewTaskFactories;
	private Map<EdgeViewTaskFactory, Map<String, String>> edgeViewTaskFactories;
	
	private Map<NetworkViewTaskFactory, Map<String, String>> networkViewTaskFactories;
	
	public TaskFactoryListener() {
		nodeViewTaskFactories = new HashMap<NodeViewTaskFactory, Map<String, String>>();
		edgeViewTaskFactories = new HashMap<EdgeViewTaskFactory, Map<String, String>>();
		networkViewTaskFactories = new HashMap<NetworkViewTaskFactory, Map<String, String>>();
	}
	
	public void addNodeViewTaskFactory(NodeViewTaskFactory taskFactory, Map<String, String> properties) {
		nodeViewTaskFactories.put(taskFactory, properties);
	}
	
	public void addEdgeViewTaskFactory(EdgeViewTaskFactory taskFactory, Map<String, String> properties) {
		edgeViewTaskFactories.put(taskFactory, properties);
	}
	
	public void addNetworkViewTaskFactory(NetworkViewTaskFactory taskFactory, Map<String, String> properties) {
		networkViewTaskFactories.put(taskFactory, properties);
	}
	
	public void removeNodeViewTaskFactory(NodeViewTaskFactory taskFactory, Map<String, String> properties) {
		nodeViewTaskFactories.put(taskFactory, properties);
	}
	
	public void removeEdgeViewTaskFactory(EdgeViewTaskFactory taskFactory, Map<String, String> properties) {
		edgeViewTaskFactories.put(taskFactory, properties);
	}
	
	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory taskFactory, Map<String, String> properties) {
		networkViewTaskFactories.put(taskFactory, properties);
	}
}
