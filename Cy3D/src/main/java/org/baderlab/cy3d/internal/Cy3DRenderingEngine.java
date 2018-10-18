package org.baderlab.cy3d.internal;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.util.Properties;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.RootPaneContainer;

import org.baderlab.cy3d.internal.cytoscape.view.Cy3DNetworkView;
import org.baderlab.cy3d.internal.eventbus.EventBusProvider;
import org.baderlab.cy3d.internal.graphics.GraphicsConfiguration;
import org.baderlab.cy3d.internal.graphics.RenderEventListener;
import org.baderlab.cy3d.internal.task.TaskFactoryListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.swing.DialogTaskManager;

import com.jogamp.common.util.awt.AWTEDTExecutor;
import com.jogamp.nativewindow.awt.AWTPrintLifecycle;

/** 
 * This class represents a Cy3DRenderingEngine, responsible for
 * creating a rendering of a {@link CyNetwork}.
 */
class Cy3DRenderingEngine implements RenderingEngine<CyNetwork> {
	
	private final Cy3DNetworkView networkView;
	private final VisualLexicon visualLexicon;
	
	private GLJPanel panel;
	private Properties props;
	
	
	public Cy3DRenderingEngine(
			JComponent component,
			JComponent inputComponent,
			Cy3DNetworkView viewModel, 
			VisualLexicon visualLexicon, 
			EventBusProvider eventBusProvider, 
			GraphicsConfiguration configuration,
			TaskFactoryListener taskFactoryListener, 
			DialogTaskManager taskManager) {
		
		this.networkView = viewModel;
		this.visualLexicon = visualLexicon;
		this.props = new Properties();
		
		setUpCanvas(component, inputComponent, configuration, eventBusProvider, taskFactoryListener, taskManager);
	}
	
	
	/** Set up the canvas by creating and placing it, along with a Graphics
	 * object, into the container
	 * 
	 * @param container A container in the GUI window used to contain
	 * the rendered results
	 */
	private void setUpCanvas(JComponent container, JComponent inputComponent, 
			                 GraphicsConfiguration configuration, EventBusProvider eventBusProvider, 
			                 TaskFactoryListener taskFactoryListener, DialogTaskManager taskManager) {
		
		GLProfile profile = GLProfile.getDefault(); // Use the system's default version of OpenGL
		GLCapabilities capabilities = new GLCapabilities(profile);
		capabilities.setHardwareAccelerated(true);
		capabilities.setDoubleBuffered(true);
		
		panel = new GLJPanel(capabilities); // GLJPanel is meant to be used with JInternalFrame
		panel.setIgnoreRepaint(true); // TODO: check if negative effects produced by this
		//panel.setDoubleBuffered(true);
		
		RenderEventListener renderEventListener = new RenderEventListener(networkView, visualLexicon, eventBusProvider, 
				                                                          configuration, taskFactoryListener, taskManager, panel, inputComponent);

		panel.addGLEventListener(renderEventListener);
		
		networkView.addContainer(panel); // When networkView.updateView() is called it will repaint all containers it owns

		if (container instanceof RootPaneContainer) {
			RootPaneContainer rootPaneContainer = (RootPaneContainer) container;
			Container pane = rootPaneContainer.getContentPane();
			pane.setLayout(new BorderLayout());
			pane.add(panel, BorderLayout.CENTER);
		} else {
			container.setLayout(new BorderLayout());
			container.add(panel, BorderLayout.CENTER);
		}
		
		configuration.initializeFrame(container, inputComponent);
	}
	
	
	@Override
	public View<CyNetwork> getViewModel() {
		return networkView;
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return visualLexicon;
	}

	@Override
	public Properties getProperties() {
		return props; // can't return null or (File > Print) won't work
	}
	
	@Override
	public Printable createPrintable() {
		return null;
	}

	@Override
	public Image createImage(int width, int height) {
		Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);		

		Dimension panelSize = panel.getSize();
		
		panel.setSize(width, height);
		panel.paint(image.getGraphics());
		panel.setSize(panelSize);
		
		return image;
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int width, int height) {
		return null;
	}

	@Override
	public void printCanvas(final Graphics printCanvas) {
		double scaleX = (double)panel.getWidth()  / (double)panel.getSurfaceWidth();
		double scaleY = (double)panel.getHeight() / (double)panel.getSurfaceHeight();
		
		AWTPrintLifecycle.Context ctx = AWTPrintLifecycle.Context.setupPrint(panel, scaleX, scaleY, 0, -1, -1);
		try {
			AWTEDTExecutor.singleton.invoke(true, new Runnable() {
				public void run() {
					panel.print(printCanvas);
				}
			});
		} finally {
			ctx.releasePrint();
		}
	}
	
	@Override
	public String getRendererId() {
		return Cy3DNetworkViewRenderer.ID;
	}
	
	@Override
	public void dispose() {
	}
}