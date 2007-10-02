package org.nightlabs.jfire.scripting.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class SimpleConditionComposite 
extends XComposite 
{ 
	public SimpleConditionComposite(Collection<ScriptConditioner> scriptConditioners, Composite parent, int style) 
	{
		super(parent, style);
		this.scriptConditioners = scriptConditioners;
		createComposite(this);
	}

	public SimpleConditionComposite(Collection<ScriptConditioner> scriptConditioners,
			Composite parent, int style, LayoutMode layoutMode, LayoutDataMode layoutDataMode) 
	{
		super(parent, style, layoutMode, layoutDataMode);
		this.scriptConditioners = scriptConditioners;
		createComposite(this);
	}

	private Collection<ScriptConditioner> scriptConditioners;
	private Map<ScriptRegistryItemID, List<Object>> variable2PossibleValues;
	private Map<ScriptRegistryItemID, List<CompareOperator>> variable2CompareOperators;	
	private Map<ScriptRegistryItemID, String> variable2Name;
	private Map<Object, String> value2Name;
	
	private XComboComposite<ScriptRegistryItemID> variableCombo;
	private XComboComposite<CompareOperator> operatorCombo;	
	private XComboComposite<Object> valueCombo;

	protected void createComposite(Composite parent) 
	{
		GridLayout layout = new GridLayout(3, true);
		XComposite.configureLayout(LayoutMode.TOP_BOTTOM_WRAPPER, layout);
		parent.setLayout(layout);
		
		int size = scriptConditioners.size();
		List<ScriptRegistryItemID> variableNames = new ArrayList<ScriptRegistryItemID>(size);
		variable2PossibleValues = new HashMap<ScriptRegistryItemID, List<Object>>(size);
		variable2CompareOperators = new HashMap<ScriptRegistryItemID, List<CompareOperator>>(size);
		variable2Name = new HashMap<ScriptRegistryItemID, String>(size);
		value2Name = new HashMap<Object, String>();
		for (ScriptConditioner scriptConditioner : scriptConditioners) {
			variableNames.add(scriptConditioner.getScriptRegistryItemID());
			variable2PossibleValues.put(scriptConditioner.getScriptRegistryItemID(), 
					new ArrayList(scriptConditioner.getPossibleValues()));
			variable2CompareOperators.put(scriptConditioner.getScriptRegistryItemID(), 
					scriptConditioner.getCompareOperators());
			variable2Name.put(scriptConditioner.getScriptRegistryItemID(), 
					scriptConditioner.getScript().getName().getText());
			ILabelProvider labelProvider = scriptConditioner.getValueLabelProvider();
			for (Object value : scriptConditioner.getPossibleValues()) {
				value2Name.put(value, labelProvider.getText(value));
			}
		}

		int defaultWidgetStyle = XComboComposite.getDefaultWidgetStyle(this); 
		
		variableCombo = new XComboComposite<ScriptRegistryItemID>( parent, defaultWidgetStyle, 
				(String) null, scriptLabelProvider );
		variableCombo.setInput( variableNames );
		variableCombo.setLayoutData(new GridData(GridData.FILL_BOTH));
		variableCombo.addSelectionListener(variableListener);
		variableCombo.selectElementByIndex(0);
		selectedVariable = variableCombo.getSelectedElement();

		operatorCombo = new XComboComposite<CompareOperator>(	parent, defaultWidgetStyle, (String) null );
		operatorCombo.setInput( variable2CompareOperators.get(selectedVariable) );
		operatorCombo.setLayoutData(new GridData(GridData.FILL_BOTH));
		operatorCombo.addSelectionListener(operatorListener);

		valueCombo = new XComboComposite<Object>(	parent, defaultWidgetStyle, (String) null,
				valueLabelProvider );
		valueCombo.setInput( variable2PossibleValues.get(selectedVariable) );
		valueCombo.setLayoutData(new GridData(GridData.FILL_BOTH));		
		valueCombo.addSelectionListener(valueListener);
		
		refresh();
		variableCombo.selectElementByIndex(0);
	}
	
	private org.eclipse.jface.viewers.ILabelProvider scriptLabelProvider = new org.eclipse.jface.viewers.LabelProvider(){	
		public String getText(Object object) {
			return variable2Name.get((ScriptRegistryItemID)object);
		}	
	};

	private org.eclipse.jface.viewers.ILabelProvider valueLabelProvider = new org.eclipse.jface.viewers.LabelProvider(){	
		public String getText(Object object) {
			return value2Name.get(object);
		}	
	};
	
	private SelectionListener operatorListener = new SelectionListener(){	
		public void widgetSelected(SelectionEvent e) {
			condition = null;
			selectedCompareOperator = operatorCombo.getSelectedElement();
			fireConditionChanged();
		}	
		public void widgetDefaultSelected(SelectionEvent e) {			
		}	
	};
	
	private SelectionListener valueListener = new SelectionListener(){	
		public void widgetSelected(SelectionEvent e) {
			condition = null;
			selectedValue = valueCombo.getSelectedElement();
			fireConditionChanged();
		}	
		public void widgetDefaultSelected(SelectionEvent e) {			
		}	
	};

	private SelectionListener variableListener = new SelectionListener(){	
		public void widgetSelected(SelectionEvent e) { 
			refresh();				
		}	
		public void widgetDefaultSelected(SelectionEvent e) {			
		}	
	};
	
	protected void refresh() 
	{
		condition = null; 
		selectedVariable = variableCombo.getSelectedElement();
		operatorCombo.setInput(variable2CompareOperators.get(selectedVariable));
		valueCombo.setInput(variable2PossibleValues.get(selectedVariable));
		
		operatorCombo.selectElementByIndex(0);
		if (valueCombo.getElements().size() > 0)
			valueCombo.selectElementByIndex(0);

		selectedVariable = variableCombo.getSelectedElement();
		selectedCompareOperator = operatorCombo.getSelectedElement();
		if (valueCombo.getElements().size() > 0)
			selectedValue = valueCombo.getSelectedElement();
		else
			selectedValue = "";		 //$NON-NLS-1$
		
		fireConditionChanged();
		layout(true, true);
	}
	
	private void selectVariable(ScriptRegistryItemID scriptID) {
		variableCombo.selectElement(scriptID);
		refresh();
	}

	private void selectCompareOperator(CompareOperator compareOperator) {
		operatorCombo.selectElement(compareOperator);
		selectedCompareOperator = compareOperator;
	}

	private void selectValue(Object value) {
		valueCombo.selectElement(value);
		selectedValue = value;
	}
	
	private ScriptRegistryItemID selectedVariable;
	private CompareOperator selectedCompareOperator;
	private Object selectedValue;
	
	private ISimpleCondition condition;
	public ISimpleCondition getCondition() 
	{
		if (condition == null) {			
			condition = new SimpleCondition(selectedVariable, 
					selectedCompareOperator, selectedValue);			
		}
		return condition;
	}	

	public void setSimpleCondition(ISimpleCondition simpleCondition) 
	{
		selectVariable(simpleCondition.getScriptRegistryItemID());
		selectCompareOperator(simpleCondition.getCompareOperator());
		selectValue(simpleCondition.getValue());
	}
		
	private ListenerList listeners = new ListenerList();
	public void addConditionChangedListener(ConditionChangeListener listener) {
		listeners.add(listener);
	}
	public void removeConditionChangedListener(ConditionChangeListener listener) {
		listeners.remove(listener);
	}
	
	private void fireConditionChanged() 
	{
		for (int i=0; i<listeners.getListeners().length; i++) {
			ConditionChangeListener listener = (ConditionChangeListener) listeners.getListeners()[i];
			listener.conditonChanged(new ConditionChangedEvent(getCondition(), this));
		}
	}
	
}
