package org.nightlabs.jfire.voucher.admin.ui.editor;

import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.priceconfig.FetchGroupsPriceConfig;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeLocal;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.admin.ui.editor.AbstractProductTypeDetailPageController;
import org.nightlabs.jfire.voucher.VoucherManagerRemote;
import org.nightlabs.jfire.voucher.accounting.VoucherLocalAccountantDelegate;
import org.nightlabs.jfire.voucher.dao.VoucherTypeDAO;
import org.nightlabs.jfire.voucher.scripting.VoucherLayout;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class VoucherTypeDetailPageController
extends AbstractProductTypeDetailPageController<VoucherType>
{
	private VoucherLayout voucherLayout;

	/**
	 * @param editor
	 */
	public VoucherTypeDetailPageController(EntityEditor editor) {
		super(editor);
	}

	/**
	 * @param editor
	 * @param startBackgroundLoading
	 */
	public VoucherTypeDetailPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}

	public static final String[] FETCH_GROUPS_VOUCHER_TYPE = CollectionUtil.mergeArrays(
			FETCH_GROUPS_DEFAULT,
			new String[] {
					FetchGroupsPriceConfig.FETCH_GROUP_EDIT,
					VoucherType.FETCH_GROUP_PACKAGE_PRICE_CONFIG,
					VoucherType.FETCH_GROUP_INNER_PRICE_CONFIG,
					ProductType.FETCH_GROUP_PRODUCT_TYPE_LOCAL,
					ProductTypeLocal.FETCH_GROUP_LOCAL_ACCOUNTANT_DELEGATE,
					ProductTypeLocal.FETCH_GROUP_FIELD_METADATA_MAP,
					VoucherLocalAccountantDelegate.FETCH_GROUP_VOUCHER_LOCAL_ACCOUNTS,
					VoucherLocalAccountantDelegate.FETCH_GROUP_NAME,
					Account.FETCH_GROUP_NAME,
					Account.FETCH_GROUP_CURRENCY,
					VoucherType.FETCH_GROUP_VOUCHER_LAYOUT
			}
	);
	//	protected void createVoucherLayout(VoucherTypeDetailPage page)
	//	{
	//		if (page.getVoucherLayoutSection().getVoucherLayoutComposite() == null) // no UI created, yet
	//			return;
	//
	//		File selectedFile = page.getVoucherLayoutSection().getVoucherLayoutComposite().getSelectedFile();
	//		VoucherLayout voucherLayout = getVoucherType().getVoucherLayout();
	//		if (voucherLayout == null) {
	//			voucherLayout = new VoucherLayout(IDGenerator.getOrganisationID(),
	//					IDGenerator.nextID(VoucherLayout.class));
	//		}
	//		try {
	//			if (selectedFile != null) {
	//				voucherLayout.loadFile(selectedFile);
	//				voucherLayout.saveFile(selectedFile);
	//				getVoucherType().setVoucherLayout(voucherLayout);
	//				getVoucherType().getFieldMetaData(VoucherType.FieldName.voucherLayout).setValueInherited(false);
	//				// TODO: inheritance should be controllable by UI. Marco.
	//			}
	//		} catch (Exception e) {
	//			throw new RuntimeException(e);
	//		}
	//	}

	@Override
	protected String[] getEntityFetchGroups() {
		return FETCH_GROUPS_VOUCHER_TYPE;
	}

	@Override
	protected VoucherType retrieveProductType(ProgressMonitor monitor)
	{
		return VoucherTypeDAO.sharedInstance().getVoucherType(
				getProductTypeID(),
				getEntityFetchGroups(),
				getEntityMaxFetchDepth(),
				monitor);
	}

	@Override
	public VoucherType getExtendedProductType(ProgressMonitor monitor, ProductTypeID extendedProductTypeID)
	{
		return VoucherTypeDAO.sharedInstance().getVoucherType(
				extendedProductTypeID,
				getEntityFetchGroups(),
				getEntityMaxFetchDepth(),
				monitor);
	}

	@Override
	protected VoucherType storeProductType(VoucherType voucherType, ProgressMonitor monitor)
	{
		//		I'm not quite sure what this code here is supposed to do. But since I introduced the new handling of voucher layouts, where they
		//		first have to be uploaded to the server in order to assign them, I think this code here is obsolete now. Tobias.
		//
		//		// TODO: WORKAROUND: Why is the access to the page here ?!? Alex
		//		for (IFormPage page : getPages()) {
		//			if (page instanceof VoucherTypeDetailPage) {
		//				createVoucherLayout((VoucherTypeDetailPage) page);
		//			}
		//		}

		if (voucherLayout != null) {
			voucherType.setVoucherLayout(voucherLayout);
			voucherType.getFieldMetaData(VoucherType.FieldName.voucherLayout).setValueInherited(false);
			// TODO Inheritance should be controllable by UI. Tobias
		}

		try {
			VoucherManagerRemote voucherManager = JFireEjb3Factory.getRemoteBean(VoucherManagerRemote.class, Login.getLogin().getInitialContextProperties());
			VoucherType newVoucherType = voucherManager.storeVoucherType(voucherType, true, getEntityFetchGroups(), NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			return newVoucherType;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	protected VoucherType getVoucherType() {
		return getProductType();
	}

	public void setVoucherLayout(VoucherLayout selectedLayout) {
		this.voucherLayout = selectedLayout;
	}
}
