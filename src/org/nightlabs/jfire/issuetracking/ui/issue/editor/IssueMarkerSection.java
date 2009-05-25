package org.nightlabs.jfire.issuetracking.ui.issue.editor;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.issuemarker.IssueMarker;
import org.nightlabs.jfire.issuetracking.ui.issue.editor.issueMarker.AddIssueMarkerWizard;
import org.nightlabs.jfire.issuetracking.ui.issue.editor.issueMarker.IssueMarkerTable;

/**
 * A section for the {@link IssueEditorGeneralPage} to handle the interface mechanisms for the
 * {@link IssueMarker}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueMarkerSection extends AbstractIssueEditorGeneralSection {
	private AddIssueMarkerAction addIssueMarkerAction;
	private RemoveIssueMarkerAction removeIssueMarkerAction;
	private IssueMarkerTable issueMarkerTable;

	public IssueMarkerSection(FormPage page, Composite parent, final IssueEditorPageController controller) {
		super(page, parent, controller);
		getSection().setText("Issue Markers");
		getSection().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getSection().setLayout(new GridLayout());

		XComposite client = new XComposite(getSection(), SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		client.getGridLayout().numColumns = 1;


		// Top set of control buttons: To add and remove IssueMarkers.
		addIssueMarkerAction = new AddIssueMarkerAction();
		getToolBarManager().add(addIssueMarkerAction);

		removeIssueMarkerAction = new RemoveIssueMarkerAction();
		getToolBarManager().add(removeIssueMarkerAction);



		// The table displaying the IssueMarkers.
		IssueMarkerTableComposite imtComposite = new IssueMarkerTableComposite(client, SWT.NONE);
		issueMarkerTable = imtComposite.getIssueMarkerTable();	// <-- FOR LATER: Consider handling RIGHT-CLICK here; with a popup menu enabling Add and Remove actions.
		issueMarkerTable.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent event) { handleRemoveActionButton(); }
		});


		getSection().setClient(client);
		updateToolBarManager();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issuetracking.ui.issue.editor.AbstractIssueEditorGeneralSection#doSetIssue(org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	protected void doSetIssue(Issue issue) {
		Set<IssueMarker> iMs = issue.getIssueMarkers();
		boolean isMarkersExist = iMs != null && !iMs.isEmpty();
		if (isMarkersExist)
			issueMarkerTable.setInput(iMs);

		getSection().setExpanded(isMarkersExist);
		handleRemoveActionButton();
	}

	/**
	 * Disables the Remove button if nothing on the table is selected.
	 */
	protected void handleRemoveActionButton() {
		assert removeIssueMarkerAction != null;
		removeIssueMarkerAction.setEnabled( issueMarkerTable.getSelectionIndex() >= 0 );
	}


	// -----------------------------------------------------------------------------------------------------------------------------------|
	/**
	 *  Setup more control for the TableComposite in this Section.
	 *  This seems enough, and we dont have to have anything more elaborate.
	 */
	private class IssueMarkerTableComposite extends XComposite {
		private IssueMarkerTable issueMarkerTable;

		public IssueMarkerTableComposite(Composite parent, int style) {
			super(parent, style, LayoutMode.TIGHT_WRAPPER);

			// Prepare the wrapper, and sew in the Table.
			getGridLayout().numColumns = 2;
			getGridLayout().makeColumnsEqualWidth = false;
			getGridData().grabExcessHorizontalSpace = true;

			GridData gridData = new GridData(GridData.FILL_BOTH);
			gridData.heightHint = 100;

			issueMarkerTable = new IssueMarkerTable(this);
			issueMarkerTable.setLayoutData(gridData);
		}

		public IssueMarkerTable getIssueMarkerTable() { return issueMarkerTable; }
	}


	// -----------------------------------------------------------------------------------------------------------------------------------|
	/**
	 * Handles the action to add a new IssueMarker.
	 */
	private class AddIssueMarkerAction extends Action {
		private Issue issue;
		public AddIssueMarkerAction() {
			setId(AddIssueMarkerAction.class.getName());
			setImageDescriptor(SharedImages.ADD_16x16);
			setToolTipText("Mark issue");
			setText("Mark issue");
		}

		@Override
		public void run() {
			issue = getIssue();
			DynamicPathWizardDialog dialog = new DynamicPathWizardDialog( new AddIssueMarkerWizard(issue) );
			dialog.open();

			// React only when something was chosen from the Wizard.
			if (dialog.getReturnCode() != Window.CANCEL) {
				// Handles the case that the Table was previously empty.
				if (issueMarkerTable.getItemCount() == 0) {
					issueMarkerTable.setInput(issue.getIssueMarkers());
					getSection().setExpanded(true);
				}

				issueMarkerTable.refresh(true);
				markDirty();
			}
		}
	}


	// -----------------------------------------------------------------------------------------------------------------------------------|
	/**
	 * Handles the action to remove the selected IssueMarker(s).
	 */
	private class RemoveIssueMarkerAction extends Action {
		private Issue issue;
		public RemoveIssueMarkerAction() {
			setId(RemoveIssueMarkerAction.class.getName());
			setImageDescriptor(SharedImages.DELETE_16x16);
			setToolTipText("Delete currently selected issue marker");
			setText("Delete issue marker");
		}

		@Override
		public void run() {
			Collection<IssueMarker> items = issueMarkerTable.getSelectedElements();
			if (items.isEmpty()) return;

			issue = getIssue();
			for(IssueMarker issueMarker : items) {
				issueMarkerTable.removeElement(issueMarker);
				issue.removeIssueMarker(issueMarker);
			}

			// Done!
			markDirty();
		}
	}

}
