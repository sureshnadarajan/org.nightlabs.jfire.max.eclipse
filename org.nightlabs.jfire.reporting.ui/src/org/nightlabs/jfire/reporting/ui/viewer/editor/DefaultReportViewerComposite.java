/**
 *
 */
package org.nightlabs.jfire.reporting.ui.viewer.editor;

import java.io.File;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jfire.reporting.Birt;
import org.nightlabs.jfire.reporting.Birt.OutputFormat;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;
import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout;
import org.nightlabs.jfire.reporting.ui.config.DefaultReportViewerCfMod;
import org.nightlabs.jfire.reporting.ui.layout.PreparedRenderedReportLayout;
import org.nightlabs.jfire.reporting.ui.layout.RenderedReportLayoutProvider;
import org.nightlabs.jfire.reporting.ui.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

/**
 * This Composite incorporates two widgets to view
 * reports rendered to pdf and html.
 * <p>
 * It uses the SWT {@link Browser} widget to display the entry
 * URL of an rendered report, this works for html.
 * For pdf it might work as well, if an appropriate pluing
 * is installed for the systems default browser. If
 * not this Composite can also display pdfs via the
 * Adobe Viewer Java bean.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class DefaultReportViewerComposite extends XComposite {

	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DefaultReportViewerComposite.class);

	private Composite stack;
	private StackLayout stackLayout;
	private Composite fetchingLayoutComposite;
	// TODO: Maybe add progressmonitor to status composite
	private BrowserWrapperComposite browser;

//	private Composite awtWrapper;
//	private Frame awtFrame;
//	private Viewer viewer;
//	private PdfViewer pdfViewer;

	/**
	 * The {@link PreparedRenderedReportLayout} for the currently
	 * displayed report.
	 */
	private PreparedRenderedReportLayout preparedLayout;
	/**
	 * Hyperlink displayed when there where errors during report rendering.
	 */
	private ImageHyperlink errorLink;
	
	private DefaultReportViewerUtil reportViewerUtil;

/*	private static class ThreadDeathWorkaround implements IExceptionHandler {
		private Set<DefaultReportViewerComposite> registeredComposites = new HashSet<DefaultReportViewerComposite>();

		public boolean handleException(ExceptionHandlerParam handlerParam) {
			logger.warn("WORKAROUND for Adobe PDF bean ThreadDeath = log.", handlerParam.getTriggerException()); //$NON-NLS-1$
			return true;
		}

		public void registerComposite(final DefaultReportViewerComposite composite) {
			boolean haveToAddHandler = registeredComposites.size() < 1;
			if (!registeredComposites.contains(composite)) {
				registeredComposites.add(composite);
				composite.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent arg0) {
						unregisterComposite(composite);
					}
				});
			}
			if (haveToAddHandler) {
				ExceptionHandlerRegistry.sharedInstance().addExceptionHandler(ThreadDeath.class.getName(), this);
				logger.info("Added WORKAROUND ExceptionHandler for Adobe PDF bean ThreadDeath"); //$NON-NLS-1$
			}
		}

		private void unregisterComposite(DefaultReportViewerComposite composite) {
			registeredComposites.remove(composite);
			if (registeredComposites.size() < 1) {
				ExceptionHandlerRegistry.sharedInstance().removeExceptionHandler(ThreadDeath.class.getName());
				logger.info("Removed WORKAROUND ExceptionHandler for Adobe PDF bean ThreadDeath"); //$NON-NLS-1$
			}
		}
	}*/

