package com.cherokeelessons.syncorpus.models;

import com.github.vbauer.jackdaw.annotation.JBean;

@JBean
public abstract class AbstractTsvEntryModel {
	public static TsvEntry createTsvEntry(String column1, String column2) {
		TsvEntry tsvEntry = new TsvEntry();
		tsvEntry.setColumn1(column1);
		tsvEntry.setColumn2(column2);
		return tsvEntry;
	}
	
	protected String column1;
	protected String column2;
}
