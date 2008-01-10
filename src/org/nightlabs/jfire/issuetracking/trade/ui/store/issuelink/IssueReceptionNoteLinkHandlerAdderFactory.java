package org.nightlabs.jfire.issuetracking.trade.ui.store.issuelink;

import org.nightlabs.jfire.issuetracking.ui.issuelink.AbstractIssueLinkHandlerFactory;
import org.nightlabs.jfire.issuetracking.ui.issuelink.IssueLinkAdder;
import org.nightlabs.jfire.store.ReceptionNote;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 */
public class IssueReceptionNoteLinkHandlerAdderFactory 
extends AbstractIssueLinkHandlerFactory
{
	public IssueLinkAdder createIssueLinkAdder() {
		IssueLinkAdder adder = new IssueReceptionNoteLinkAdder();
		adder.init(this);
		
		return adder;
	}

	public Class<? extends Object> getLinkObjectClass() {
		return ReceptionNote.class;
	}
}
