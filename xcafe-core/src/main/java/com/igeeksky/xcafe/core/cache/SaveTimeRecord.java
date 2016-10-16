package com.igeeksky.xcafe.core.cache;

class SaveTimeRecord extends Record {
	
	final String shortUri;
	final long saveTime;
	final int length;
	
	public SaveTimeRecord(String shortUri, long saveTime, int length) {
		this.shortUri = shortUri;
		this.saveTime = saveTime;
		this.length = length;
	}

}
