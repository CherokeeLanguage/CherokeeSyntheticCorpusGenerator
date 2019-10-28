package com.cherokeelessons.syncorpus;

import java.text.NumberFormat;
import java.util.Iterator;

import org.testng.annotations.Test;

import com.cherokeelessons.syncorpus.models.CorpusEntry;

public class Tests {
	@Test(priority = 0)
	public void corpusEntries() {
		System.out.println("HAVE " + DataUtil.getCorpusEntries().getCards().size() + " CORPUS MAIN ENTRIES");
	}

	@Test(priority = 1)
	public void corpusEntriesDump() {
		int count=0;
		for (CorpusEntry entry : DataUtil.getCorpusEntries().getCards()) {
			String l1 = entry.getChallenge().get(0);
//			l1 = l1.replaceAll("[" + SpecialChars.UNDERX + SpecialChars.UNDERDOT + SpecialChars.TONE_MARKER_1 + SpecialChars.TONE_MARKER_2
//					+ SpecialChars.TONE_MARKER_3 + SpecialChars.TONE_MARKER_4 + "]+", "");
			Iterator<String> i2 = entry.getAnswer().iterator();
			while (i2.hasNext()) {
				String next = i2.next();
				System.out.println(l1 + ".\t" + next+".");
				assert !next.toLowerCase().contains("xshe");
				assert !next.toLowerCase().contains("xhe");
				count++;
			}
		}
		System.out.println("TOTAL ENTRIES: "+NumberFormat.getInstance().format(count));
	}
}
