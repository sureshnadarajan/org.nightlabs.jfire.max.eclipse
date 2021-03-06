package org.nightlabs.jfire.trade.ui.overview;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.DateTimeEdit;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery.FieldChangeCarrier;
import org.nightlabs.jdo.query.QueryEvent;
import org.nightlabs.jdo.query.QueryProvider;
import org.nightlabs.jfire.base.ui.search.AbstractQueryFilterComposite;
import org.nightlabs.jfire.base.ui.security.UserSearchDialog;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.dao.ProductTypeDAO;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.dao.LegalEntityDAO;
import org.nightlabs.jfire.trade.query.AbstractArticleContainerQuery;
import org.nightlabs.jfire.trade.ui.legalentity.edit.LegalEntitySearchCreateWizard;
import org.nightlabs.jfire.trade.ui.resource.Messages;
import org.nightlabs.jfire.trade.ui.store.search.GenericProductTypeSearchDialog;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.l10n.IDateFormatter;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractArticleContainerFilterComposite<Q extends AbstractArticleContainerQuery>
extends AbstractQueryFilterComposite<Q>
{
	private static final String ARTICLE_CONTAINER_GROUP_ID = "ArticleContainerFilterComposite"; //$NON-NLS-1$

	private DateTimeEdit createDateAfter;
	private DateTimeEdit createDateBefore;

	private Button userActiveButton;
	private Text userText;
	private Button userBrowseButton;

	private Button vendorActiveButton;
	private Text vendorText;
	private Button vendorBrowseButton;

	private Button customerActiveButton;
	private Text customerText;
	private Button customerBrowseButton;

	private Button productTypeActiveButton;
	private Text productTypeText;
	private Button productTypeBrowseButton;

	

	/**
	 * @param parent
	 *          The parent to instantiate this filter into.
	 * @param style
	 *          The style to apply.
	 * @param layoutMode
	 *          The layout mode to use. See {@link XComposite.LayoutMode}.
	 * @param layoutDataMode
	 *          The layout data mode to use. See {@link XComposite.LayoutDataMode}.
	 * @param queryProvider
	 *          The queryProvider to use. It may be <code>null</code>, but the caller has to
	 *          ensure, that it is set before {@link #getQuery()} is called!
	 */
	public AbstractArticleContainerFilterComposite(final Composite parent, final int style,
			final LayoutMode layoutMode, final LayoutDataMode layoutDataMode,
			final QueryProvider<? super Q> queryProvider)
	{
		super(parent, style, layoutMode, layoutDataMode, queryProvider);
		createComposite();
	}

	/**
	 * @param parent
	 *          The parent to instantiate this filter into.
	 * @param style
	 *          The style to apply.
	 * @param queryProvider
	 *          The queryProvider to use. It may be <code>null</code>, but the caller has to
	 *          ensure, that it is set before {@link #getQuery()} is called!
	 */
	public AbstractArticleContainerFilterComposite(final Composite parent, final int style,
			final QueryProvider<? super Q> queryProvider)
	{
		super(parent, style, queryProvider);
		createComposite();
	}

	@Override
	protected void createComposite()
	{
		final Group createDTGroup = new Group(this, SWT.NONE);
		createDTGroup.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.createDateGroup.text")); //$NON-NLS-1$
		createDTGroup.setLayout(new GridLayout(2, true));
		createDTGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createQueryDateOptionsRow(createDTGroup);
		XComposite wrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER,
				LayoutDataMode.GRID_DATA_HORIZONTAL, 4);
		createQueryUserOptionsRow(wrapper);
	}
	
	protected void createQueryDateOptionsRow(Group createDTGroup)
	{

		final long dateTimeEditStyle = IDateFormatter.FLAGS_DATE_SHORT_TIME_HMS_WEEKDAY + DateTimeEdit.FLAGS_SHOW_ACTIVE_CHECK_BOX;
		createDateAfter = new DateTimeEdit(createDTGroup, dateTimeEditStyle, Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.createDateMin.caption")); //$NON-NLS-1$
		createDateAfter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createDateAfter.setActive(false);
		createDateAfter.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				getQuery().setCreateDTMin(createDateAfter.getDate());
			}
		});
		createDateAfter.addActiveChangeListener(new ButtonSelectionListener()
		{
			@Override
			protected void handleSelection(final boolean active)
			{
				getQuery().setFieldEnabled(AbstractArticleContainerQuery.FieldName.createDTMin, active);
			}
		});
		createDateBefore = new DateTimeEdit(createDTGroup, dateTimeEditStyle, Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.createDateMax.caption")); //$NON-NLS-1$
		createDateBefore.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createDateBefore.setActive(false);
		createDateBefore.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				getQuery().setCreateDTMax(createDateBefore.getDate());
			}
		});
		createDateBefore.addActiveChangeListener(new ButtonSelectionListener()
		{
			@Override
			protected void handleSelection(final boolean active)
			{
				getQuery().setFieldEnabled(AbstractArticleContainerQuery.FieldName.createDTMax, active);
			}
		});
	}
	
	protected void createQueryUserOptionsRow(XComposite wrapper)
	{
		final int buttonStyle = SWT.FLAT;
		final Group userGroup = new Group(wrapper, SWT.NONE);
		userGroup.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.userGroup.text")); //$NON-NLS-1$
		userGroup.setLayout(new GridLayout(2, false));
		userGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		userActiveButton = new Button(userGroup, SWT.CHECK);
		userActiveButton.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.userActiveButton.text")); //$NON-NLS-1$
		final GridData userLabelData = new GridData(GridData.FILL_HORIZONTAL);
		userLabelData.horizontalSpan = 2;
		userActiveButton.setLayoutData(userLabelData);
		userActiveButton.addSelectionListener(new ButtonSelectionListener()
		{
			@Override
			protected void handleSelection(final boolean active)
			{
				getQuery().setFieldEnabled(AbstractArticleContainerQuery.FieldName.createUserID, active);
			}
		});
		userText = new Text(userGroup, SWT.READ_ONLY | getBorderStyle());
		userText.setEnabled(false);
		userText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		userText.addSelectionListener(userSelectionListener);
		userBrowseButton = new Button(userGroup, buttonStyle);
		userBrowseButton.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.userBrowseButton.text")); //$NON-NLS-1$
		userBrowseButton.addSelectionListener(userSelectionListener);
		userBrowseButton.setEnabled(false);

		final Group vendorGroup = new Group(wrapper, SWT.NONE);
		vendorGroup.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.vendorGroup.text")); //$NON-NLS-1$
		vendorGroup.setLayout(new GridLayout(2, false));
		vendorGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		vendorActiveButton = new Button(vendorGroup, SWT.CHECK);
		vendorActiveButton.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.vendorActiveButton.text")); //$NON-NLS-1$
		final GridData vendorLabelData = new GridData(GridData.FILL_HORIZONTAL);
		vendorLabelData.horizontalSpan = 2;
		vendorActiveButton.setLayoutData(vendorLabelData);
		vendorActiveButton.addSelectionListener(new ButtonSelectionListener()
		{
			@Override
			protected void handleSelection(final boolean active)
			{
				getQuery().setFieldEnabled(AbstractArticleContainerQuery.FieldName.vendorID, active);
			}
		});
		vendorText = new Text(vendorGroup, SWT.READ_ONLY | getBorderStyle());
		vendorText.setEnabled(false);
		vendorText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		vendorText.addSelectionListener(vendorSelectionListener);
		vendorBrowseButton = new Button(vendorGroup, buttonStyle);
		vendorBrowseButton.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.vendorBrowseButton.text")); //$NON-NLS-1$
		vendorBrowseButton.addSelectionListener(vendorSelectionListener);
		vendorBrowseButton.setEnabled(false);

		final Group customerGroup = new Group(wrapper, SWT.NONE);
		customerGroup.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.customerGroup.text")); //$NON-NLS-1$
		customerGroup.setLayout(new GridLayout(2, false));
		customerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		customerActiveButton = new Button(customerGroup, SWT.CHECK);
		customerActiveButton.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.customerActiveButton.text")); //$NON-NLS-1$
		final GridData customerLabelData = new GridData(GridData.FILL_HORIZONTAL);
		customerLabelData.horizontalSpan = 2;
		customerActiveButton.setLayoutData(customerLabelData);
		customerText = new Text(customerGroup, SWT.READ_ONLY | getBorderStyle());
		customerText.setEnabled(false);
		customerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		customerText.addSelectionListener(customerSelectionListener);
		customerBrowseButton = new Button(customerGroup, buttonStyle);
		customerBrowseButton.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.customerBrowseButton.text")); //$NON-NLS-1$
		customerBrowseButton.addSelectionListener(customerSelectionListener);
		customerBrowseButton.setEnabled(false);
		customerActiveButton.addSelectionListener(new ButtonSelectionListener()
		{
			@Override
			protected void handleSelection(final boolean active)
			{
				getQuery().setFieldEnabled(AbstractArticleContainerQuery.FieldName.customerID, active);
			}
		});

		final Group productTypeGroup = new Group(wrapper, SWT.NONE);
		productTypeGroup.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.AbstractArticleContainerFilterComposite.group.text")); //$NON-NLS-1$
		productTypeGroup.setLayout(new GridLayout(2, false));
		productTypeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		productTypeActiveButton = new Button(productTypeGroup, SWT.CHECK);
		productTypeActiveButton.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.customerActiveButton.text")); //$NON-NLS-1$
		final GridData productTypeLabelData = new GridData(GridData.FILL_HORIZONTAL);
		productTypeLabelData.horizontalSpan = 2;
		productTypeActiveButton.setLayoutData(productTypeLabelData);
		productTypeText = new Text(productTypeGroup, SWT.READ_ONLY | getBorderStyle());
		productTypeText.setEnabled(false);
		productTypeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		productTypeText.addSelectionListener(productTypeSelectionListener);
		productTypeBrowseButton = new Button(productTypeGroup, buttonStyle);
		productTypeBrowseButton.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.ArticleContainerFilterComposite.customerBrowseButton.text")); //$NON-NLS-1$
		productTypeBrowseButton.addSelectionListener(productTypeSelectionListener);
		productTypeBrowseButton.setEnabled(false);
		productTypeActiveButton.addSelectionListener(new ButtonSelectionListener()
		{
			@Override
			protected void handleSelection(final boolean active)
			{
				getQuery().setFieldEnabled(AbstractArticleContainerQuery.FieldName.productTypeID, active);
			}
		});		
	}
	
	private final SelectionListener productTypeSelectionListener = new SelectionAdapter()
	{
		@Override
		public void widgetSelected(final SelectionEvent e)
		{
			final GenericProductTypeSearchDialog dialog = new GenericProductTypeSearchDialog(
					getShell());
			final int returnCode = dialog.open();
			if (returnCode == Window.OK) {
				final ProductType productType = dialog.getProductType();
				if (productType != null) {
					final ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(productType);
					getQuery().setProductTypeID(productTypeID);
					productTypeText.setText(productType.getName().getText());
				}
			}
		}
	};

	private final SelectionListener userSelectionListener = new SelectionAdapter()
	{
		@Override
		public void widgetSelected(final SelectionEvent e)
		{
			final UserSearchDialog dialog = new UserSearchDialog(getShell(), userText.getText());
			final int returnCode = dialog.open();
			if (returnCode == Window.OK) {
				final User selectedUser = dialog.getSelectedUser();
				final UserID selectedUserID = (UserID) JDOHelper.getObjectId(selectedUser);
				getQuery().setCreateUserID(selectedUserID);
				if (selectedUser != null)
					userText.setText(selectedUser.getName());
			}
		}
	};

	private final SelectionListener vendorSelectionListener = new SelectionAdapter()
	{
		@Override
		public void widgetSelected(final SelectionEvent e)
		{
			final LegalEntity _legalEntity = LegalEntitySearchCreateWizard.open(vendorText.getText(), false);
			if (_legalEntity != null) {
				final AnchorID selectedVendorID = (AnchorID) JDOHelper.getObjectId(_legalEntity);
				getQuery().setVendorID(selectedVendorID);
				final LegalEntity legalEntity = LegalEntityDAO.sharedInstance().getLegalEntity(selectedVendorID,
						new String[] {LegalEntity.FETCH_GROUP_PERSON, FetchPlan.DEFAULT},
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new NullProgressMonitor()
				);
				vendorText.setText(legalEntity.getPerson().getDisplayName());
			}
		}
	};

	private final SelectionListener customerSelectionListener = new SelectionAdapter()
	{
		@Override
		public void widgetSelected(final SelectionEvent e)
		{
			final LegalEntity _legalEntity = LegalEntitySearchCreateWizard.open(customerText.getText(), false);
			if (_legalEntity != null) {
				final AnchorID selectedCustomerID = (AnchorID) JDOHelper.getObjectId(_legalEntity);
				getQuery().setCustomerID(selectedCustomerID);
				final LegalEntity legalEntity = LegalEntityDAO.sharedInstance().getLegalEntity(selectedCustomerID,
						new String[] {LegalEntity.FETCH_GROUP_PERSON, FetchPlan.DEFAULT},
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new NullProgressMonitor()
				);
				if (legalEntity.getPerson() != null && legalEntity.getPerson().getDisplayName() != null)
					customerText.setText(legalEntity.getPerson().getDisplayName());
			}
		}
	};

	/**
	 * The names of the fields used in this filter.
	 */
	private static Set<String> fieldNames;
	static
	{
		fieldNames = new HashSet<String>(5);
		fieldNames.add(AbstractArticleContainerQuery.FieldName.createDTMax);
		fieldNames.add(AbstractArticleContainerQuery.FieldName.createDTMin);
		fieldNames.add(AbstractArticleContainerQuery.FieldName.createUserID);
		fieldNames.add(AbstractArticleContainerQuery.FieldName.customerID);
		fieldNames.add(AbstractArticleContainerQuery.FieldName.vendorID);
		fieldNames.add(AbstractArticleContainerQuery.FieldName.productTypeID);
	}

	@Override
	protected Set<String> getFieldNames()
	{
		return fieldNames;
	}

	@Override
	protected String getGroupID()
	{
		return ARTICLE_CONTAINER_GROUP_ID;
	}

	@Override
	protected void updateUI(final QueryEvent event, final List<FieldChangeCarrier> changedFields)
	{
		for (final FieldChangeCarrier changedField : changedFields)
		{
			final String propertyName = changedField.getPropertyName();
			if (AbstractArticleContainerQuery.FieldName.createDTMax.equals(propertyName))
			{
				final Date maxDate = (Date) changedField.getNewValue();
				createDateBefore.setDate(maxDate);
			}
			else if (getEnableFieldName(AbstractArticleContainerQuery.FieldName.createDTMax).equals(propertyName))
			{
				final boolean newActiveState = (Boolean) changedField.getNewValue();
				if (createDateBefore.isActive() != newActiveState)
				{
					createDateBefore.setActive(newActiveState);
					setSearchSectionActive(newActiveState);
				}
			}
			else if (AbstractArticleContainerQuery.FieldName.createDTMin.equals(propertyName))
			{
				final Date minDate = (Date) changedField.getNewValue();
				createDateAfter.setDate(minDate);
			}
			else if (AbstractSearchQuery.getEnabledFieldName(
					AbstractArticleContainerQuery.FieldName.createDTMin).equals(propertyName))
			{
				final boolean active = (Boolean) changedField.getNewValue();
				if (createDateAfter.isActive() != active)
				{
					createDateAfter.setActive(active);
					setSearchSectionActive(active);
				}
			}
			else if (AbstractArticleContainerQuery.FieldName.createUserID.equals(propertyName))
			{
				final UserID userID = (UserID) changedField.getNewValue();
				if (userID == null)
				{
					userText.setText(""); //$NON-NLS-1$
				}
				else
				{
					final User selectedUser = UserDAO.sharedInstance().getUser(
							userID, new String[] { FetchPlan.DEFAULT },
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor()
					);

					if (selectedUser != null) {
						userText.setText(selectedUser.getName());
					}
				}
			}
			else if (AbstractSearchQuery.getEnabledFieldName(
					AbstractArticleContainerQuery.FieldName.createUserID).equals(propertyName))
			{
				final boolean active = (Boolean) changedField.getNewValue();
				userText.setEnabled(active);
				userBrowseButton.setEnabled(active);
				setSearchSectionActive(userActiveButton, active);
			}
			else if (AbstractArticleContainerQuery.FieldName.customerID.equals(propertyName))
			{
				final AnchorID customerID = (AnchorID) changedField.getNewValue();
				if (customerID == null)
				{
					customerText.setText(""); //$NON-NLS-1$
				}
				else
				{
					final LegalEntity customer = LegalEntityDAO.sharedInstance().getLegalEntity(
							customerID, new String[] {LegalEntity.FETCH_GROUP_PERSON, FetchPlan.DEFAULT},
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor()
					);

					customerText.setText(customer.getPerson().getDisplayName());
				}
			}
			else if (AbstractSearchQuery.getEnabledFieldName(
					AbstractArticleContainerQuery.FieldName.customerID).equals(propertyName))
			{
				final boolean active = (Boolean) changedField.getNewValue();
				customerText.setEnabled(active);
				customerBrowseButton.setEnabled(active);
				setSearchSectionActive(customerActiveButton, active);
			}
			else if (AbstractArticleContainerQuery.FieldName.vendorID.equals(propertyName))
			{
				final AnchorID vendorID = (AnchorID) changedField.getNewValue();
				if (vendorID == null)
				{
					vendorText.setText(""); //$NON-NLS-1$
				}
				else
				{
					final LegalEntity vendor = LegalEntityDAO.sharedInstance().getLegalEntity(
							vendorID, new String[] {LegalEntity.FETCH_GROUP_PERSON, FetchPlan.DEFAULT},
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor()
					);

					vendorText.setText(vendor.getPerson().getDisplayName());
				}
			}
			else if (AbstractSearchQuery.getEnabledFieldName(
					AbstractArticleContainerQuery.FieldName.vendorID).equals(propertyName))
			{
				final boolean active = (Boolean) changedField.getNewValue();
				vendorText.setEnabled(active);
				vendorBrowseButton.setEnabled(active);
				setSearchSectionActive(vendorActiveButton, active);
			}
			else if (AbstractArticleContainerQuery.FieldName.productTypeID.equals(propertyName))
			{
				final ProductTypeID productTypeID = (ProductTypeID) changedField.getNewValue();
				if (productTypeID == null)
				{
					productTypeText.setText(""); //$NON-NLS-1$
				}
				else
				{
					final ProductType productType = ProductTypeDAO.sharedInstance().getProductType(
							productTypeID, new String[] {FetchPlan.DEFAULT, ProductType.FETCH_GROUP_NAME},
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor()
					);

					productTypeText.setText(productType.getName().getText());
				}
			}
			else if (AbstractSearchQuery.getEnabledFieldName(
					AbstractArticleContainerQuery.FieldName.productTypeID).equals(propertyName))
			{
				final boolean active = (Boolean) changedField.getNewValue();
				productTypeText.setEnabled(active);
				productTypeBrowseButton.setEnabled(active);
				setSearchSectionActive(productTypeActiveButton, active);
			}
		} // for (FieldChangeCarrier changedField : event.getChangedFields())
	}

}
