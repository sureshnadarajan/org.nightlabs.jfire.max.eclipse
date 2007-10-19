/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.trade.admin.ui.gridpriceconfig;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.notification.IDirtyStateManager;
import org.nightlabs.base.ui.progress.XProgressMonitor;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.TariffMapper;
import org.nightlabs.jfire.accounting.gridpriceconfig.AssignInnerPriceConfigCommand;
import org.nightlabs.jfire.accounting.gridpriceconfig.FormulaPriceConfig;
import org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculationException;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator;
import org.nightlabs.jfire.accounting.priceconfig.FetchGroupsPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.CustomerGroupMapper;
import org.nightlabs.jfire.trade.admin.ui.gridpriceconfig.wizard.AbstractChooseGridPriceConfigPage;
import org.nightlabs.jfire.trade.admin.ui.gridpriceconfig.wizard.AbstractChooseGridPriceConfigWizard;
import org.nightlabs.jfire.trade.admin.ui.resource.Messages;
import org.nightlabs.jfire.trade.ui.customergroupmapping.CustomerGroupMappingDAO;
import org.nightlabs.jfire.trade.ui.tariffmapping.TariffMappingDAO;

/**
 * This composite can be used to display and edit the whole grid-price-configuration
 * of one ProductType.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class PriceConfigComposite extends XComposite
{
	private static final Logger logger = Logger.getLogger(PriceConfigComposite.class);

	private ProductTypeSelector productTypeSelector;
	private DimensionValueSelector dimensionValueSelector;
	private DimensionXYSelector dimensionXYSelector;
	private PriceConfigGrid priceConfigGrid;
	private CellDetail cellDetail;
	private PriceCalculator priceCalculator;

	protected ProductTypeSelector createProductTypeSelector(Composite parent)
	{
		return new ProductTypeSelectorListImpl(parent, SWT.NONE);
	}

	/**
	 * @return Returns the productTypeSelector.
	 */
	public ProductTypeSelector getProductTypeSelector()
	{
		return productTypeSelector;
	}

	protected DimensionValueSelector createDimensionValueSelector(Composite parent)
	{
		return new DimensionValueSelectorComboImpl(parent, SWT.NONE);
	}

	/**
	 * @return Returns the dimensionValueSelector.
	 */
	public DimensionValueSelector getDimensionValueSelector()
	{
		return dimensionValueSelector;
	}

	protected DimensionXYSelector createDimensionXYSelector(Composite parent)
	{
		return new DimensionXYSelectorComboImpl(parent, SWT.NONE, getDimensionValueSelector());
	}

	protected PriceConfigGrid createPriceConfigGrid(Composite parent)
	{
		return new PriceConfigGrid(
				parent,
				getProductTypeSelector(),
				getDimensionValueSelector(),
				getDimensionXYSelector());
	}

	protected CellDetail createCellDetail(Composite parent)
	{
		return new CellDetail(parent, SWT.NONE, getPriceConfigGrid(), this);
	}

	/**
	 * @return Returns the priceConfigGrid.
	 */
	public PriceConfigGrid getPriceConfigGrid()
	{
		return priceConfigGrid;
	}

	/**
	 * @return Returns the dimensionXYSelector.
	 */
	public DimensionXYSelector getDimensionXYSelector()
	{
		return dimensionXYSelector;
	}

	/**
	 * @return Returns the cellDetail.
	 */
	public CellDetail getCellDetail()
	{
		return cellDetail;
	}

	/**
	 * @return Returns the priceCalculator.
	 */
	public PriceCalculator getPriceCalculator()
	{
		return priceCalculator;
	}

	public PriceConfigComposite(Composite parent) {
		this(parent, null);
	}
	
	private IDirtyStateManager dirtyStateManager;
	public IDirtyStateManager getDirtyStateManager() {
		return dirtyStateManager;
	}
	
	private boolean changed = true;
	public boolean isChanged() {
		return changed;
	}
	
	private Composite stackWrapper;
	private StackLayout stackLayout;
	private Composite contentComp = null;
	private Composite noPriceConfigComp = null;
	public PriceConfigComposite(Composite parent, IDirtyStateManager dirtyStateManager)
	{
		super(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

		this.dirtyStateManager = dirtyStateManager;
		
		stackWrapper = new XComposite(this, SWT.NONE);
		stackLayout = new StackLayout();		
		stackWrapper.setLayout(stackLayout);
		stackWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));		
		
		contentComp = createContentComposite(stackWrapper);
		noPriceConfigComp = createNoPriceConfigAssignedComposite(stackWrapper);
