package org.baderlab.cy3d.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.io.IOException;
import java.util.Properties;

import org.baderlab.cy3d.internal.command.GetDefaultRendererCommandTaskFactory;
import org.baderlab.cy3d.internal.command.SetDefaultRendererCommandTaskFactory;
import org.baderlab.cy3d.internal.cytoscape.view.Cy3DVisualLexicon;
import org.baderlab.cy3d.internal.eventbus.EventBusProvider;
import org.baderlab.cy3d.internal.graphics.GraphicsConfigurationFactory;
import org.baderlab.cy3d.internal.layouts.BoxLayoutAlgorithm;
import org.baderlab.cy3d.internal.layouts.CenterLayoutAlgorithm;
import org.baderlab.cy3d.internal.layouts.CyLayoutAlgorithmAdapter;
import org.baderlab.cy3d.internal.layouts.FlattenLayoutAlgorithm;
import org.baderlab.cy3d.internal.layouts.GridLayoutAlgorithm;
import org.baderlab.cy3d.internal.layouts.SphericalLayoutAlgorithm;
import org.baderlab.cy3d.internal.task.TaskFactoryListener;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewFactoryConfig;
import org.cytoscape.view.model.CyNetworkViewFactoryProvider;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

/**
 * CyActivator object used to import and export services from and to Cytoscape, such
 * as manager and factory objects.
 */
public class CyActivator extends AbstractCyActivator {

	public void start(BundleContext bc) {
		CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		CySwingApplication application = getService(bc, CySwingApplication.class);
		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);
		RenderingEngineManager renderingEngineManager = getService(bc, RenderingEngineManager.class);
		UndoSupport undoSupport = getService(bc, UndoSupport.class);
		CyLayoutAlgorithmManager layoutAlgorithmManager =  getService(bc, CyLayoutAlgorithmManager.class);
		TunableSetter tunableSetter = getService(bc, TunableSetter.class);
		
		// TaskManager object used to execute tasks
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		
		// Register service to collect references to relevant task factories for the right-click context menu
		TaskFactoryListener taskFactoryListener = new TaskFactoryListener();
		registerServiceListener(bc, taskFactoryListener::addNodeViewTaskFactory, taskFactoryListener::removeNodeViewTaskFactory, NodeViewTaskFactory.class);
		registerServiceListener(bc, taskFactoryListener::addEdgeViewTaskFactory, taskFactoryListener::removeEdgeViewTaskFactory, EdgeViewTaskFactory.class);
		registerServiceListener(bc, taskFactoryListener::addNetworkViewTaskFactory, taskFactoryListener::removeNetworkViewTaskFactory, NetworkViewTaskFactory.class);
		registerServiceListener(bc, taskFactoryListener::addNetworkViewLocationTaskFactory, taskFactoryListener::removeNetworkViewLocationTaskFactory, NetworkViewLocationTaskFactory.class);
		
		// Cy3D Visual Lexicon
		Cy3DVisualLexicon cy3dVisualLexicon = new Cy3DVisualLexicon();
		Properties cy3dVisualLexiconProps = new Properties();
		cy3dVisualLexiconProps.setProperty("serviceType", "visualLexicon");
		cy3dVisualLexiconProps.setProperty("id", "cy3d");
		registerService(bc, cy3dVisualLexicon, VisualLexicon.class, cy3dVisualLexiconProps);

		// Cy3D NetworkView factory
		EventBusProvider eventBusProvider = new EventBusProvider();
		
