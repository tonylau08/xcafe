package com.igeeksky.xcafe.core.context;

import com.igeeksky.xcafe.web.filter.FilterDef;

public interface Context {

	public FilterDef findFilterDef(String filterName);

	public void addFilterDef(FilterDef filterDef);

}