//		stackLayout.topControl = contentComp;		
		stackLayout.topControl = noPriceConfigComp;
	}

	protected Composite createLeftCarrierComposite(Composite parent)
	{
		return new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
	}

	protected Composite createRightCarrierComposite(Composite parent)
	{
		return new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
	}

	protected Composite createContentComposite(Composite parent) 
	{
		SashForm sfLeftRight = new SashForm(parent, SWT.NONE | SWT.HORIZONTAL);
		sfLeftRight.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite left = createLeftCarrierComposite(sfLeftRight);
		Composite right = createRightCarrierComposite(sfLeftRight);
		sfLeftRight.setWeights(new int[] {1, 2});

		productTypeSelector = createProductTypeSelector(left);
		dimensionValueSelector = createDimensionValueSelector(left);
		dimensionXYSelector = createDimensionXYSelector(right);

		SashForm sfGrid = new SashForm(right, SWT.NONE | SWT.VERTICAL);
		sfGrid.setLayoutData(new GridData(GridData.FILL_BOTH));

		priceConfigGrid = createPriceConfigGrid(sfGrid);
		priceConfigGrid.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (dirtyStateManager != null)
					dirtyStateManager.markDirty();
			}
		});

		cellDetail = createCellDetail(sfGrid);
		// add listeners to notify dirty state
//		cellDetail.getCellDetailText().addModifyListener(cellEditModifyListener);
//		cellDetail.getCellDetailFallbackText().addModifyListener(cellEditModifyListener);

//		cellDetail.getCellDetailText().getDocument().addDocumentListener(cellEditModifyListener);
//		cellDetail.getCellDetailFallbackText().getDocument().addDocumentListener(cellEditModifyListener);

//		dimensionValueSelector.addPropertyChangeListener(
//			DimensionValueSelector.PROPERTYCHANGEKEY_ADDDIMENSIONVALUE, new PropertyChangeListener() {
//				public void propertyChange(PropertyChangeEvent evt) {
//					if (priceCalculator != null) {
//						priceCalculator.preparePriceCalculation_createPackagedResultPriceConfigs();
//					}
//				}
//			}
//		);

		dimensionValueSelector.addPropertyChangeListener(
				DimensionValueSelector.PROPERTYCHANGEKEY_ADDDIMENSIONVALUE, 
				dimensionValueChangeListener);

		dimensionValueSelector.addPropertyChangeListener(
				DimensionValueSelector.PROPERTYCHANGEKEY_REMOVEDIMENSIONVALUE, 
				dimensionValueChangeListener);
		
		sfGrid.setWeights(new int[] {1, 1});
		
		return sfLeftRight;
	}
	
	protected Composite createNoPriceConfigAssignedComposite(Composite parent) 
	{
		Composite noPriceConfigComp = new XComposite(parent, SWT.NONE);
		Label label = new Label(noPriceConfigComp, SWT.NONE);
		label.setText(Messages.getString("org.nightlabs.jfire.trade.admin.ui.gridpriceconfig.PriceConfigComposite.noPriceConfigAssignedLabel.text")); //$NON-NLS-1$
		Button assignButton = new Button(noPriceConfigComp, SWT.NONE);
		assignButton.setText(Messages.getString("org.nightlabs.jfire.trade.admin.ui.gridpriceconfig.PriceConfigComposite.assignButton.text")); //$NON-NLS-1$
		assignButton.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				AbstractChooseGridPriceConfigWizard wizard = createChoosePriceConfigWizard(
						(ProductTypeID) JDOHelper.getObjectId(packageProductType.getExtendedProductType())); 
				DynamicPathWizardDialog dialog = new DynamicPathWizardDialog(wizard);
//				dialog.setTitle("Choose Price Configuration");
				int returnCode = dialog.open();
				if (returnCode == Dialog.OK) {
					assignNewPriceConfig(wizard);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}		
		});
		return noPriceConfigComp;
	}

	protected static final String[] FETCH_GROUPS_INNER_PRICE_CONFIG_FOR_EDITING = {
		FetchPlan.DEFAULT,
		FetchGroupsPriceConfig.FETCH_GROUP_EDIT};

	protected abstract IInnerPriceConfig retrieveInnerPriceConfigForEditing(PriceConfigID priceConfigID);

	protected IInnerPriceConfig createInnerPriceConfig()
	{
		return new FormulaPriceConfig(IDGenerator.getOrganisationID(), IDGenerator.nextID(PriceConfig.class));
	}

