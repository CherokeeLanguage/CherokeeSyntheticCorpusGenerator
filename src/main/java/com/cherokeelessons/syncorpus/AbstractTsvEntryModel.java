package com.cherokeelessons.syncorpus;

import com.github.vbauer.jackdaw.annotation.JBean;

@JBean
public abstract class AbstractTsvEntryModel {
	public static TsvEntry createTsvEntry() {
		TsvEntry tsvEntry = new TsvEntry();
		return tsvEntry;
	}
	
	protected String l1;
	protected String l2;
}
