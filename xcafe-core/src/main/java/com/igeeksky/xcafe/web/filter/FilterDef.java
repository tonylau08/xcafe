package com.igeeksky.xcafe.web.filter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.igeeksky.xcafe.util.Assert;
import com.igeeksky.xcafe.util.StringUtil;

public class FilterDef implements Serializable {

	private static final long serialVersionUID = -1764863118777565529L;

	private String description = null;

	public String getDescription() {
		return (this.description);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	private String displayName = null;

	public String getDisplayName() {
		return (this.displayName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * The filter instance associated with this definition
	 */
	private transient javax.servlet.Filter filter = null;

	public javax.servlet.Filter getFilter() {
		return filter;
	}

	public void setFilter(javax.servlet.Filter filter) {
		this.filter = filter;
	}

	/**
	 * The fully qualified name of the Java class that implements this filter.
	 */
	private String filterClass = null;

	public String getFilterClass() {
		return (this.filterClass);
	}

	public void setFilterClass(String filterClass) {
		this.filterClass = filterClass;
	}

	/**
	 * The name of this filter, which must be unique among the filters defined
	 * for a particular web application.
	 */
	private String filterName = null;

	public String getFilterName() {
		return (this.filterName);
	}

	public void setFilterName(String filterName) {
		Assert.isTrue(StringUtil.isNotEmpty(filterName), "filterName must not be null or blank");
		this.filterName = filterName;
	}

	/**
	 * The large icon associated with this filter.
	 */
	private String largeIcon = null;

	public String getLargeIcon() {
		return (this.largeIcon);
	}

	public void setLargeIcon(String largeIcon) {
		this.largeIcon = largeIcon;
	}

	private final Map<String, String> parameters = new HashMap<>();

	public Map<String, String> getParameterMap() {
		return (this.parameters);
	}

	/**
	 * The small icon associated with this filter.
	 */
	private String smallIcon = null;

	public String getSmallIcon() {
		return (this.smallIcon);
	}

	public void setSmallIcon(String smallIcon) {
		this.smallIcon = smallIcon;
	}

	private String asyncSupported = null;

	public String getAsyncSupported() {
		return asyncSupported;
	}

	public void setAsyncSupported(String asyncSupported) {
		this.asyncSupported = asyncSupported;
	}

	public void addInitParameter(String name, String value) {
		if (parameters.containsKey(name)) {
			return;
		}
		parameters.put(name, value);

	}
	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder("FilterDef[");
		sb.append("filterName=");
		sb.append(this.filterName);
		sb.append(", filterClass=");
		sb.append(this.filterClass);
		sb.append("]");
		return (sb.toString());

	}

}