// If you are visiting this class for the first time,
// consider taking a look at the following files:
//
// src/main/resources/controls.txt -- contains information about controls
// src/main/resources/overview-todo.txt -- contains information about what 
// is to be done

package org.cytoscape.paperwing.internal;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.presentation.property.RichVisualLexicon;

/** The main class for the Wind rendering engines responsible for
 * creating graphics with the use of the JOGL (Java OpenGL) library
 * 
 * @author Paperwing (Yue Dong)
 */
public class Graphics implements GLEventListener {

	/**
	 * This value controls distance scaling when converting from node
	 * coordinates to drawing coordinates
	 */
	private static final float DISTANCE_SCALE = 178.0f; 
	
	/** The radius to use for the drag selection border's straight segments */
	private static final float SELECT_BORDER_RADIUS = 0.0027f;
	
	/** The distance from the camera to draw the selection box */
	private static final double SELECT_BORDER_DISTANCE = 0.91;
	
	/** Controls the distance apart to draw the reticle for the mouse */
	private static final double RETICLE_DISTANCE = 0.06;
	
	/** Controls the radius of the reticle */
	private static final double RETICLE_RADIUS = 0.012;
	
	
	/** The display list index for the reticle */
	private int reticleListIndex;
	
	/** The display list index for the selection border segments */
	private int selectBorderListIndex;

	
	/** Number of frames elapsed */
	private int framesElapsed = 0;
	
	
	/** The height of the screen */
	private int screenHeight;
	
	/** The width of the screen */
	private int screenWidth;
	
	
	/** The NULL coordinate which means "no coordinate" */
	private static int NULL_COORDINATE = Integer.MIN_VALUE;
	
	
	/** A flag for whether drag selection mode is currently active */
	private boolean dragSelectMode;
	

	/** A constant that stands for "no index is here" */
	// TODO: NO_INDEX relies on cytoscape's guarantee that node and edge indices are nonnegative
	private static final int NO_INDEX = -1; // Value representing that no node or edge index is being held
	
	/** The index of the node currently being hovered over */
	private int hoverNodeIndex;
	
	/** The index of the edge currently being hovered over */
	private int hoverEdgeIndex;
	
	
	
	/** A draw state modifier which can be used to modify the appearance
	 * of certain objects
	 */
	public static enum DrawStateModifier {
	    HOVERED, SELECTED, NORMAL, ENLARGED, SELECT_BORDER
	}
	
	/** A constant that stands for "no type is here" */
	private static final int NO_TYPE = -1;
	
	/** A constant representing the type node */
	private static final int NODE_TYPE = 0;
	
	/** A constant representing the type edge */
	private static final int EDGE_TYPE = 1;
	
	
	
	/** A monitor to keep track of keyboard events */
	private KeyboardMonitor keys;
	
	/** A monitor to keep track of mouse events */
	private MouseMonitor mouse;
	
	/** The camera to use for transformation of 3D scene */
	private SimpleCamera camera;
	
	
	
	/** The application manager for the Cytoscape application */
	private CyApplicationManager applicationManager;
	
	/** The current Cytoscape network manager */
	private CyNetworkManager networkManager;
	
	/** A view manager for network views */
	private CyNetworkViewManager networkViewManager;
	
	/** A rendering engine manager */
	private RenderingEngineManager renderingEngineManager;
	
	
	/** The network view to be rendered */
	private CyNetworkView networkView;
	
	/** The visual lexicon to use */
	private VisualLexicon visualLexicon;
	
	/** A debug boolean */
	private boolean latch_1;
	
	/** A boolean to use lower quality 3D shapes to improve framerate */
	private boolean lowerQuality = false;
	
	/** A boolean to disable real-time shape picking to improve framerate */
	private boolean skipHover = false;
	
	/** A projection of the current mouse position into 3D coordinates to be used 
	 * for mouse drag movement of certain objects */
	private Vector3 currentSelectedProjection;
	
