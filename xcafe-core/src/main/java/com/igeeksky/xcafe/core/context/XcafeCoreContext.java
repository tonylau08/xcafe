package com.igeeksky.xcafe.core.context;

import com.igeeksky.xcafe.web.filter.FilterDef;

public class XcafeCoreContext implements Context {
	
	private boolean isStaticSupport = true;
	
	@Override
	public FilterDef findFilterDef(String filterName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addFilterDef(FilterDef filterDef) {
		// TODO Auto-generated method stub
		
	}

	public boolean isStaticSupport() {
		return isStaticSupport;
	}

	public void setStaticSupport(boolean isStaticSupport) {
		this.isStaticSupport = isStaticSupport;
	}
	
}
