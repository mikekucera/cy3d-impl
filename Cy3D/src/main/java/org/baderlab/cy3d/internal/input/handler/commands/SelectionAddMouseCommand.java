package org.baderlab.cy3d.internal.input.handler.commands;

import org.baderlab.cy3d.internal.data.GraphicsData;
import org.baderlab.cy3d.internal.data.GraphicsSelectionData;
import org.baderlab.cy3d.internal.data.PickingData;
import org.baderlab.cy3d.internal.input.handler.MouseCommandAdapter;
import org.baderlab.cy3d.internal.tools.NetworkToolkit;
import org.cytoscape.view.model.CyNetworkView;

public class SelectionAddMouseCommand extends MouseCommandAdapter {

	private static int NO_INDEX = -1; 
	
	
	private final GraphicsData graphicsData;
	private final GraphicsSelectionData selectionData;
	
	
	public SelectionAddMouseCommand(GraphicsData graphicsData) {
		this.graphicsData = graphicsData;
		this.selectionData = graphicsData.getSelectionData();
	}
	
	
	@Override
	public void clicked(int x, int y) {
		CyNetworkView networkView = graphicsData.getNetworkSnapshot();
		long newHoverNodeIndex = graphicsData.getPickingData().getClosestPickedNodeIndex();
		long newHoverEdgeIndex = graphicsData.getPickingData().getClosestPickedEdgeIndex();

		selectionData.setHoverNodeIndex(newHoverNodeIndex);
		selectionData.setHoverEdgeIndex(newHoverEdgeIndex);
		
		if (!selectionData.isDragSelectMode()) {
			if (newHoverNodeIndex != NO_INDEX) {
				if (NetworkToolkit.checkNodeSelected(newHoverNodeIndex, networkView)) {
					// Deselect the node if it was already selected
					NetworkToolkit.setNodeSelected(newHoverNodeIndex, networkView, false);
				} else {
					// Select the node if it was not selected
					NetworkToolkit.setNodeSelected(newHoverNodeIndex, networkView, true);
				}
				
			} else if (newHoverEdgeIndex != NO_INDEX) {
				if (NetworkToolkit.checkEdgeSelected(newHoverEdgeIndex, networkView)) {
					// Deselect the edge if it was already selected
					NetworkToolkit.setEdgeSelected(newHoverEdgeIndex, networkView, false);
				} else {
					// Select the edge if it was not selected
					NetworkToolkit.setEdgeSelected(newHoverEdgeIndex, networkView, true);
				}
			}
		}
	}
	
	
	// Drag movement
	
	@Override
	public void dragStart(int x, int y) {
		selectionData.setSelectTopLeftX(x);
		selectionData.setSelectTopLeftY(y);
		selectionData.setSelectTopLeftFound(true);
	}

	@Override
	public void dragMove(int x, int y) {
		selectionData.setSelectBottomRightX(x);
		selectionData.setSelectBottomRightY(y);
		
		if (Math.abs(selectionData.getSelectTopLeftX() - x) >= 1 && Math.abs(selectionData.getSelectTopLeftY() - y) >= 1) {
			selectionData.setDragSelectMode(true);
		}
	}

	@Override
	public void dragEnd(int x, int y) {
		CyNetworkView networkView = graphicsData.getNetworkSnapshot();
		PickingData pickingData = graphicsData.getPickingData();
		
		selectionData.setDragSelectMode(false);
		selectionData.setSelectTopLeftFound(false);
		
		for (long index : pickingData.getPickedNodeIndices()) {
			NetworkToolkit.setNodeSelected(index, networkView, true);
		}
		for (long index : pickingData.getPickedEdgeIndices()) {
			NetworkToolkit.setEdgeSelected(index, networkView, true);
		}
		
		pickingData.getPickedNodeIndices().clear();
		pickingData.getPickedEdgeIndices().clear();
	}
	
	
	@Override
	public void moved(int x, int y) {
		// MKTODO I think this returns the picked node index computed from the last frame.
		long newHoverNodeIndex = graphicsData.getPickingData().getClosestPickedNodeIndex();
		long newHoverEdgeIndex = graphicsData.getPickingData().getClosestPickedEdgeIndex();
		
		selectionData.setHoverNodeIndex(newHoverNodeIndex);
		selectionData.setHoverEdgeIndex(newHoverEdgeIndex);
	}

	@Override
	public void exited() {
		selectionData.setHoverNodeIndex(NO_INDEX);
		selectionData.setHoverEdgeIndex(NO_INDEX);
	}
}