	/** A projection of the mouse position into 3D coordinates to be used 
	 * for mouse drag movement of certain objects */
	private Vector3 previousSelectedProjection;
	
	/** The distance from the projected point to the screen */
	private double selectProjectionDistance;
	
	
	private boolean mapMode = false;
	private Graphics mapPartner = null;
	
	private Container mapContainer = null;
	
	private static Map<CyNetworkView, List<Graphics>> registry = null;
	static {
		Graphics.registry = new HashMap<CyNetworkView, List<Graphics>>();
	}
	
	/** A class capable of storing the edge and node indices of edges and nodes
	 * that were found to be selected using the shape picking methods
	 */
	public class PickResults {
		public Set<Integer> nodeIndices = new LinkedHashSet<Integer>();
		public Set<Integer> edgeIndices = new LinkedHashSet<Integer>();
	}
	
	/** Initialize a singleton that seems to help with JOGL in some compatibility
	 * aspects
	 */
	public static void initSingleton() {
		GLProfile.initSingleton(false);
		//System.out.println("initSingleton called");
	}
	
	/** Create a new Graphics object
	 * 
	 * @param networkView The CyNetworkView object, representing the 
	 * View<CyNetwork> object that we are rendering
	 * @param visualLexicon The visual lexicon being used
	 */
	public Graphics(CyNetworkView networkView, VisualLexicon visualLexicon) {
		if (registry.get(networkView) == null) {
			List<Graphics> list = new LinkedList<Graphics>();
			list.add(this);
			
			registry.put(networkView, list);
		} else {
			registry.get(networkView).add(this);
		}
	
		keys = new KeyboardMonitor();
		mouse = new MouseMonitor();

		// TODO: add default constant speeds for camera movement
		camera = new SimpleCamera(new Vector3(0, 0, 2), new Vector3(0, 0, 0),
				new Vector3(0, 1, 0), 0.04, 0.0033, 0.01, 0.01, 0.4);
		
		this.networkView = networkView;
		this.visualLexicon = visualLexicon;
		
		selectedNodes = new LinkedHashSet<CyNode>();
		selectedEdges = new LinkedHashSet<CyEdge>();
		
		selectedNodeIndices = new HashSet<Integer>();
		selectedEdgeIndices = new HashSet<Integer>();

		hoverNodeIndex = NO_INDEX;
		hoverEdgeIndex = NO_INDEX;
		
		dragSelectMode = false;
	}
	
	public void setMapMode(boolean mapMode) {
		this.mapMode = mapMode;
	}
	
	private void setMapPartner(Graphics mapPartner) {
		this.mapPartner = mapPartner;
	}
	
	public void setMapContainer(Container container) {
		this.mapContainer = container;
	}
	
	public void findMapPartner() {
		List<Graphics> list = registry.get(networkView);
		
		if (list == null) {
			return;
		} else {
			for (Graphics g : list) {
				if (g.mapMode != this.mapMode && g.mapPartner == null && this.mapPartner == null) {
					g.setMapPartner(this);
					this.setMapPartner(g);
					
					System.out.println("Graphics with mapMode (" + mapMode + ") found partner. Link is + " + g + " and " + this);
					
					return;
				}
			}
		}
		
	}
	
	/** Attach the KeyboardMonitor and MouseMonitors, which are listeners,
	 * to the specified component for capturing keyboard and mouse events
	 * 
	 * @param component The component to listen to events for
	 */
	public void trackInput(Component component) {
		component.addMouseListener(mouse);
		component.addMouseMotionListener(mouse);
		component.addMouseWheelListener(mouse);
		component.addFocusListener(mouse);
		
		component.addKeyListener(keys);
		component.addFocusListener(keys);
	}
	
	/** Assign the values of certain managers that were imported as OSGi services
	 * 
	 * @param applicationManager The Cytoscape application's application manager
	 * @param networkManager The network manager
	 * @param networkViewManager The network view manager
	 * @param renderingEngineManager The rendering engine manager
	 */
	public void setManagers(CyApplicationManager applicationManager,
			CyNetworkManager networkManager,
			CyNetworkViewManager networkViewManager,
			RenderingEngineManager renderingEngineManager) {
		this.applicationManager = applicationManager;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.renderingEngineManager = renderingEngineManager;
	}