		CyNetworkViewFactoryProvider netViewFactoryFactory = getService(bc, CyNetworkViewFactoryProvider.class);
		CyNetworkViewFactoryConfig config = netViewFactoryFactory.createConfig(cy3dVisualLexicon);
		config.addNonClearableVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION);
		config.addTrackedVisualProperty(Cy3DVisualLexicon.CONFIG_PROP_SELECTED_NODES, BasicVisualLexicon.NODE_SELECTED, Boolean.TRUE::equals);
		CyNetworkViewFactory netViewFactory = netViewFactoryFactory.createNetworkViewFactory(cy3dVisualLexicon, Cy3DNetworkViewRenderer.ID, config);
		
		Properties cy3dNetworkViewFactoryProps = new Properties();
		cy3dNetworkViewFactoryProps.setProperty("serviceType", "factory");
		registerService(bc, netViewFactory, CyNetworkViewFactory.class, cy3dNetworkViewFactoryProps);

		
		// Main RenderingEngine factory
		GraphicsConfigurationFactory mainFactory = GraphicsConfigurationFactory.MAIN_FACTORY;
		Cy3DRenderingEngineFactory cy3dMainRenderingEngineFactory = new Cy3DRenderingEngineFactory(
				renderingEngineManager, cy3dVisualLexicon, taskFactoryListener, dialogTaskManager, eventBusProvider, mainFactory);
		
		// Bird's Eye RenderingEngine factory
		GraphicsConfigurationFactory birdsEyeFactory = GraphicsConfigurationFactory.BIRDS_EYE_FACTORY;
		Cy3DRenderingEngineFactory cy3dBirdsEyeRenderingEngineFactory = new Cy3DRenderingEngineFactory(
				renderingEngineManager, cy3dVisualLexicon, taskFactoryListener, dialogTaskManager, eventBusProvider, birdsEyeFactory);

		
		// NetworkViewRenderer, this is the main entry point that Cytoscape will call into
		Cy3DNetworkViewRenderer networkViewRenderer = new Cy3DNetworkViewRenderer(netViewFactory, cy3dMainRenderingEngineFactory, cy3dBirdsEyeRenderingEngineFactory);
		registerService(bc, networkViewRenderer, NetworkViewRenderer.class, new Properties());
		
		// Still need to register the rendering engine factory directly
		Properties renderingEngineProps = new Properties();
		renderingEngineProps.setProperty(ID, Cy3DNetworkViewRenderer.ID);
		registerAllServices(bc, cy3dMainRenderingEngineFactory, renderingEngineProps);
		
		// Layout algorithms
		CyLayoutAlgorithm frAlgorithm = layoutAlgorithmManager.getLayout("fruchterman-rheingold");
		CyLayoutAlgorithmAdapter fr3DAlgorithm = new CyLayoutAlgorithmAdapter(frAlgorithm, tunableSetter, "fruchterman-rheingold-3D", "3D Force directed (BioLayout)");
		
		registerLayoutAlgorithms(bc,
				fr3DAlgorithm,
				new SphericalLayoutAlgorithm(undoSupport),
				new GridLayoutAlgorithm(undoSupport),
				new BoxLayoutAlgorithm(undoSupport),
				new FlattenLayoutAlgorithm(undoSupport),
				new CenterLayoutAlgorithm(undoSupport)
		);
		
		// About dialog
		AboutDialogAction aboutDialogAction = new AboutDialogAction(application, openBrowser);
		aboutDialogAction.setPreferredMenu("Apps.Cy3D");
		registerAllServices(bc, aboutDialogAction, new Properties());
		
		// Commands
		{
			Properties props = new Properties();
			props.put(COMMAND, "set renderer");
			props.put(COMMAND_NAMESPACE, "cy3d");
			props.put(COMMAND_LONG_DESCRIPTION, "Sets the default renderer used by Cytoscape when creating new network views.");
			registerService(bc, new SetDefaultRendererCommandTaskFactory(applicationManager), TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.put(COMMAND, "get renderer");
			props.put(COMMAND_NAMESPACE, "cy3d");
			props.put(COMMAND_LONG_DESCRIPTION, "Returns the renderer ID for the current default renderer.");
			registerService(bc, new GetDefaultRendererCommandTaskFactory(applicationManager), TaskFactory.class, props);
		}
		
		
		// Special handling for JOGL library
		try {
			JoglInitializer.unpackNativeLibrariesForJOGL(bc);
		} catch (IOException e) {
			// This App will be useless if Jogl can't find its libraries, so best throw an exception to OSGi to shut it down.
			throw new RuntimeException(e);
 		}
	}

	
	private void registerLayoutAlgorithms(BundleContext bc, CyLayoutAlgorithm... algorithms) {
		for(int i = 0; i < algorithms.length; i++) {
			Properties props = new Properties();
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, algorithms[i].toString());
			props.setProperty(MENU_GRAVITY, "30." + (i+1));
			if(i == 0)
				props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			if(i == algorithms.length-1)
				props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			
			registerService(bc, algorithms[i], CyLayoutAlgorithm.class, props);
		}
	}
	
}
