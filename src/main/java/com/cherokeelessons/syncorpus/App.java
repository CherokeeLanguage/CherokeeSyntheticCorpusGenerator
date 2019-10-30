package com.cherokeelessons.syncorpus;

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
		doBasicConjugations();
		doWordSubstitutions();
	}

	private void doWordSubstitutions() {
		
	}

	private void doBasicConjugations() {
		for (CorpusEntry entry : Conjugator.getCorpusEntries().getCards()) {
			String l1 = entry.getChallenge().get(0);
			String pronunciationMarkers = SpecialChars.UNDERX + SpecialChars.UNDERDOT + SpecialChars.TONE_MARKER_1
					+ SpecialChars.TONE_MARKER_2 + SpecialChars.TONE_MARKER_3 + SpecialChars.TONE_MARKER_4;
			l1 = l1.replaceAll("[" + pronunciationMarkers + "]+", "");
			Iterator<String> i2 = entry.getAnswer().iterator();
			while (i2.hasNext()) {
				String next = i2.next();
				out.println(l1 + ".\t" + next + ".");
			}
		}
	}

}
