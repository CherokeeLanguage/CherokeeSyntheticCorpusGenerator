package com.cherokeelessons.syncorpus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import com.cherokeelessons.syncorpus.models.CorpusEntries;
import com.cherokeelessons.syncorpus.models.CorpusEntry;
import com.cherokeelessons.syncorpus.models.SpecialChars;

/**
 * Simple substitution of "nouns" and "gendered pronouns" for various CED
 * example entries. Only entries with substitutions are generated.
 * 
 * @author muksihs
 *
 */
public class WordSubstitutor {

	private static String CED_EXAMPLES = "ced.en-chr.tsv";
	private static CorpusEntries corpusEntries = new CorpusEntries();
	private static CorpusEntries generatedCorpusEntries = new CorpusEntries();

	public static List<CorpusEntry> getCorpusEntries() throws IOException {
		loadResources();
		doEnglishUncontractions();
		doSplits();
		doHeSheSubstitutions();
		dedupeEntries(); //do last
		return generatedCorpusEntries.getCards();
	}

	/**
	 * Make sure we don't have any duplicate entries. Do last.
	 */
	private static void dedupeEntries() {
		Set<String> already = new HashSet<>();
		Iterator<CorpusEntry> iterator = generatedCorpusEntries.getCards().iterator();
		while (iterator.hasNext()) {
			CorpusEntry entry = iterator.next();
			String lcEn = entry.getAnswer().get(0).toLowerCase().trim();
			String lcChr = entry.getChallenge().get(0).toLowerCase().trim();
			String enChr = lcEn+"|"+lcChr;
			if (already.contains(enChr)) {
				iterator.remove();
				continue;
			}
			already.add(enChr);
		}
	}

	/**
	 * Look for common English contractions and decontract them. Add new primary
	 * entries for word substitution consideration and split consideration and also add to the corpus list
	 * to be output. Must be done before any split checking or substitutions. Assumes *straight* quotes.
	 */
	private static void doEnglishUncontractions() {
		ListIterator<CorpusEntry> listIterator = corpusEntries.getCards().listIterator();
		while (listIterator.hasNext()) {
			CorpusEntry entry = listIterator.next();
			String en = entry.getAnswer().get(0);
			String chr = entry.getChallenge().get(0);
			if (!en.contains("'")) {
				continue;
			}
			String alt = en;
			alt = alt.replaceAll("(?i)(is)n't\\b", "$1 not");
			alt = alt.replaceAll("(?i)(do)n't\\b", "$1 not");
			alt = alt.replaceAll("(?i)(did)n't\\b", "$1 not");
			alt = alt.replaceAll("(?i)(does)n't\\b", "$1 not");
			alt = alt.replaceAll("(?i)(he)'s\\b", "$1 is");
			alt = alt.replaceAll("(?i)(it)'s\\b", "$1 is");
			alt = alt.replaceAll("(?i)(i)'m\\b", "$1 am");
			alt = alt.replaceAll("(?i)(you)'ll\\b", "$1 will");
			alt = alt.replaceAll("(?i)(that)'s\\b", "$1 is");
			alt = alt.replaceAll("(?i)(who)'s\\b", "$1 is");
			alt = alt.replaceAll("(?i)(let)'s\\b", "$1 us");
			alt = alt.replaceAll("(?i)(there)'s\\b", "$1 is");
			alt = alt.replaceAll("(?i)(i)'ll\\b", "$1 will");
			alt = alt.replaceAll("(?i)(we)'ll\\b", "$1 will");
			alt = alt.replaceAll("(?i)(i)'ve\\b", "$1 have");
			alt = alt.replaceAll("(?i)(you)'re\\b", "$1 are");
			alt = alt.replaceAll("(?i)(they)'re\\b", "$1 are");
			alt = alt.replaceAll("(?i)(can)'t\\b", "$1 not");
			alt = alt.replaceAll("(?i)(wo)n't\\b", "$1 not");
			alt = alt.replaceAll("(?i)(what)'s\\b", "$1 is");
			alt = alt.replaceAll("(?i)(has)n't\\b", "$1 not");
			alt = alt.replaceAll("(?i)(have)n't\\b", "$1 not");
			alt = alt.replaceAll("(?i)(\\w)n't\\b", "$1 not");
			if (!alt.equals(en)) {
				CorpusEntry ce = new CorpusEntry();
				ce.setAnswer(Arrays.asList(alt));
				ce.setChallenge(Arrays.asList(chr));
				listIterator.add(ce);
				ce = new CorpusEntry();
				ce.setAnswer(Arrays.asList(alt));
				ce.setChallenge(Arrays.asList(chr));
				generatedCorpusEntries.getCards().add(ce);
				continue;
			}
		}
	}