	/** Main drawing method; can be called by an {@link Animator} such as
	 * {@link FPSAnimator}, responsible for drawing the scene and advancing
	 * the frame
	 * 
	 * @param drawable The GLAutoDrawable object used for rendering
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		
		GL2 gl = drawable.getGL().getGL2();
		
		// Check input
		checkInput(gl);

		// Reset scene
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		Vector3 position = camera.getPosition();
		Vector3 target = camera.getTarget();
		Vector3 up = camera.getUp();

		
		GLU glu = new GLU();
		glu.gluLookAt(position.x(), position.y(), position.z(), target.x(),
				target.y(), target.z(), up.x(), up.y(), up.z());
		
		// Draw selection box
		// ------------------

		if (dragSelectMode) {
			
			drawSelectBox(gl, SELECT_BORDER_DISTANCE);
		}
		
		// Control light positioning
		// -------------------------
		
		float[] lightPosition = { -4.0f, 4.0f, 6.0f, 1.0f };
		
		// Code below toggles the light following the camera
		// gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION,
		// FloatBuffer.wrap(lightPosition));


		// Draw nodes and edges
		// --------------------
		
		drawNodes(gl);
		drawEdges(gl, DrawStateModifier.NORMAL);
		
		framesElapsed++;
		
		if (mapMode) {
			if (mapContainer != null) {
				mapContainer.repaint();
			}
		}
		
		// Draw the location rectangle on the map
		if (mapMode) {
			if (mapPartner != null) {
				SimpleCamera partnerCamera = mapPartner.camera;
				
				Vector3 topLeft = mapPartner.projectScreenCoordinates(0, 0, partnerCamera.getDistance());
				Vector3 bottomLeft = mapPartner.projectScreenCoordinates(0, mapPartner.screenHeight, partnerCamera.getDistance());
				
				Vector3 topRight = mapPartner.projectScreenCoordinates(mapPartner.screenWidth, 0, partnerCamera.getDistance());
				Vector3 bottomRight = mapPartner.projectScreenCoordinates(mapPartner.screenWidth, mapPartner.screenHeight, partnerCamera.getDistance());
				
				gl.glDisable(GL2.GL_LIGHTING);
				gl.glColor3f(0.7f, 0.7f, 0.6f);
				
				// Below uses converted 3D coordinates
				gl.glBegin(GL2.GL_LINE_LOOP);
				gl.glVertex3d(topLeft.x(), topLeft.y(), topLeft.z());
				gl.glVertex3d(bottomLeft.x(), bottomLeft.y(), bottomLeft.z());
				gl.glVertex3d(bottomRight.x(), bottomRight.y(), bottomRight.z());
				gl.glVertex3d(topRight.x(), topRight.y(), topRight.z());
				gl.glEnd();
				
				
				gl.glEnable(GL2.GL_LIGHTING);
			}
		}
		
	}
	
	/** Obtain input and check for changes in the keyboard and mouse buttons,
	 * as well as mouse movement. This method also handles responses
	 * to such events
	 * 
	 * @param gl The {@link GL2} object used for rendering
	 */
	private void checkInput(GL2 gl) {
		
		
	}
	
	/**
	 * Obtain the average position of a set of nodes, where each node has the same
	 * weight in the average
	 * 
	 * @param nodes The {@link Collection} of nodes
	 * @return The average position
	 */
	private Vector3 findAveragePosition(Collection<CyNode> nodes) {
		if (nodes.isEmpty()) {
			return null;
		}
		
		double x = 0;
		double y = 0;
		double z = 0;
		
		View<CyNode> nodeView;
		
		for (CyNode node : nodes) {
			// TODO: This relies on an efficient traversal of nodes, as well
			// as efficient retrieval from the networkView object
			nodeView = networkView.getNodeView(node);
			
			if (nodeView != null) {
				x += nodeView.getVisualProperty(RichVisualLexicon.NODE_X_LOCATION);
				y += nodeView.getVisualProperty(RichVisualLexicon.NODE_Y_LOCATION);
				z += nodeView.getVisualProperty(RichVisualLexicon.NODE_Z_LOCATION);
			} else {
				System.out.println("Node with no view found: " + node + 
						", index: " + node.getIndex());
			}
		}
		
		Vector3 result = new Vector3(x, y, z);
		result.divideLocal(DISTANCE_SCALE * nodes.size());
		
		return result;
	}
	

