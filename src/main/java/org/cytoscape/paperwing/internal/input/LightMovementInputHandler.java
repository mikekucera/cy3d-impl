package org.cytoscape.paperwing.internal.input;

import org.cytoscape.paperwing.internal.data.GraphicsData;
import org.cytoscape.paperwing.internal.data.GraphicsSelectionData;
import org.cytoscape.paperwing.internal.data.LightingData;
import org.cytoscape.paperwing.internal.geometric.Vector3;
import org.cytoscape.paperwing.internal.lighting.Light;
import org.cytoscape.paperwing.internal.tools.GeometryToolkit;
import org.cytoscape.paperwing.internal.tools.NetworkToolkit;
import org.cytoscape.paperwing.internal.tools.SimpleCamera;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;

/**
 * An {@link InputHandler} capable of handling input related to movement of the various scene lights.
 */
public class LightMovementInputHandler implements InputHandler {
	
	// TODO: Move to LightingData
	private Vector3 previousMouseProjection;
	private Vector3 currentMouseProjection;
	
	public LightMovementInputHandler() {
		previousMouseProjection = new Vector3();
		currentMouseProjection = new Vector3();
	}
	
	@Override
	public void processInput(KeyboardMonitor keys, MouseMonitor mouse,
			GraphicsData graphicsData) {
		
		processLightMovement(keys, mouse, graphicsData);
	}
	
	// Movement would be similar to dragging of nodes
	private void processLightMovement(KeyboardMonitor keys, 
			MouseMonitor mouse, GraphicsData graphicsData) {
		
		LightingData lightingData = graphicsData.getLightingData();
		SimpleCamera camera = graphicsData.getCamera();
		
		// Currently attempts to move only the light at index 0
		Light light = lightingData.getLight(0);
		
		float[] lightPosition = light.getPosition();
		Vector3 currentLightPosition = new Vector3(lightPosition[0], lightPosition[1], lightPosition[2]);
		
		currentLightPosition.divideLocal(lightPosition[3]); // Since lightPosition contains homogenous coordinates, perform division
		
		double mouseProjectionDistance = GeometryToolkit.findOrthogonalDistance(
				camera.getPosition(), currentLightPosition, camera.getDirection());
		
		// Capture mouse position
		if (mouse.getPressed().contains(MouseEvent.BUTTON3)) {
			
			currentMouseProjection.set(
					GeometryToolkit.convertMouseTo3d(mouse, graphicsData, mouseProjectionDistance));
		}
		
		// Capture new mouse position and use displacement to move lights
		if (mouse.hasMoved() 
				&& mouse.getHeld().contains(MouseEvent.BUTTON3)
				&& keys.getHeld().contains(KeyEvent.VK_CONTROL)) {
			
			previousMouseProjection.set(currentMouseProjection);
			currentMouseProjection.set(
					GeometryToolkit.convertMouseTo3d(mouse, graphicsData, mouseProjectionDistance));
			
			Vector3 displacement = currentMouseProjection.subtract(previousMouseProjection);
			
			Vector3 newLightPosition = currentLightPosition.plus(displacement);
			
			light.setPosition((float) newLightPosition.x(), 
					(float) newLightPosition.y(), 
					(float) newLightPosition.z(), 
					1.0f);
		}
	}
}