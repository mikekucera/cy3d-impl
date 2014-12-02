package org.baderlab.cy3d.internal.cytoscape.view;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class DefaultValueVault {
	
	// Assumes VisualProperty ID names are unique
	private Map<String, VisualPropertyValueHolder<?>> nodeDefaultValues;
	private Map<String, VisualPropertyValueHolder<?>> edgeDefaultValues;
	private Map<String, VisualPropertyValueHolder<?>> networkDefaultValues;
	
	private Map<Class<? extends CyIdentifiable>,
		Map<String, VisualPropertyValueHolder<?>>> defaultValueSets;
	
	private VisualLexicon visualLexicon;
	
	public DefaultValueVault(VisualLexicon visualLexicon) {
		this.visualLexicon = visualLexicon;
		
		nodeDefaultValues = new HashMap<String, VisualPropertyValueHolder<?>>();
		edgeDefaultValues = new HashMap<String, VisualPropertyValueHolder<?>>();
		networkDefaultValues = new HashMap<String, VisualPropertyValueHolder<?>>();
		
		defaultValueSets = new HashMap<Class<? extends CyIdentifiable>, Map<String, VisualPropertyValueHolder<?>>>();
		defaultValueSets.put(CyNode.class, nodeDefaultValues);
		defaultValueSets.put(CyEdge.class, edgeDefaultValues);
		defaultValueSets.put(CyNetwork.class, networkDefaultValues);
	
		// Populate with default values from the relevant VisualLexicon (eg. BasicVisualLexicon, MinimalVisualLexicon)
		populateDefaultValues();
		
		// Override VisualLexicon default values with custom
		// Wind values, useful for old MinimalVisualLexicon values
//		updateDefaultValues();
	}
	
	// Sets initial default values
	private void populateDefaultValues() {
		VisualPropertyValueHolder<?> valueHolder;
		Class<?> targetDataType;
		
		for (VisualProperty<?> visualProperty : visualLexicon.getAllVisualProperties()) {
			valueHolder = new VisualPropertyValueHolder<Object>(visualProperty.getDefault());
			targetDataType = visualProperty.getTargetDataType();
			
			if (defaultValueSets.get(targetDataType) != null) {
				defaultValueSets.get(targetDataType).put(visualProperty.getIdString(), valueHolder);
			}
		}
	}
	
//	// Override selected values from VisualProperties
//	private void updateDefaultValues() {
//		modifyDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(120, 120, 120));
//	}
	
//	@Override
//	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> visualProperty,
//			V defaultValue) {
//		
//	}
	
	public <T, V extends T> void modifyDefaultValue(VisualProperty<? extends T> visualProperty, V value) {
		Class<?> targetDataType = visualProperty.getTargetDataType();
		
		VisualPropertyValueHolder<V> valueHolder = new VisualPropertyValueHolder<V>(value);
		
		if (defaultValueSets.get(targetDataType) != null) {
			defaultValueSets.get(targetDataType).put(visualProperty.getIdString(), valueHolder);
		}
	}
	
	/**
	 * Obtain the default value stored for a given visual property.
	 * 
	 * @param <T> The type of the visual property's value
	 * @param visualProperty The visual property to look for a default value with
	 * @return The default value of the visual property
	 */
	public <T> T getDefaultValue(VisualProperty<T> visualProperty) {
		Class<?> targetDataType = visualProperty.getTargetDataType();
		
		if (defaultValueSets.get(targetDataType) != null) {
			VisualPropertyValueHolder<T> valueHolder = 
				(VisualPropertyValueHolder<T>)
				defaultValueSets.get(targetDataType).get(visualProperty.getIdString());
		
			if (valueHolder != null) {
				return valueHolder.getValue();
			}
		}
		
		return null;
	}
	
//	public void initializeNode(VisualPropertyKeeper<CyNode> keeper) {
//		for (Entry<String, VisualPropertyValueHolder<?>> entry: nodeDefaultValues.entrySet()) {	
//			keeper.setVisualProperty(entry.getKey(), entry.getValue().getValue());
//		}
//	}
//	
//	public void initializeEdge(VisualPropertyKeeper<CyEdge> keeper) {
//		for (Entry<String, VisualPropertyValueHolder<?>> entry: edgeDefaultValues.entrySet()) {	
//			keeper.setVisualProperty(entry.getKey(), entry.getValue().getValue());
//		}
//	}
//	
//	public void initializeNetwork(VisualPropertyKeeper<CyNetwork> keeper) {
//		for (Entry<String, VisualPropertyValueHolder<?>> entry: networkDefaultValues.entrySet()) {	
//			keeper.setVisualProperty(entry.getKey(), entry.getValue().getValue());
//		}
//	}
}

