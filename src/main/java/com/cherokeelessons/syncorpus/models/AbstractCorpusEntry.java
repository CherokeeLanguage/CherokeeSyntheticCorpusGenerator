package com.cherokeelessons.syncorpus.models;

import java.util.ArrayList;
import java.util.List;

import com.github.vbauer.jackdaw.annotation.JBean;

@JBean
public abstract class AbstractCorpusEntry {
	protected int id;
	protected List<String> challenge = new ArrayList<>();
	protected List<String> answer = new ArrayList<>();
	protected String key;
	protected String pgroup;
	protected String vgroup;
	protected boolean reversed;
	public AbstractCorpusEntry() {
	}
	public AbstractCorpusEntry(AbstractCorpusEntry entry) {
		answer.addAll(entry.answer);
		challenge.addAll(entry.challenge);
		id = entry.id;
		key = entry.key;
		pgroup = entry.pgroup;
		vgroup = entry.vgroup;
	}
}