//	private ModifyListener cellEditModifyListener = new ModifyListener(){	
//		public void modifyText(ModifyEvent e) {
//			if (dirtyStateManager != null)
//				dirtyStateManager.markDirty();
//		}	
//	};
	
//	private boolean initalState = false;
//	/**
//	 * sets the initalState, to determine that while in this state no calls to the
//	 * dirtyStateManager are performed, to avoid dirty states when calling 
//	 * setPackageProductType(ProductType packageProductType)
//	 * 
//	 * @param initalState the initalState to set
//	 */
//	public void setInitaliseState(boolean initalState) {
//		this.initalState = initalState;
//	}
//	/**
//	 * returns the initalState
//	 * @return the initalState
//	 */
//	public boolean isInitalState() {
//		return initalState;
//	}
	
//	private IDocumentListener cellEditModifyListener = new IDocumentListener(){	
//		public void documentAboutToBeChanged(DocumentEvent arg0) {
//		}
//
//		public void documentChanged(DocumentEvent arg0) {
////			// FIXME: documentChanged is called even if only a productType is selected
////			if (dirtyStateManager != null && !initalState)
////				dirtyStateManager.markDirty();
//		}	
//	};

	/**
	 * This listener is triggered, whenever a DimensionValue is either added or removed.
	 * It is NOT triggered, when a DimensionValue is merely selected (i.e. no real change to the PriceConfig).
	 */
	private PropertyChangeListener dimensionValueChangeListener = new PropertyChangeListener(){
		public void propertyChange(PropertyChangeEvent evt) {
			if (logger.isDebugEnabled())
				logger.debug("dimensionValueChangeListener#propertyChange: propertyName=" + evt.getPropertyName()); //$NON-NLS-1$
//			if (dirtyStateManager != null && !initalState)
//				dirtyStateManager.markDirty();

			if (priceCalculator != null)
				priceCalculator.preparePriceCalculation_createPackagedResultPriceConfigs();

			if (dirtyStateManager != null)
				dirtyStateManager.markDirty();
		}	
	};
	
	public static final String[] FETCH_GROUPS_TARIFF_MAPPING = {
		FetchPlan.DEFAULT
	};

	public static final String[] FETCH_GROUPS_CUSTOMER_GROUP_MAPPING = {
		FetchPlan.DEFAULT
	};

	protected CustomerGroupMapper createCustomerGroupMapper()
	{
		return new CustomerGroupMapper(CustomerGroupMappingDAO.sharedInstance().getCustomerGroupMappings(FETCH_GROUPS_CUSTOMER_GROUP_MAPPING, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new XProgressMonitor()));
	}

	protected TariffMapper createTariffMapper()
	{
		return new TariffMapper(TariffMappingDAO.sharedInstance().getTariffMappings(FETCH_GROUPS_TARIFF_MAPPING, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new XProgressMonitor()));
	}

	protected PriceCalculator createPriceCalculator(ProductType packageProductType)
	{
		return new PriceCalculator(packageProductType, createCustomerGroupMapper(), createTariffMapper());
	}

	private ProductType packageProductType = null;	
	public ProductType getPackageProductType() {
		return packageProductType;
	}
	
	public void setPackageProductType(ProductType packageProductType)
