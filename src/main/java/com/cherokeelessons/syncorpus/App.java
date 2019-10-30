package com.cherokeelessons.syncorpus;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import com.cherokeelessons.syncorpus.models.CorpusEntry;
import com.cherokeelessons.syncorpus.models.SpecialChars;

public class App implements Runnable {

	private PrintStream out;

	public App(PrintStream out) {
		this.out = out;
	}

	@Override
	public void run() {
		try {
			doBasicConjugations();
			doWordSubstitutions();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void doWordSubstitutions() throws IOException {
		for (CorpusEntry entry :WordSubstitutor.getCorpusEntries()) {
			String l1 = entry.getChr().get(0);
			Iterator<String> i2 = entry.getEn().iterator();
			while (i2.hasNext()) {
				String next = i2.next();
				out.println(l1 + "\t" + next);
			}
		}
	}

	private void doBasicConjugations() {
		for (CorpusEntry entry : Conjugator.getCorpusEntries().getCards()) {
			String l1 = entry.getChr().get(0);
			String pronunciationMarkers = SpecialChars.UNDERX + SpecialChars.UNDERDOT + SpecialChars.TONE_MARKER_1
					+ SpecialChars.TONE_MARKER_2 + SpecialChars.TONE_MARKER_3 + SpecialChars.TONE_MARKER_4;
			l1 = l1.replaceAll("[" + pronunciationMarkers + "]+", "");
			Iterator<String> i2 = entry.getEn().iterator();
			while (i2.hasNext()) {
				String next = i2.next();
				out.println(l1 + ".\t" + next + ".");
			}
		}
	}

}
