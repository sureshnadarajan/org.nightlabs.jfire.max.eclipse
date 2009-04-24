package org.nightlabs.jfire.issuetimetracking.admin.ui;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.composite.ListComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.dao.PriceFragmentTypeDAO;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issuetimetracking.ProjectCost;
import org.nightlabs.jfire.issuetimetracking.ProjectCostValue;
import org.nightlabs.jfire.security.User;
import org.nightlabs.progress.NullProgressMonitor;

/** 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class UserCostSection 
extends ToolBarSectionPart 
{
	private XComposite client;
	private ListComposite<User> userList;
	private Text costText;
	private Text revenueText;

	private ProjectCostValue currentProjectCostValue;
	private ProjectCostEditorPageController controller;
	/**
	 * @param page
	 * @param parent
	 * @param style
	 * @param title
	 */
	public UserCostSection(FormPage page, Composite parent, ProjectCostEditorPageController controller) {
		super(
				page, parent, 
				ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE,
		"User Cost");
		this.controller = controller;
		getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
		getSection().setLayout(new GridLayout());

		client = new XComposite(getSection(), SWT.NONE);
		client.getGridLayout().numColumns = 1; 
		GridData gridData = new GridData(GridData.FILL_BOTH);
		client.setLayoutData(gridData);

		//User List
		XComposite userComposite = new XComposite(client, SWT.NONE);
		userComposite.getGridLayout().numColumns = 2;
		gridData = new GridData(GridData.FILL_BOTH);
		userComposite.setLayoutData(gridData);

		userList = new ListComposite<User>(userComposite, SWT.NONE);
		userList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof User) {
					User user = (User) element;
					return user.getName();
				}
				return "";
			}
		});

		userList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				User selectedUser = userList.getSelectedElement();
				String userID = selectedUser.getUserID();

				currentProjectCostValue = projectCost.getProjectCostValue(userID);
				if (currentProjectCostValue == null) {
					createProjectCostValue(userID);
//					currentProjectCostValue = new ProjectCostValue(projectCost, IDGenerator.nextID(ProjectCostValue.class));
//
//					if (priceFragmentType == null)
//						priceFragmentType =  
//							PriceFragmentTypeDAO.sharedInstance().getPriceFragmentType(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL,
//									new String[] { FetchPlan.DEFAULT}, 
//									NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
//									new NullProgressMonitor());
//					currentProjectCostValue.getCost().setAmount(priceFragmentType, projectCost.getDefaultCost().getAmount());
//					currentProjectCostValue.getRevenue().setAmount(priceFragmentType, projectCost.getDefaultRevenue().getAmount());
//					projectCost.addProjectCostValue(userID, currentProjectCostValue);
				}
				costText.setText(Long.toString(currentProjectCostValue.getCost().getAmount()));
				revenueText.setText(Long.toString(currentProjectCostValue.getRevenue().getAmount()));
			}
		});

		gridData = new GridData(GridData.FILL_BOTH);
		userList.setLayoutData(gridData);

		XComposite c = new XComposite(userComposite, SWT.NONE);
		c.getGridLayout().numColumns = 2;

		//Cost
		Label monthlyCostLabel = new Label(c, SWT.NONE);
		monthlyCostLabel.setText("Hourly Cost");
		costText = new Text(c, SWT.SINGLE);
		costText.setEnabled(false);
		costText.addListener (SWT.Verify, new Listener () {
			public void handleEvent (Event e) {
				String string = e.text;
				char [] chars = new char [string.length ()];
				string.getChars (0, chars.length, chars, 0);
				for (int i=0; i<chars.length; i++) {
					if (!('0' <= chars [i] && chars [i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});

		costText.setTextLimit(20);
		costText.addKeyListener(keyListener);
		gridData = new GridData();
		gridData.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
		gridData.widthHint = 150;
		gridData.verticalIndent = 5;
		costText.setLayoutData(gridData);

		//Revenue
		Label monthlyRevenueLabel = new Label(c, SWT.NONE);
		monthlyRevenueLabel.setText("Hourly Revenue");
		revenueText = new Text(c, SWT.SINGLE);
		revenueText.setTextLimit(20);
		revenueText.setEnabled(false);
		revenueText.addKeyListener(keyListener);
		revenueText.addListener (SWT.Verify, new Listener () {
			public void handleEvent (Event e) {
				String string = e.text;
				char [] chars = new char [string.length ()];
				string.getChars (0, chars.length, chars, 0);
				for (int i=0; i<chars.length; i++) {
					if (!('0' <= chars [i] && chars [i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});

		gridData = new GridData();
		gridData.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
		gridData.widthHint = 150;
		gridData.verticalIndent = 5;
		revenueText.setLayoutData(gridData);

		getSection().setClient(client);
	}

	public XComposite getClient() {
		return client;
	}

	private PriceFragmentType priceFragmentType;

	private KeyAdapter keyListener = new KeyAdapter() {
		public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
			if (priceFragmentType == null)
				priceFragmentType =  
					PriceFragmentTypeDAO.sharedInstance().getPriceFragmentType(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL,
							new String[] { FetchPlan.DEFAULT}, 
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
							new NullProgressMonitor());

			if (currentProjectCostValue != null) {
				if (e.getSource() == costText)
					currentProjectCostValue.getCost().setAmount(priceFragmentType, Long.parseLong(costText.getText().isEmpty() ? "0" : costText.getText()));
				if (e.getSource() == revenueText)
					currentProjectCostValue.getRevenue().setAmount(priceFragmentType, Long.parseLong(revenueText.getText().isEmpty() ? "0" : costText.getText()));
			}
			markDirty();
		};
	};

	//	private ModifyListener modifyListener = new ModifyListener() {
	//		public void modifyText(ModifyEvent e) {
	//			if (priceFragmentType == null)
	//				priceFragmentType =  
	//					PriceFragmentTypeDAO.sharedInstance().getPriceFragmentType(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL,
	//							new String[] { FetchPlan.DEFAULT}, 
	//							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
	//							new NullProgressMonitor());
	//
	//			if (e.getSource() == costText)
	//				currentProjectCostValue.getCost().setAmount(priceFragmentType, Long.parseLong(costText.getText()));
	//			if (e.getSource() == revenueText)
	//				currentProjectCostValue.getRevenue().setAmount(priceFragmentType, Long.parseLong(revenueText.getText()));
	//			
	//			markDirty();
	//		}
	//	};
	//	private UserID selectedUserID;
	//	public void setSelectedUserID(UserID userID) {
	//		this.selectedUserID = userID;
	//		ProjectCostValue projectCostValue = projectCost.getProjectCostValue(userID.userID);
	//		costText.setText(Double.toString(projectCostValue.getCost().getAmountAsDouble()));
	//		revenueText.setText(Double.toString(projectCostValue.getRevenue().getAmountAsDouble()));
	//	}
	//	
	//	public UserID getSelectedUserID() {
	//		return selectedUserID;
	//	}

	private ProjectCost projectCost;
	public void setProjectCost(final ProjectCost projectCost) {
		this.projectCost = projectCost;

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				userList.setInput(projectCost.getProject().getMembers());		
				if (!projectCost.getProject().getMembers().isEmpty()) {
					userList.selectElementByIndex(0);
					User user = userList.getSelectedElement();
					costText.setEnabled(true);
					revenueText.setEnabled(true);
					ProjectCostValue projectCostValue = projectCost.getProjectCostValue(user.getUserID());
					
					if (projectCostValue == null)
						projectCostValue = createProjectCostValue(user.getUserID());
					costText.setText(Long.toString(projectCostValue.getCost().getAmount()));
					revenueText.setText(Long.toString(projectCostValue.getRevenue().getAmount()));
				}
			}
		});
	}

	private ProjectCostValue createProjectCostValue(String userID) {
		currentProjectCostValue = new ProjectCostValue(projectCost, IDGenerator.nextID(ProjectCostValue.class));

		if (priceFragmentType == null)
			priceFragmentType =  
				PriceFragmentTypeDAO.sharedInstance().getPriceFragmentType(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL,
						new String[] { FetchPlan.DEFAULT}, 
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
						new NullProgressMonitor());
		currentProjectCostValue.getCost().setAmount(priceFragmentType, projectCost.getDefaultCost().getAmount());
		currentProjectCostValue.getRevenue().setAmount(priceFragmentType, projectCost.getDefaultRevenue().getAmount());
		projectCost.addProjectCostValue(userID, currentProjectCostValue);
		
		return currentProjectCostValue;
	}
}