//	throws ModuleException
	{
		productTypeSelector.setPackageProductType(null);
		dimensionValueSelector.setGridPriceConfig(null);
		priceCalculator = null;
		this.packageProductType = packageProductType;
		
		if (packageProductType != null) {
			if (packageProductType.getInnerPriceConfig() != null || packageProductType.getPackagePriceConfig() != null) {
				priceCalculator = createPriceCalculator(packageProductType);
				priceCalculator.preparePriceCalculation();
				try {
					priceCalculator.calculatePrices();
				} catch (PriceCalculationException e) {
					throw new RuntimeException(e);
				}
				
				// The packagePriceConfig defines all parameters (dimension values) we need to know.
				// When the packagePriceConfig comes from the server (after preparePriceCalculation hase been called)
				// it has the same parameters (except PriceFragmentTypes) as the innerPriceConfig.
				// The PriceFragmentTypes have already been collected from all the packaged PriceConfigs.
				GridPriceConfig gridPriceConfig = (GridPriceConfig) packageProductType.getPackagePriceConfig();
				if (gridPriceConfig == null)
					gridPriceConfig = (GridPriceConfig) packageProductType.getInnerPriceConfig();

				dimensionValueSelector.setGridPriceConfig(gridPriceConfig);
				productTypeSelector.setPackageProductType(packageProductType);								
			}
			
			if (packageProductType.getInnerPriceConfig() != null) {
				// show price config comp
				stackLayout.topControl = contentComp;
				stackWrapper.layout(true, true);								
			}
		} // if (packageProductType != null) {
		priceConfigGrid.setPriceCalculator(priceCalculator);
	}

	/**
	 * stores the Price Configurations
	 *  
	 * @param priceConfigs the priceConfigs to store
	 * @param assignInnerPriceConfigCommand TODO
	 * @return a Collection of the stored gridPriceConfigs
	 */
	protected abstract <P extends GridPriceConfig> Collection<P> storePriceConfigs(Collection<P> priceConfigs, AssignInnerPriceConfigCommand assignInnerPriceConfigCommand);

	/**
	 * returns an implementation of {@link AbstractChooseGridPriceConfigWizard} to let the user
	 * choose a Price Configuration, if no Price Configuration has been assigned yet
	 * 
	 * @param parentProductTypeID the parent product Type
	 * @return an implementation of AbstractChooseGridPriceConfigWizard
	 */
	public abstract AbstractChooseGridPriceConfigWizard createChoosePriceConfigWizard(ProductTypeID parentProductTypeID);
	
	/**
	 * Create an implementation of {@link CellReferenceProductTypeSelector} to let the user
	 * choose a <code>ProductType</code> or return <code>null</code>, if this dimension is not
	 * used in the concrete price config.
	 * 
	 * @return an implementation of {@link CellReferenceProductTypeSelector} or <code>null</code>.
	 */
	public abstract CellReferenceProductTypeSelector createCellReferenceProductTypeSelector();
	
	public void submit()
	{
		ProductType packageProductType = productTypeSelector.getPackageProductType();
		if (packageProductType == null)
			return;

		ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(packageProductType);
//		PriceConfigID innerPriceConfigID = (PriceConfigID) JDOHelper.getObjectId(packageProductType.getInnerPriceConfig()); // This doesn't work, if the PC is new!
		IInnerPriceConfig ipc = packageProductType.getInnerPriceConfig();
		PriceConfigID innerPriceConfigID = ipc == null ? null : PriceConfigID.create(ipc.getOrganisationID(), ipc.getPriceConfigID());

		String localOrganisationID;
		try {
			localOrganisationID = Login.getLogin().getOrganisationID();
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}

		// collect all price configurations
		Map<GridPriceConfig, List<ProductTypeSelector.Item>> priceConfigs = new HashMap<GridPriceConfig, List<ProductTypeSelector.Item>>(productTypeSelector.getProductTypeItems().size());
		for (Iterator it = productTypeSelector.getProductTypeItems().iterator(); it.hasNext(); ) {
			ProductTypeSelector.Item item = (ProductTypeSelector.Item) it.next();
			if (!localOrganisationID.equals(item.getProductType().getOrganisationID())) // ignore partner-ProductTypes as we must not modify their prices
				continue;

			GridPriceConfig priceConfig = item.getPriceConfig();
			if (priceConfig != null) {
				List<ProductTypeSelector.Item> items = priceConfigs.get(priceConfig);
				if (items == null) {
					items = new ArrayList<ProductTypeSelector.Item>();
					priceConfigs.put(priceConfig, items);
				}
				items.add(item);
			}
		}

		Set<PriceConfigID> priceConfigIDs = NLJDOHelper.getObjectIDSet(priceConfigs.keySet());
		priceConfigIDs = new HashSet<PriceConfigID>(priceConfigIDs);
		// if there are priceConfigs which have never been stored (i.e. no ID assigned), we ignore them silently.
		while (priceConfigIDs.remove(null));

		if (!priceConfigIDs.isEmpty()) {
			// show the consequences and ask the user whether he really wants to save
			StorePriceConfigsConfirmationDialog dialog = new StorePriceConfigsConfirmationDialog(
					getShell(),
					priceConfigIDs,
					productTypeID, innerPriceConfigID);
			if (dialog.open() != StorePriceConfigsConfirmationDialog.OK)
				return;
		}

		// store the price configs to the server (it will recalculate all affected product types)
		Collection<GridPriceConfig> newPCs = storePriceConfigs(
				new ArrayList<GridPriceConfig>(priceConfigs.keySet()), // the new ArrayList is necessary, because the keySet is not serializable!
				new AssignInnerPriceConfigCommand(
						productTypeID,
						innerPriceConfigID,
						packageProductType.getFieldMetaData("innerPriceConfig").isValueInherited()));

		// and replace the local price configs by the new ones (freshly detached from the server)
		for (GridPriceConfig priceConfig : newPCs) {
			List<ProductTypeSelector.Item> items = priceConfigs.get(priceConfig);
			for (ProductTypeSelector.Item item : items)
				item.setPriceConfig(priceConfig);
		}
	}
	
	public void assignNewPriceConfig(AbstractChooseGridPriceConfigWizard wizard) 
	{
		IInnerPriceConfig innerPC = wizard.getAbstractChooseGridPriceConfigPage().getSelectedPriceConfig();
		if (innerPC != null) {
			PriceConfigID innerPCID = (PriceConfigID) JDOHelper.getObjectId(innerPC);
			if (innerPCID != null)
				innerPC = retrieveInnerPriceConfigForEditing(innerPCID);
		}
		packageProductType.setInnerPriceConfig(innerPC);

		switch (wizard.getAbstractChooseGridPriceConfigPage().getAction()) {
			case AbstractChooseGridPriceConfigPage.ACTION_INHERIT:
				packageProductType.getFieldMetaData("innerPriceConfig").setValueInherited(true); //$NON-NLS-1$
				break;
			case AbstractChooseGridPriceConfigPage.ACTION_LATER:
				packageProductType.getFieldMetaData("innerPriceConfig").setValueInherited(false); //$NON-NLS-1$
				// nothing
				break;
			case AbstractChooseGridPriceConfigPage.ACTION_CREATE:
				packageProductType.getFieldMetaData("innerPriceConfig").setValueInherited(false); //$NON-NLS-1$
				IInnerPriceConfig fpc = createInnerPriceConfig();
				fpc.getName().copyFrom(wizard.getAbstractChooseGridPriceConfigPage().getNewPriceConfigNameBuffer());
				packageProductType.setInnerPriceConfig(fpc);
				break;
			case AbstractChooseGridPriceConfigPage.ACTION_SELECT:
				packageProductType.getFieldMetaData("innerPriceConfig").setValueInherited(false); //$NON-NLS-1$
				break;
			default:
				throw new IllegalStateException("selectPriceConfigPage.getAction() returned unknown action: " + wizard.getAbstractChooseGridPriceConfigPage().getAction()); //$NON-NLS-1$
		} // switch (selectPriceConfigPage.getAction()) {

		if (packageProductType.getInnerPriceConfig() == null)
			stackLayout.topControl = noPriceConfigComp;
		else {
			stackLayout.topControl = contentComp;

			((GridPriceConfig)packageProductType.getPackagePriceConfig()).adoptParameters(packageProductType.getInnerPriceConfig(), false);
		}
		stackWrapper.layout(true, true);
		setPackageProductType(packageProductType);
		dirtyStateManager.markDirty();
	}
}
