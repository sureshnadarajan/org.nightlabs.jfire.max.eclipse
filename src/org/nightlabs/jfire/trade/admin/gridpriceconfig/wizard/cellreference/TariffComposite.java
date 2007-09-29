package org.nightlabs.jfire.trade.admin.gridpriceconfig.wizard.cellreference;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.trade.tariff.TariffListComposite;

public class TariffComposite extends AbstractCellReferenceComposite{

	private Tariff selectedTariff = null;
	private CellReferencePage cellReferencePage = null;
	
	public TariffComposite(CellReferencePage cellReferencePage, Composite parent) {
		super(parent, SWT.None);
		this.cellReferencePage = cellReferencePage;

//		Group tariffListGroup = new Group(this, SWT.NONE);
//		tariffListGroup.setText(Messages.getString("org.nightlabs.jfire.trade.admin.gridpriceconfig.wizard.cellreference.TariffComposite.tariffListGroup.text")); //$NON-NLS-1$
//		tariffListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
//		tariffListGroup.setLayout(new GridLayout());

		TariffListComposite tariffListComposite = new TariffListComposite(this, SWT.NONE, false, null);
		tariffListComposite.getGridData().grabExcessHorizontalSpace = true;
		tariffListComposite.loadTariffs();
		tariffListComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				TariffListComposite tc = (TariffListComposite)e.getSource();
				Tariff t = tc.getSelectedTariff();
				if(t != null){
					selectedTariff = t;
					checked(true);
				}//if
			}
		});
	}
	
	protected void createScript(){
		StringBuffer scriptBuffer = new StringBuffer();
		
		scriptBuffer.append("TariffID.create") //$NON-NLS-1$
			.append(CellReferenceWizard.L_BRACKET)
				.append(CellReferenceWizard.DOUBLE_QUOTE).append(selectedTariff.getOrganisationID()).append(CellReferenceWizard.DOUBLE_QUOTE)
				.append(",") //$NON-NLS-1$
				.append(CellReferenceWizard.DOUBLE_QUOTE).append(selectedTariff.getTariffID()).append(CellReferenceWizard.DOUBLE_QUOTE)
			.append(CellReferenceWizard.R_BRACKET);
		
		cellReferencePage.addDimensionScript(this.getClass().getName(), scriptBuffer.toString());
	}
	
	@Override
	protected void doEnable() {
		if(selectedTariff != null)
			createScript();
	}
	
	@Override
	protected void doDisable() {
		cellReferencePage.removeDimensionScript(this.getClass().getName());
	}
}
