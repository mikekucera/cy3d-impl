package org.cytoscape.paperwing.internal.graphics;

import java.util.List;

import javax.media.opengl.GL2;

import org.cytoscape.paperwing.internal.SimpleCamera;
import org.cytoscape.paperwing.internal.Vector3;

public class RenderSelectionBoxProcedure implements ReadOnlyGraphicsProcedure {

	@Override
	public void initialize(GraphicsData graphicsData) {
		// TODO Auto-generated method stub

	}

	/**
	 * Draw the drag selection box
	 * 
	 * @param gl
	 *            The {@link GL2} rendering object
	 * @param drawDistance
	 *            The distance from the camera to draw the box
	 */
	@Override
	public void execute(GraphicsData graphicsData) {
		GL2 gl = graphicsData.getGlContext();
		
		if (!graphicsData.getSelectionData().isDragSelectMode()) {
			return;
		}

		int selectTopLeftX = graphicsData.getSelectionData()
				.getSelectTopLeftX();
		int selectTopLeftY = graphicsData.getSelectionData()
				.getSelectTopLeftY();
		int selectBottomRightX = graphicsData.getSelectionData()
				.getSelectBottomRightX();
		int selectBottomRightY = graphicsData.getSelectionData()
				.getSelectBottomRightY();

		int screenWidth = graphicsData.getScreenWidth();
		int screenHeight = graphicsData.getScreenHeight();
		SimpleCamera camera = graphicsData.getCamera();
		double drawDistance = graphicsData.getCamera().getDistance();

		Vector3 topLeft = GraphicsUtility.projectScreenCoordinates(
				selectTopLeftX, selectTopLeftY, screenWidth, screenHeight,
				drawDistance, camera);
		Vector3 bottomLeft = GraphicsUtility.projectScreenCoordinates(
				selectTopLeftX, selectBottomRightY, screenWidth, screenHeight,
				drawDistance, camera);

		Vector3 topRight = GraphicsUtility.projectScreenCoordinates(
				selectBottomRightX, selectTopLeftY, screenWidth, screenHeight,
				drawDistance, camera);
		Vector3 bottomRight = GraphicsUtility.projectScreenCoordinates(
				selectBottomRightX, selectBottomRightY, screenWidth,
				screenHeight, drawDistance, camera);

		/**
		 * // Below uses older cylinder approach drawSingleSelectEdge(gl,
		 * topLeft, topRight); drawSingleSelectEdge(gl, topLeft, bottomLeft);
		 * 
		 * drawSingleSelectEdge(gl, topRight, bottomRight);
		 * drawSingleSelectEdge(gl, bottomLeft, bottomRight);
		 **/

		gl.glDisable(GL2.GL_LIGHTING);
		gl.glColor3f(0.0f, 0.4f, 0.6f);

		// Below uses converted 3D coordinates
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glVertex3d(topLeft.x(), topLeft.y(), topLeft.z());
		gl.glVertex3d(bottomLeft.x(), bottomLeft.y(), bottomLeft.z());
		gl.glVertex3d(bottomRight.x(), bottomRight.y(), bottomRight.z());
		gl.glVertex3d(topRight.x(), topRight.y(), topRight.z());
		gl.glEnd();

		// Below uses raw 2d coordinates
		// gl.glBegin(GL2.GL_LINE_LOOP);
		// gl.glVertex2i(selectTopLeftX, selectTopLeftY);
		// gl.glVertex2i(selectTopLeftX, selectBottomRightY);
		// gl.glVertex2i(selectBottomRightX, selectBottomRightY);
		// gl.glVertex2i(selectBottomRightX, selectTopLeftY);
		// gl.glEnd();

		gl.glEnable(GL2.GL_LIGHTING);
	}

}