//	private static ThreadDeathWorkaround threadDeathWorkaround = new ThreadDeathWorkaround();

	public DefaultReportViewerComposite(Composite parent, int style) {
		super(parent, style, LayoutMode.TIGHT_WRAPPER);
		stack = new Composite(this, SWT.NONE);
		stack.setLayoutData(new GridData(GridData.FILL_BOTH));
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);
		fetchingLayoutComposite = new Composite(stack, SWT.NONE);
		fetchingLayoutComposite.setLayout(new GridLayout());
		Label label = new Label(fetchingLayoutComposite, SWT.WRAP);
		label.setText(Messages.getString("org.nightlabs.jfire.reporting.ui.viewer.editor.DefaultReportViewerComposite.label.text")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		browser = new BrowserWrapperComposite(stack, SWT.NONE);

		reportViewerUtil = DefaultReportViewerUtil.create(); 
		reportViewerUtil.createPDFViewer(stack);
		
		stackLayout.topControl = fetchingLayoutComposite;
	}

	public void switchToStatus() {
		if (!isDisposed()) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					stackLayout.topControl = fetchingLayoutComposite;
					stack.layout(true, true);
				}
			});
		}
	}

	/**
	 * Fetches the {@link RenderedReportLayout} for the given renderRequest
	 * and displays it in the viewer.
	 *
	 * @param renderRequest The render request for the layout to show.
	 */
	public void showReport(final RenderReportRequest renderRequest) {
		switchToStatus();
		Job fetchJob = new Job(Messages.getString("org.nightlabs.jfire.reporting.ui.viewer.editor.DefaultReportViewerComposite.fetchJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) {
				final PreparedRenderedReportLayout preparedLayout = RenderedReportLayoutProvider.sharedInstance().getPreparedRenderedReportLayout(
						renderRequest, monitor
					);
				if (!isDisposed()) {
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							updateViewer(renderRequest.getOutputFormat(), preparedLayout);
						}
					});
				}

				return Status.OK_STATUS;
			}

		};
		fetchJob.schedule();
	}

	public void showReport(final RenderedReportLayout reportLayout) {
		switchToStatus();
		Job fetchJob = new Job(Messages.getString("org.nightlabs.jfire.reporting.ui.viewer.editor.DefaultReportViewerComposite.fetchJob.name")){			 //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) {
				final PreparedRenderedReportLayout preparedLayout = getPreparedRenderedReportLayout(reportLayout, monitor);
				if (!isDisposed()) {
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							updateViewer(reportLayout.getHeader().getOutputFormat(), preparedLayout);
						}
					});
				}
				return Status.OK_STATUS;
			}
		};
		fetchJob.schedule();
	}

	/**
	 * Uses the {@link RenderedReportLayoutProvider} to prepare (unpack) the given
	 * {@link RenderedReportLayout} and returns the URL to its entry file.
	 *
	 * @param reportLayout The rendered layout to prepare.
	 * @param monitor An {@link ProgressMonitor} to provide feedback.
	 * @return The {@link PreparedRenderedReportLayout} for the the given .
	 */
	protected PreparedRenderedReportLayout getPreparedRenderedReportLayout(RenderedReportLayout reportLayout, ProgressMonitor monitor) {
		return RenderedReportLayoutProvider.sharedInstance().getPreparedRenderedReportLayout(reportLayout, monitor);
	}

	/**
	 * Updates the viewer (switches viewer from browser to adobe bean if necessary)
	 * and displays the given preparedLayout.
	 *
	 * @param format The format the prepared layout is in.
	 * @param preparedLayout The prepared layout.
	 */
	protected void updateViewer(Birt.OutputFormat format, final PreparedRenderedReportLayout preparedLayout) {
		this.preparedLayout = preparedLayout;
		reportViewerUtil.setReportLayout(preparedLayout);
		DefaultReportViewerCfMod cfMod = DefaultReportViewerCfMod.sharedInstance();
		if (!isDisposed()) {
			if (format == OutputFormat.pdf && !cfMod.isUseInternalBrowserForPDFs()) {
				reportViewerUtil.updatePDFViewer(stackLayout);
			}
			else {
				// Use the browser widget as default for all other
				stackLayout.topControl = browser;
				browser.setUrl(reportViewerUtil.getResourceLocation());
				
			}
			if (errorLink != null && !errorLink.isDisposed()) {
				errorLink.dispose();
			}
			if (preparedLayout.getRenderedReportLayout().getHeader().hasRenderingErrors()) {
				errorLink = new ImageHyperlink(this, SWT.NONE);
				errorLink.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				errorLink.setText(Messages.getString("org.nightlabs.jfire.reporting.ui.viewer.editor.DefaultReportViewerComposite.errorLink.text")); //$NON-NLS-1$
				errorLink.addHyperlinkListener(new HyperlinkAdapter() {
					@Override
					public void linkActivated(final HyperlinkEvent e) {
						Collection<Throwable> ts = preparedLayout.getRenderedReportLayout().getHeader().getRenderingErrors();
						for (Throwable t : ts) {
							ExceptionHandlerRegistry.asyncHandleException(t);
						}
					}
				});
				errorLink.setImage(
					FieldDecorationRegistry.getDefault().getFieldDecoration(
						FieldDecorationRegistry.DEC_ERROR
					).getImage()
				);
			}
//			// WORKAROUND: The marginHeiht = 1 is a workaround for the acrobat viewer, that won't layout well initially otherwise
//			stackLayout.marginHeight = 1;
			this.layout(true, true);
		}
	}

	public PreparedRenderedReportLayout getPreparedLayout() {
		return preparedLayout;
	}

	private static File lastSaveDirectory;
}
