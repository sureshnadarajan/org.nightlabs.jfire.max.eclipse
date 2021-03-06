package org.nightlabs.jfire.voucher.admin.ui.editor.accountpriceconfig;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.FetchPlan;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.action.InheritanceAction;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.priceconfig.FetchGroupsPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.voucher.accounting.VoucherPriceConfig;
import org.nightlabs.jfire.voucher.admin.ui.VoucherAdminPlugin;
import org.nightlabs.jfire.voucher.admin.ui.priceconfig.CurrencyAmountTable;
import org.nightlabs.jfire.voucher.admin.ui.priceconfig.IPriceConfigValueChangedListener;
import org.nightlabs.jfire.voucher.admin.ui.resource.Messages;
import org.nightlabs.jfire.voucher.dao.VoucherTypeDAO;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author fitas [at] NightLabs [dot] de
 *
 */
public class VoucherPriceConfigSection
extends ToolBarSectionPart
{
	private CurrencyAmountTable currencyAmountTableWrapper;

	private VoucherPriceConfig originalVoucherConfig;
	private VoucherType voucherType;
	private VoucherType parentVoucherType;
	private InheritanceAction inheritanceAction;

	private Composite stackWrapper;
	private StackLayout stackLayout;
	private Composite assignNewPriceConfigWrapper = null;
	private Map<Currency, Long> orgPriceMap;

	/**
	 * @param page
	 * @param parent
	 * @param style
	 */
	public VoucherPriceConfigSection(IFormPage page, Composite parent, int style) {
		super(page, parent, style, "Price Configuration"); //$NON-NLS-1$
		
		AddCurrencyConfigAction addCurrencyConfigAction = new AddCurrencyConfigAction();
		getToolBarManager().add(addCurrencyConfigAction);

		RemoveCurrencyConfigAction removeCurrencyConfigAction = new RemoveCurrencyConfigAction();
		getToolBarManager().add(removeCurrencyConfigAction);

		AssignPriceConfigAction assignPriceConfigAction = new AssignPriceConfigAction();
		getToolBarManager().add(assignPriceConfigAction);


		inheritanceAction = new InheritanceAction(){
			@Override
			public void run() {
				inheritPressed();
			}
		};
		inheritanceAction.setEnabled(false);

		getToolBarManager().add(inheritanceAction);

		stackWrapper = new XComposite(getContainer(), SWT.NONE);
		stackLayout = new StackLayout();
		stackWrapper.setLayout(stackLayout);
		stackWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));

		currencyAmountTableWrapper = new CurrencyAmountTable(stackWrapper,false);
		assignNewPriceConfigWrapper = new XComposite(stackWrapper, SWT.NONE,
				LayoutMode.TOTAL_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);

		Button assignNewPriceConfigButton = new Button(assignNewPriceConfigWrapper, SWT.NONE);
		//assignNewPriceConfigButton.setImage(SharedImages.getSharedImage(
		//		TradeAdminPlugin.getDefault(), AbstractGridPriceConfigSection.class, "AssignPriceConfig"));
		assignNewPriceConfigButton.setToolTipText(Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.price.VoucherPriceConfigSection.AssignNewConfigBtnTooltip"));		 //$NON-NLS-1$
		assignNewPriceConfigButton.setText(Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.price.VoucherPriceConfigSection.AssignNewConfigBtn")); //$NON-NLS-1$
		assignNewPriceConfigButton.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				assignPriceConfigPressed();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		currencyAmountTableWrapper.addPriceConfigValueChangedListener(new IPriceConfigValueChangedListener() {
			public void priceValueChanged()
			{
				// if value has changed then fire change event.
				if(!orgPriceMap.equals(currencyAmountTableWrapper.getMap()))
				{
					markDirty();
				}
			}
		});
		

		stackLayout.topControl = currencyAmountTableWrapper;
		MenuManager menuManager = new MenuManager();
		menuManager.add(addCurrencyConfigAction);
		menuManager.add(removeCurrencyConfigAction);
		Menu menu = menuManager.createContextMenu(currencyAmountTableWrapper.getTable());
		getContainer().setMenu(menu);
		updateToolBarManager();
	}

	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);
		if (getVoucherPriceConfig() != null)
		{
			Map<Currency, Long> map = currencyAmountTableWrapper.getMap();
			VoucherPriceConfig actualVoucherConfig = getVoucherPriceConfig();
			for (Map.Entry<Currency, Long> me : map.entrySet()) {
				actualVoucherConfig.setPrice(me.getKey(), me.getValue());
			}
		}
	}

	protected void switchtoNewAssignPriceConfigPage()
	{
		stackLayout.topControl = assignNewPriceConfigWrapper;
		stackWrapper.layout(true, true);
	}

	protected void switchtoEditPriceConfigPage()
	{
		stackLayout.topControl = currencyAmountTableWrapper;
		stackWrapper.layout(true, true);
	}

	protected void inheritPressed() {
		if( inheritanceAction.isChecked() )
		{
			parentVoucherType =  VoucherTypeDAO.sharedInstance().getVoucherType(
					voucherType.getExtendedProductTypeID(),
					new String[] { FetchPlan.DEFAULT, 
						VoucherType.FETCH_GROUP_PACKAGE_PRICE_CONFIG,
						FetchGroupsPriceConfig.FETCH_GROUP_EDIT, 
						PriceConfig.FETCH_GROUP_NAME},
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
			voucherType.setPackagePriceConfig(parentVoucherType.getPackagePriceConfig());
		}
		else
			voucherType.setPackagePriceConfig(originalVoucherConfig);

		voucherType.getFieldMetaData(ProductType.FieldName.packagePriceConfig).setValueInherited( 
				!voucherType.getFieldMetaData(ProductType.FieldName.packagePriceConfig).isValueInherited());
		updatePricesTable();
		markDirty();
	}

	protected VoucherPriceConfig getVoucherPriceConfig()
	{
		if (voucherType.getPackagePriceConfig() == null)
			return null;

		if(voucherType.getPackagePriceConfig() instanceof VoucherPriceConfig)
		{
			VoucherPriceConfig	voucherConfigPrice = (VoucherPriceConfig) voucherType.getPackagePriceConfig();
			return voucherConfigPrice;
		}
		else
			throw new IllegalStateException("PriceConfig is not an instance of VoucherPriceConfig"); //$NON-NLS-1$
	}

	public void setVoucherType(VoucherType voucherType)
	{
		this.voucherType = voucherType;
		originalVoucherConfig = (VoucherPriceConfig) voucherType.getPackagePriceConfig();

		if (originalVoucherConfig == null)
			orgPriceMap = new HashMap<Currency, Long>();
		else
			orgPriceMap = originalVoucherConfig.getPrices();

		
		switchtoEditPriceConfigPage();
		updatePricesTable();

		inheritanceAction.setChecked(
				voucherType.getFieldMetaData(
						ProductType.FieldName.packagePriceConfig
				).isValueInherited()
		);
		inheritanceAction.setEnabled(voucherType.getExtendedProductTypeID() != null);
	}

	protected void updatePricesTable()
	{
		VoucherPriceConfig voucherPriceConfig = getVoucherPriceConfig();
		if(voucherPriceConfig != null)
			switchtoEditPriceConfigPage();
		else
		{
			switchtoNewAssignPriceConfigPage();
			return;
		}
		// "Price Configuration / "
		Map<Currency, Long> map = new HashMap<Currency, Long>(voucherPriceConfig.getPrices());
		currencyAmountTableWrapper.setMap(map);
		String str = String.format("%s - %s",Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.accountpriceconfig.VoucherPriceConfigSection.priceConfiguration"),voucherPriceConfig.getName().getText());				 //$NON-NLS-1$ //$NON-NLS-2$
		getSection().setText(str);
	}

	protected void addCurrencyPressed()
	{
		currencyAmountTableWrapper.addCurrency();
		markDirty();
	}

	protected void removeCurrencyPressed()
	{
		currencyAmountTableWrapper.removeCurrency();
		markDirty();
	}
	
	protected void assignPriceConfigPressed()
	{
		PriceVoucherTypeWizard priceVoucherTypeWizard = new PriceVoucherTypeWizard(voucherType.getExtendedProductTypeID() , voucherType);
		DynamicPathWizardDialog wizardDialog = new DynamicPathWizardDialog(priceVoucherTypeWizard);
		if( wizardDialog.open() == Window.OK)
		{
			inheritanceAction.setChecked(voucherType.getFieldMetaData(ProductType.FieldName.packagePriceConfig).isValueInherited());
			updatePricesTable();
			markDirty();
		}
	}

	class AssignPriceConfigAction
	extends Action
	{
		public AssignPriceConfigAction() {
			super();
			setId(AssignPriceConfigAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					VoucherAdminPlugin.getDefault(),
					VoucherPriceConfigSection.class,
			"AssignPriceConfig")); //$NON-NLS-1$
			setToolTipText(Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.price.VoucherPriceConfigSection.AssignPriceConfigActionText"));  //$NON-NLS-1$
			setText(Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.price.VoucherPriceConfigSection.AssignPriceConfigActionTooltip")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			assignPriceConfigPressed();
		}
	}

	class AddCurrencyConfigAction
	extends Action
	{
		public AddCurrencyConfigAction() {
			super();
			setId(AddCurrencyConfigAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					VoucherAdminPlugin.getDefault(),
					VoucherPriceConfigSection.class,
			"Add")); //$NON-NLS-1$
			setToolTipText(Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.price.VoucherPriceConfigSection.AddCurrencyConfigActionTooltip"));  //$NON-NLS-1$
			setText(Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.price.VoucherPriceConfigSection.AddCurrencyConfigActionText")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			addCurrencyPressed();
		}
	}

	class RemoveCurrencyConfigAction
	extends Action
	{
		public RemoveCurrencyConfigAction() {
			super();
			setId(RemoveCurrencyConfigAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					VoucherAdminPlugin.getDefault(),
					VoucherPriceConfigSection.class,
			"Remove")); //$NON-NLS-1$
			setToolTipText(Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.price.VoucherPriceConfigSection.RemoveCurrencyConfigActionTooltip"));  //$NON-NLS-1$
			setText(Messages.getString("org.nightlabs.jfire.voucher.admin.ui.editor.price.VoucherPriceConfigSection.RemoveCurrencyConfigActionText")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			removeCurrencyPressed();
		}
	}

}

