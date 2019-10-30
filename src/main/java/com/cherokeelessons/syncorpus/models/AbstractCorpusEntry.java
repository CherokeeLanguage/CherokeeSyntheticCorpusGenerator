package com.cherokeelessons.syncorpus.models;

import java.util.ArrayList;
import java.util.List;

import com.github.vbauer.jackdaw.annotation.JBean;

@JBean
public abstract class AbstractCorpusEntry {
	protected int id;
	protected List<String> chr = new ArrayList<>();
	protected List<String> en = new ArrayList<>();
	protected String key;
	protected String pgroup;
	protected String vgroup;
	protected boolean reversed;
	public AbstractCorpusEntry() {
	}
	public AbstractCorpusEntry(AbstractCorpusEntry entry) {
		en.addAll(entry.en);
		chr.addAll(entry.chr);
		id = entry.id;
		key = entry.key;
		pgroup = entry.pgroup;
		vgroup = entry.vgroup;
	}
}
