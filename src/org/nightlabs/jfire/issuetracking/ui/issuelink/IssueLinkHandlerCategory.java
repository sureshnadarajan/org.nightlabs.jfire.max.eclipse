/**
 * 
 */
package org.nightlabs.jfire.issuetracking.ui.issuelink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author chairat
 *
 */
public class IssueLinkHandlerCategory {

	private String categoryId;
	
	private String parentCategoryId;
	
	private IssueLinkHandlerCategory parent;
	
	private List<IssueLinkHandlerCategory> childCategories = new ArrayList<IssueLinkHandlerCategory>();
	
	private List<IssueLinkHandlerFactory> childFactories = new ArrayList<IssueLinkHandlerFactory>();
	
	private String name;
	
	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public IssueLinkHandlerCategory getParent() {
		return parent;
	}

	public void setParent(IssueLinkHandlerCategory parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<IssueLinkHandlerCategory> getChildCategories() {
		return Collections.unmodifiableList(childCategories);
	}
	
	public void addChildCategory(IssueLinkHandlerCategory child) {
		childCategories.add(child);
	}
	
	public void removeChildCategory(IssueLinkHandlerCategory child) {
		childCategories.remove(child);
	}

	public List<IssueLinkHandlerFactory> getChildFactories() {
		return Collections.unmodifiableList(childFactories);
	}
	
	public void addChildFactory(IssueLinkHandlerFactory child) {
		childFactories.add(child);
	}
	
	public void removeChildFactory(IssueLinkHandlerFactory child) {
		childFactories.remove(child);
	}
	
	public String getParentCategoryId() {
		return parentCategoryId;
	}

	public void setParentCategoryId(String parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}
	
	public List<Object> getChildObjects() {
		List<Object> objs = new ArrayList<Object>();
		objs.addAll(childCategories);
		objs.addAll(childFactories);
		return objs;
	}
	

}