	/**
	 * See if we can split the corpus entry into multiple entries for training. If
	 * yes, add new primary entries for word substitution consideration and also add
	 * to the corpus list to be output. Uses a simplistic punctuation and numbers
	 * must match check.
	 */
	private static void doSplits() {
		ListIterator<CorpusEntry> listIterator = corpusEntries.getCards().listIterator();
		while (listIterator.hasNext()) {
			CorpusEntry entry = listIterator.next();
			String en = entry.getAnswer().get(0);
			String chr = entry.getChallenge().get(0);
			if (en.matches(".*\\d.*")) {
				continue;
			}
			String xen = en.replaceAll("(?i)['a-z\\s]", "");
			String xchr = chr.replaceAll("(?i)['Ꭰ-Ᏼ\\s]", "");
			if (!xen.equals(xchr)) {
				continue;
			}
			en = en.replaceAll("([.?:;!,])", "$1\n");
			chr = chr.replaceAll("([.?:;!,])", "$1\n");
			String[] ensplit = en.split("\n");
			String[] chrsplit = chr.split("\n");
			if (ensplit == null || chrsplit == null) {
				continue;
			}
			assert ensplit.length == chrsplit.length;
			if (ensplit.length == 1) {
				continue;
			}
			for (int ix = 0; ix < ensplit.length && ix < chrsplit.length; ix++) {
				String entrim = ensplit[ix].trim();
				if (StringUtils.isBlank(entrim)) {
					continue;
				}
				String chrtrim = chrsplit[ix].trim();
				if (StringUtils.isBlank(chrtrim)) {
					continue;
				}
				CorpusEntry ce = new CorpusEntry();
				ce.setAnswer(Arrays.asList(entrim));
				ce.setChallenge(Arrays.asList(chrtrim));
				listIterator.add(ce);
				ce = new CorpusEntry();
				ce.setAnswer(Arrays.asList(entrim));
				ce.setChallenge(Arrays.asList(chrtrim));
				generatedCorpusEntries.getCards().add(ce);
			}
		}
	}

	private static void doHeSheSubstitutions() {
		for (CorpusEntry entry : corpusEntries.getCards()) {
			String en = entry.getAnswer().get(0);
			String chr = entry.getChallenge().get(0);
			en = en.replaceAll("(?i)\b(he|she|him|her|his|hers)\b", SpecialChars.RIGHT_ARROW + "$1");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "He\b", "She");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "he\b", "she");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "She\b", "He");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "she\b", "he");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "Him\b", "Her");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "him\b", "her");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "Her\b", "Him");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "her\b", "him");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "His\b", "Hers");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "his\b", "hers");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "Hers\b", "His");
			en = en.replaceAll(SpecialChars.RIGHT_ARROW + "hers\b", "his");
			if (!en.equalsIgnoreCase(entry.getAnswer().get(0))) {
				CorpusEntry ce = new CorpusEntry();
				ce.setAnswer(Arrays.asList(en.trim()));
				ce.setChallenge(Arrays.asList(chr.trim()));
				generatedCorpusEntries.getCards().add(ce);
			}
		}
	}

	private static void loadResources() throws IOException {
		List<String[]> entries = new ArrayList<>();
		try (LineIterator l = IOUtils.lineIterator(Conjugator.class.getResourceAsStream(CED_EXAMPLES),
				StandardCharsets.UTF_8)) {
			while (l.hasNext()) {
				entries.add(Arrays.copyOf(l.next().split("\t"), 2));
			}
		}
		// some of the entries are bad, remove them
		Iterator<String[]> iEntries = entries.iterator();
		while (iEntries.hasNext()) {
			String[] next = iEntries.next();
			String en = next[0];
			if (StringUtils.isBlank(en)) {
				iEntries.remove();
				continue;
			}
			String chr = next[1];
			if (StringUtils.isBlank(chr)) {
				iEntries.remove();
				continue;
			}
			if (en.matches("(?i).*[Ꭰ-Ᏼ].*")) {
				iEntries.remove();
				continue;
			}
			if (!en.matches("(?i).*[a-z].*")) {
				iEntries.remove();
				continue;
			}
			if (chr.matches("(?i).*[a-z].*")) {
				iEntries.remove();
				continue;
			}
			if (!chr.matches("(?i).*[Ꭰ-Ᏼ].*")) {
				iEntries.remove();
				continue;
			}
			CorpusEntry entry = new CorpusEntry();
			entry.setAnswer(Arrays.asList(en));
			entry.setChallenge(Arrays.asList(chr));
			corpusEntries.getCards().add(entry);
		}

	}

}