	/** Draw the drag selection box
	 * 
	 * @param gl The {@link GL2} rendering object
	 * @param drawDistance The distance from the camera to draw the box
	 */
	private void drawSelectBox(GL2 gl, double drawDistance) {
		
		
	}
	
	@Override
	public void dispose(GLAutoDrawable autoDrawable) {

	}

	/** Initialize the Graphics object, performing certain
	 * OpenGL initializations
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		initLighting(drawable);

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL.GL_DEPTH_TEST);

		gl.glDepthFunc(GL.GL_LEQUAL);
		// gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_FASTEST);
		// gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);

		gl.glViewport(0, 0, drawable.getWidth(), drawable.getHeight());

		//generateNodes();
		//generateEdges();
		startTime = System.nanoTime();
		createDisplayLists(gl);
		
		// Correct lightning for scaling certain models
		gl.glEnable(GL2.GL_NORMALIZE);
		
		// Enable blending
		// ---------------
		
		// gl.glEnable(GL2.GL_BLEND);
		// gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	}

	/** Create the display lists that are used to draw the edges and nodes,
	 * as well as certain other objects
	 * 
	 * @param gl {@link GL2} rendering object
	 */
	private void createDisplayLists(GL2 gl) {
		
		selectBorderListIndex = gl.glGenLists(1);
		
		GLU glu = new GLU();

		GLUquadric quadric = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
		glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
	}

	private void initLighting(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		float[] global = { 0.2f, 0.2f, 0.2f, 1.0f };

		gl.glEnable(GL2.GL_LIGHTING);
		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, FloatBuffer.wrap(global));
		gl.glShadeModel(GL2.GL_SMOOTH);

		float[] ambient = { 0.0f, 0.0f, 0.0f, 1.0f };
		float[] diffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] specular = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] position = { -4.0f, 4.0f, 6.0f, 1.0f };

		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, FloatBuffer.wrap(ambient));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, FloatBuffer.wrap(diffuse));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, FloatBuffer.wrap(specular));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, FloatBuffer.wrap(position));

		gl.glEnable(GL2.GL_LIGHT0);

		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
		
		// Older default values
		// float[] specularReflection = { 0.5f, 0.5f, 0.5f, 1.0f };
		// gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR,
		// FloatBuffer.wrap(specularReflection));
		// gl.glMateriali(GL2.GL_FRONT, GL2.GL_SHININESS, 40);
		
		float[] specularReflection = { 0.46f, 0.46f, 0.46f, 1.0f };
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR,
				FloatBuffer.wrap(specularReflection));
		gl.glMateriali(GL2.GL_FRONT, GL2.GL_SHININESS, 31);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {

		if (height <= 0) {
			height = 1;
		}

		GL2 gl = drawable.getGL().getGL2();

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		GLU glu = new GLU();
		glu.gluPerspective(45.0f, (float) width / height, 0.2f, 50.0f);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		screenHeight = height;
		screenWidth = width;
	}
	
	/** Move the camera so that it zooms out on a central part of the network,
	 * but this method is not finalized yet
	 */
	public void provideCentralView() {
		camera.moveTo(new Vector3(0, 0, 0));
		
		if (findAveragePosition(networkView.getModel().getNodeList()) != null) {
			camera.moveTo(findAveragePosition(networkView.getModel().getNodeList()));
		}
		camera.moveBackward();
		camera.zoomOut(40);
	}
}
