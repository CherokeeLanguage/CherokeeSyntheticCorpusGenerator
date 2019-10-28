package com.cherokeelessons.syncorpus.models;

import java.util.ArrayList;
import java.util.List;

import com.github.vbauer.jackdaw.annotation.JBean;

@JBean
public abstract class AbstractCorpusEntries {
	protected int version=390;
	protected int size=0;
	protected List<CorpusEntry> cards;
	
	public AbstractCorpusEntries() {
		cards=new ArrayList<>(2048);
	}
	
	public AbstractCorpusEntries(AbstractCorpusEntries deck) {
		cards=new ArrayList<>(deck.cards);
		version=deck.version;
		size=cards.size();
	}
}
