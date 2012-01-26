package org.cytoscape.paperwing.internal.input;

import java.awt.event.KeyEvent;
import java.util.Set;

import org.cytoscape.paperwing.internal.data.GraphicsData;
import org.cytoscape.paperwing.internal.data.GraphicsSelectionData;
import org.cytoscape.paperwing.internal.geometric.Vector3;
import org.cytoscape.paperwing.internal.tools.NetworkToolkit;
import org.cytoscape.paperwing.internal.tools.SimpleCamera;
import org.cytoscape.view.model.CyNetworkView;

public class CameraInputHandler implements InputHandler {

	@Override
	public void processInput(KeyboardMonitor keys, MouseMonitor mouse,
			GraphicsData graphicsData) {
		
		Set<Integer> held = keys.getHeld();
		SimpleCamera camera = graphicsData.getCamera();
		
		processCameraTranslation(held, camera);
		processCameraRoll(held, camera);
		processCameraRotation(held, camera);
		
		processCameraFirstPersonLook(keys, mouse, camera);
		processCameraZoom(mouse, graphicsData);
	}
	
	private void processCameraZoom(MouseMonitor mouse, GraphicsData graphicsData) {
		SimpleCamera camera = graphicsData.getCamera();
		Set<Integer> selectedNodeIndices = graphicsData.getSelectionData().getSelectedNodeIndices();
		GraphicsSelectionData selectionData = graphicsData.getSelectionData();
		CyNetworkView networkView = graphicsData.getNetworkView();
		
		// Varying distance between camera and camera's target point
		if (mouse.dWheel() != 0) {
			camera.zoomOut((double) mouse.dWheel());
			
			if (!selectedNodeIndices.isEmpty()) {
				// TODO: Check if this is a suitable place to put this, as
				// it helps to make node dragging smoother
				Vector3 averagePosition = NetworkToolkit.findCenter(selectedNodeIndices, networkView, graphicsData.getDistanceScale());
				selectionData.setSelectProjectionDistance(averagePosition.distance(camera.getPosition()));
			}
		}
	}

	private void processCameraFirstPersonLook(KeyboardMonitor keys,
			MouseMonitor mouse, SimpleCamera camera) {
		
		if (mouse.hasMoved()) {

			// First-person camera rotation
			if (keys.getHeld().contains(KeyEvent.VK_ALT)) {
				camera.turnRight(mouse.dX());
				camera.turnDown(mouse.dY());
			}
		}
	}

	private void processCameraTranslation(Set<Integer> held, SimpleCamera camera) {
		if (held.contains(KeyEvent.VK_W)) {
			camera.moveUp();
		}

		if (held.contains(KeyEvent.VK_S)) {
			camera.moveDown();
		}

		if (held.contains(KeyEvent.VK_A)) {
			camera.moveLeft();
		}

		if (held.contains(KeyEvent.VK_D)) {
			camera.moveRight();
		}

		if (held.contains(KeyEvent.VK_Q)) {
			camera.moveBackward();
		}

		if (held.contains(KeyEvent.VK_E)) {
			camera.moveForward();
		}
	}
	
	private void processCameraRotation(Set<Integer> held, SimpleCamera camera) {
		
		// If shift is pressed, perform orbit camera movement
		if (held.contains(KeyEvent.VK_SHIFT)) {

			if (held.contains(KeyEvent.VK_LEFT)) {
				camera.orbitLeft();
			}

			if (held.contains(KeyEvent.VK_RIGHT)) {
				camera.orbitRight();
			}

			if (held.contains(KeyEvent.VK_UP)) {
				camera.orbitUp();
			}

			if (held.contains(KeyEvent.VK_DOWN)) {
				camera.orbitDown();
			}

			// Otherwise, turn camera in a first-person like fashion
		} else {

			if (held.contains(KeyEvent.VK_LEFT)) {
				camera.turnLeft(4);
			}

			if (held.contains(KeyEvent.VK_RIGHT)) {
				camera.turnRight(4);
			}

			if (held.contains(KeyEvent.VK_UP)) {
				camera.turnUp(4);
			}

			if (held.contains(KeyEvent.VK_DOWN)) {
				camera.turnDown(4);
			}
		}
	}
	
	private void processCameraRoll(Set<Integer> held, SimpleCamera camera) {
		
		// Roll camera clockwise
		if (held.contains(KeyEvent.VK_X)) {
			camera.rollClockwise();
		}
	
		// Roll camera counter-clockwise
		if (held.contains(KeyEvent.VK_Z)) {
			camera.rollCounterClockwise();
		}
	}
}
