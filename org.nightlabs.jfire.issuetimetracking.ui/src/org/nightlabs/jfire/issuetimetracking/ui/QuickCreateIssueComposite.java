package org.nightlabs.jfire.issuetimetracking.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.DateTimeControl;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.custom.XCombo;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.I18nTextEditorMultiLine;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.timelength.TimeLengthComposite;
import org.nightlabs.i18n.I18nTextBuffer;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.department.prop.DepartmentDataField;
import org.nightlabs.jfire.department.ui.DepartmentComboComposite;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issuetimetracking.IssueTimeTrackingStruct;
import org.nightlabs.jfire.issuetimetracking.ui.resource.Messages;
import org.nightlabs.jfire.issuetracking.ui.issue.IssueTypeCombo;
import org.nightlabs.jfire.issuetracking.ui.project.ProjectComboComposite;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.dao.StructDAO;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.l10n.DateFormatter;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.NLLocale;

/**
 * A composite that contains UIs for adding {@link Issue}.
 *
 * @author Chairat Kongarayawetchakun - chairat[at]nightlabs[dot]de
 */
public class QuickCreateIssueComposite
extends XComposite
{
	private	StructID issueStructID;
	private IStruct issueStruct;
	private DepartmentDataField departmentDataField;

	private CreateIssueQuickDialog createIssueQuickDialog;
	/**
	 * Contructs a composite used for adding {@link Issue}.
	 *
	 * @param parent -the parent composite
	 * @param style - the SWT style flag
	 */
	public QuickCreateIssueComposite(CreateIssueQuickDialog createIssueQuickDialog, Composite parent, int style) {
		super(parent, style, LayoutMode.TIGHT_WRAPPER);
		this.createIssueQuickDialog = createIssueQuickDialog;

		issueStructID = StructID.create(Organisation.DEV_ORGANISATION_ID, Issue.class, Struct.DEFAULT_SCOPE);
		Job job = new Job(Messages.getString("org.nightlabs.jfire.issuetimetracking.ui.QuickCreateIssueComposite.loadingIssueStruct")){ //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				issueStruct = StructDAO.sharedInstance().getStruct(issueStructID,
				        new NullProgressMonitor());
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();

		initData();
		createComposite();
		initUI();
	}

	private IssueTypeCombo issueTypeCombo;
	private ProjectComboComposite projectComboComposite;
	private DepartmentComboComposite departmentComboComposite;
	private DateTimeControl startDateControl;
	private DateTimeControl startTimeControl;
	private TimeLengthComposite durationText;
	private I18nTextEditor subjectText;
	private I18nTextEditorMultiLine descriptionText;
	private Issue newIssue;

	private void createComposite() {
		getGridLayout().numColumns = 1;
		getGridLayout().makeColumnsEqualWidth = false;
		getGridData().grabExcessHorizontalSpace = true;

		XComposite mainComposite = new XComposite(this, SWT.NONE,
				LayoutMode.TIGHT_WRAPPER);
		
		int numCols = 6;
		
		mainComposite.getGridLayout().numColumns = numCols;

		issueTypeCombo = new IssueTypeCombo(mainComposite, SWT.BORDER | SWT.READ_ONLY, Messages.getString("org.nightlabs.jfire.issuetimetracking.ui.QuickCreateIssueComposite.issueTypeLabel"));
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		issueTypeCombo.setLayoutData(gridData);
		
		
		//Project
		XComposite projectComposite = new XComposite(mainComposite, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		projectComposite.setLayoutData(gridData);

		new Label(projectComposite, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.issuetimetracking.ui.QuickCreateIssueComposite.projectLabel")); //$NON-NLS-1$

		projectComboComposite = new ProjectComboComposite(projectComposite, SWT.None);

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		projectComboComposite.setLayoutData(gridData);
		projectComboComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				newIssue.setProject(projectComboComposite.getSelectedProject());
			}
		});

		//Department
		XComposite departmentComposite = new XComposite(mainComposite, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		departmentComposite.setLayoutData(gridData);

		new Label(departmentComposite, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.issuetimetracking.ui.QuickCreateIssueComposite.departmentLabel")); //$NON-NLS-1$

		departmentComboComposite = new DepartmentComboComposite(departmentComposite){
			@Override
			protected XCombo createCombo() {
				XCombo combo = super.createCombo();
				combo.addTraverseListener(new TraverseListener() {
					@Override
					public void keyTraversed(TraverseEvent e) {
						startDateControl.setFocus();
					}
				});
				return combo;
			}
		};
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		departmentComboComposite.setLayoutData(gridData);

		departmentComboComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				try {
					PropertySet propertySet = newIssue.getPropertySet();
					propertySet.inflate(issueStruct);
					departmentDataField = propertySet.getDataField(IssueTimeTrackingStruct.DEPARTMENT_FIELD, DepartmentDataField.class);
					departmentDataField.setData(departmentComboComposite.getSelectedDepartment());
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		});

		//Date
		XComposite dateComposite = new XComposite(mainComposite, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		dateComposite.setLayoutData(gridData);

		new Label(dateComposite, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.issuetimetracking.ui.QuickCreateIssueComposite.startDateLabel")); //$NON-NLS-1$
		startDateControl = new DateTimeControl(dateComposite, SWT.DATE, DateFormatter.FLAGS_DATE_SHORT);
		startDateControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		forceTextFocusOnTab(startDateControl);

		//Time
		XComposite timeComposite = new XComposite(mainComposite, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		timeComposite.setLayoutData(gridData);

		new Label(timeComposite, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.issuetimetracking.ui.QuickCreateIssueComposite.timeLabel")); //$NON-NLS-1$
		startTimeControl = new DateTimeControl(timeComposite, SWT.TIME, DateFormatter.FLAGS_TIME_HM, null);
		startTimeControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		forceTextFocusOnTab(startTimeControl);

		//Duration
		XComposite durationComposite = new XComposite(mainComposite, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		durationComposite.setLayoutData(gridData);

		new Label(durationComposite, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.issuetimetracking.ui.QuickCreateIssueComposite.durationLabel")); //$NON-NLS-1$
		durationText = new TimeLengthComposite(durationComposite);
		durationText.setDisplayZeroValues(false);
		durationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//Subject & Description
		XComposite subjectDescriptionComposite = new XComposite(mainComposite, SWT.NONE);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = numCols;
		subjectDescriptionComposite.setLayoutData(gridData);

		Label subjectLabel = new Label(subjectDescriptionComposite, SWT.WRAP);
		subjectLabel.setLayoutData(new GridData());
		subjectLabel.setText(Messages.getString("org.nightlabs.jfire.issuetimetracking.ui.QuickCreateIssueComposite.subjectLabel")); //$NON-NLS-1$

		subjectText = new I18nTextEditor(subjectDescriptionComposite) {
			@Override
			protected void createContext(Composite parent,
					LanguageChooser languageChooser, String caption) {
				super.createContext(parent, languageChooser, caption);
				setTabList(new Control[]{getText()});
			}
		};

		subjectText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				boolean hasSubjectText = !(subjectText.getI18nText().getText() != null && subjectText.getI18nText().getText().equals("")); //$NON-NLS-1$
				createIssueQuickDialog.setOKButtonEnabled(hasSubjectText);
			}
		});

		Label descriptionLabel = new Label(subjectDescriptionComposite, SWT.WRAP);
		descriptionLabel.setLayoutData(new GridData());
		descriptionLabel.setText(Messages.getString("org.nightlabs.jfire.issuetimetracking.ui.QuickCreateIssueComposite.descriptionLabel")); //$NON-NLS-1$

		descriptionText = new I18nTextEditorMultiLine(subjectDescriptionComposite, subjectText.getLanguageChooser());
		descriptionText.setI18nText(newIssue.getDescription(), EditMode.DIRECT);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.verticalSpan = 3;
		descriptionText.setLayoutData(gridData);
	}

	private void forceTextFocusOnTab(Composite composite) {
		List<Text> controlList = new ArrayList<Text>();
		Control[] controls = composite.getChildren();
		for (Control control : controls) {
			if (control instanceof Text) {
				Text t = (Text)control;
				controlList.add(t);
			}
		}

		composite.setTabList(controlList.toArray(new Text[0]));
	}

	public Issue getCreatingIssue() {
		newIssue.setIssueType(issueTypeCombo.getSelectedElement());
		newIssue.getSubject().copyFrom(subjectText.getI18nText());
		newIssue.getDescription().copyFrom(descriptionText.getI18nText());

		Calendar startDateTimeCalendar = Calendar.getInstance(NLLocale.getDefault());

		Date startTime = startTimeControl.getDate();
		if (startTime == null) {
			startDateTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
			startDateTimeCalendar.set(Calendar.MINUTE, 0);
			startDateTimeCalendar.set(Calendar.SECOND, 0);
		}
		else {
			startDateTimeCalendar.setTime(startDateControl.getDate());
			startDateTimeCalendar.setTime(startTime);
		}

		newIssue.startWorking(startDateTimeCalendar.getTime());
		startDateTimeCalendar.add(Calendar.MILLISECOND, (int)durationText.getTimeLength());
		newIssue.endWorking(startDateTimeCalendar.getTime());

		//Stores previous data
		previousStartDateTime = startDateTimeCalendar;
		return newIssue;
	}

	private Calendar previousStartDateTime;

	public void initUI() {
		if (previousStartDateTime == null) {
			previousStartDateTime = Calendar.getInstance(NLLocale.getDefault());
			previousStartDateTime.set(Calendar.HOUR_OF_DAY, 0);
			previousStartDateTime.set(Calendar.MINUTE, 0);
			previousStartDateTime.set(Calendar.SECOND, 0);
		}

		startDateControl.setDate(previousStartDateTime.getTime());
		startTimeControl.setDate(null);
		durationText.setTimeLength(0);

		subjectText.setI18nText(new I18nTextBuffer());
		descriptionText.setI18nText(new I18nTextBuffer());
		durationText.setFocus();
	}

	private User currentUser;
	public void initData() {
		newIssue = new Issue(IDGenerator.getOrganisationID(), IDGenerator.nextID(Issue.class));

		if (currentUser == null)
			currentUser = Login.sharedInstance().getUser(new String[]{User.FETCH_GROUP_NAME}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new org.eclipse.core.runtime.NullProgressMonitor());
		newIssue.setAssignee(currentUser);
		newIssue.setReporter(currentUser);
	}
}