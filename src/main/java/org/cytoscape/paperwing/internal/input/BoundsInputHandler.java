package org.cytoscape.paperwing.internal.input;

import org.cytoscape.paperwing.internal.data.CoordinatorData;
import org.cytoscape.paperwing.internal.data.GraphicsData;
import org.cytoscape.paperwing.internal.geometric.Vector3;
import org.cytoscape.paperwing.internal.utility.GraphicsUtility;

import com.jogamp.newt.event.MouseEvent;

public class BoundsInputHandler implements InputHandler {

	@Override
	public void processInput(KeyboardMonitor keys, MouseMonitor mouse,
			GraphicsData graphicsData) {
		// TODO Auto-generated method stub
		processMoveBounds(mouse, graphicsData);
	}
	
	private void processMoveBounds(MouseMonitor mouse, GraphicsData graphicsData) {
		CoordinatorData coordinatorData = graphicsData.getCoordinatorData();
		
		if (mouse.getPressed().contains(MouseEvent.BUTTON1)) {
			Vector3 mousePosition = GraphicsUtility.convertMouseTo3d(mouse, graphicsData, graphicsData.getCamera().getDistance());
			
			coordinatorData.getBounds().moveTo(mousePosition);
		}
	}
}