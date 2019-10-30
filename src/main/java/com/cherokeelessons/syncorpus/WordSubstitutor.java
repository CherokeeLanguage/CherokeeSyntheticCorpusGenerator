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
 * example entries. Only entries with substitutions are generated. <br>
 * Also does basic sentence and phrase splitting between language pairs based on
 * punctuation line ups.
 * 
 * @author muksihs
 *
 */
public class WordSubstitutor {

	private static final String NT_WEB = "corpus-nt-web.en-chr.tsv";
	private static final String GENESIS_WEB = "corpus-genesis-web.en-chr.tsv";
	private static String PHOENIX_CORPUS = "phoenix.en-chr.tsv";
	private static String CED_EXAMPLES = "ced.en-chr.tsv";
	private static CorpusEntries seedEntries = new CorpusEntries();
	private static CorpusEntries generatedCorpusEntries = new CorpusEntries();

	public static List<CorpusEntry> getCorpusEntries() throws IOException {
		loadResources();
		doEnglishUncontractions();
		doSentenceSplits();
		doHeSheSubstitutions();
		doCritterSubstitutions();
		removeParagraphEntries();
		cleanEntries(); //remove bad entries based on charsets
		dedupeEntries(); // do last
		return generatedCorpusEntries.getCards();
	}

	private static void cleanEntries() {
		Iterator<CorpusEntry> iterator = generatedCorpusEntries.getCards().iterator();
		while (iterator.hasNext()) {
			CorpusEntry entry = iterator.next();
			String en = entry.getEn().get(0);
			String chr = entry.getChr().get(0);
			if (!en.matches("(?i).*[a-z].*")){
				iterator.remove();
				continue;
			}
			if (!chr.matches("(?i).*[Ꭰ-Ᏼ].*")){
				iterator.remove();
				continue;
			}
//			if (chr.matches("(?i).*[a-z].*")){
//				iterator.remove();
//				continue;
//			}
			if (en.matches("(?i).*[Ꭰ-Ᏼ].*")){
				iterator.remove();
				continue;
			}
			
			//remove entries that have a few more cherokee words than english words
			if (StringUtils.countMatches(chr, " ")>StringUtils.countMatches(en, " ")+3) {
				iterator.remove();
				continue;
			}
			
			//remove entries that have way more than twice the english words to cherokee words
			if (StringUtils.countMatches(en, " ")>StringUtils.countMatches(chr, " ")*2) {
				iterator.remove();
				continue;
			}
		}
	}

	private static void removeParagraphEntries() {
		ListIterator<CorpusEntry> listIterator = generatedCorpusEntries.getCards().listIterator();
		while (listIterator.hasNext()) {
			CorpusEntry entry = listIterator.next();
			String en = entry.getEn().get(0);
			String chr = entry.getChr().get(0);
			//remove entries with 3 or more "." in them
			if (StringUtils.countMatches(en, ".")>3) {
				listIterator.remove();
				continue;
			}
			if (StringUtils.countMatches(chr, ".")>3) {
				listIterator.remove();
				continue;
			}
		}
	}

	/**
	 * Process previously generated entries for additional permutations to create via critter substitutions.
	 */
	private static void doCritterSubstitutions() {
		ListIterator<CorpusEntry> listIterator = generatedCorpusEntries.getCards().listIterator();
		while (listIterator.hasNext()) {
			CorpusEntry entry = listIterator.next();
			String en = entry.getEn().get(0);
			String chr = entry.getChr().get(0);
			
			//Are the sentences close matches?
			String xen = en.replaceAll("(?i)['a-z\\s]", "");
			String xchr = chr.replaceAll("(?i)['Ꭰ-Ᏼ\\s]", "");
			if (!xen.equals(xchr)) {
				continue;
			}
			//Does it look like only a single sentence?
			if (en.matches(".+[.?!].+")) {
				continue;
			}
		}
	}

	/**
	 * Make sure we don't have any duplicate entries. Do last.
	 */
	private static void dedupeEntries() {
		Set<String> already = new HashSet<>();
		Iterator<CorpusEntry> iterator = generatedCorpusEntries.getCards().iterator();
		while (iterator.hasNext()) {
			CorpusEntry entry = iterator.next();
			String lcEn = entry.getEn().get(0).toLowerCase().trim();
			String lcChr = entry.getChr().get(0).toLowerCase().trim();
			String enChr = lcEn + "|" + lcChr;
			if (already.contains(enChr)) {
				iterator.remove();
				continue;
			}
			already.add(enChr);
		}
	}

	/**
	 * Look for common English contractions and decontract them. Add new primary
	 * entries for word substitution consideration and split consideration and also
	 * add to the corpus list to be output. Must be done before any split checking
	 * or substitutions. Assumes *straight* quotes.
	 */
	private static void doEnglishUncontractions() {
		ListIterator<CorpusEntry> listIterator = seedEntries.getCards().listIterator();
		while (listIterator.hasNext()) {
			CorpusEntry entry = listIterator.next();
			String en = entry.getEn().get(0);
			String chr = entry.getChr().get(0);
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
			alt = alt.replaceAll("(?i)(is) not it\\?", "$1 it not?");
			alt = alt.replaceAll("(?i)(did) not it\\?", "$1 it not?");
			if (!alt.equals(en)) {
				CorpusEntry ce = new CorpusEntry();
				ce.setEn(Arrays.asList(alt));
				ce.setChr(Arrays.asList(chr));
				listIterator.add(ce);
				ce = new CorpusEntry();
				ce.setEn(Arrays.asList(alt));
				ce.setChr(Arrays.asList(chr));
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
	private static void doSentenceSplits() {
		ListIterator<CorpusEntry> listIterator = seedEntries.getCards().listIterator();
		while (listIterator.hasNext()) {
			CorpusEntry entry = listIterator.next();
			String en = entry.getEn().get(0);
			String chr = entry.getChr().get(0);
			if (en.matches(".*\\d.*")) {
				continue;
			}
			//try and remove from abbreviations, any "."
			en = en.replaceAll("([A-Z][a-zA-Z]*)[.]", "$1");
			
			String xen = en.replaceAll("(?i)[Ꭰ-Ᏼa-z\\s]", "");
			String xchr = chr.replaceAll("(?i)[Ꭰ-Ᏼa-z\\s]", "");
			if (!xen.equals(xchr)) {
//				System.out.println(xen+" != "+xchr);
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
//			if (StringUtils.countMatches(en, ".")<3) {
				generatedCorpusEntries.getCards().add(entry);
//			}
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
				ce.setEn(Arrays.asList(entrim));
				ce.setChr(Arrays.asList(chrtrim));
				listIterator.add(ce);
				ce = new CorpusEntry();
				ce.setEn(Arrays.asList(entrim));
				ce.setChr(Arrays.asList(chrtrim));
				generatedCorpusEntries.getCards().add(ce);
			}
		}
	}

	/**
	 * Do generic type s/he swapping on the corpus entries curated for output.
	 */
	private static void doHeSheSubstitutions() {
		ListIterator<CorpusEntry> listIterator = generatedCorpusEntries.getCards().listIterator();
		while (listIterator.hasNext()) {
			CorpusEntry entry = listIterator.next();
			String en = entry.getEn().get(0);
			String chr = entry.getChr().get(0);
			if (en.matches("(?i).*\\bher\\b.*")) {
				//English pronoun "her" is ambiguous. Skip the sentence entirely for processing.
				continue;
			}
			String alt = en;
			alt = alt.replaceAll("(?i)\\b(he|she|him|his|hers|herself|himself)\\b", SpecialChars.RIGHT_ARROW + "$1");
			alt = alt.replaceAll("(?i)"+SpecialChars.RIGHT_ARROW + "(H)imself\\b", "$1erself");
			alt = alt.replaceAll("(?i)"+SpecialChars.RIGHT_ARROW + "(H)erself\\b", "$1imself");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "he\\b", "she");
			
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "He\\b", "She");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "he\\b", "she");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "She\\b", "He");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "she\\b", "he");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "Him\\b", "Her");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "him\\b", "her");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "His\\s\\b", "Her ");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "his\\s\\b", "her ");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "His\\b", "Hers");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "his\\b", "hers");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "Hers\\b", "His");
			alt = alt.replaceAll(SpecialChars.RIGHT_ARROW + "hers\\b", "his");
			if (!alt.equalsIgnoreCase(en)) {
				CorpusEntry ce = new CorpusEntry();
				ce.setEn(Arrays.asList(alt));
				ce.setChr(Arrays.asList(chr));
				listIterator.add(ce);
			}
		}
	}

	private static void loadResources() throws IOException {
		loadCedExamples();
		loadRawPhoenixCorpus();
		loadNtWebCorpus();
		loadGenesisWebCorpus();
	}

	private static void loadCedExamples() throws IOException {
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
			entry.setEn(Arrays.asList(en));
			entry.setChr(Arrays.asList(chr));
			seedEntries.getCards().add(entry);
		}
	}
	
	private static void loadRawPhoenixCorpus() throws IOException {
		List<String[]> entries = new ArrayList<>();
		try (LineIterator l = IOUtils.lineIterator(Conjugator.class.getResourceAsStream(PHOENIX_CORPUS),
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
//			if (chr.matches("(?i).*[a-z].*")) {
//				iEntries.remove();
//				continue;
//			}
			if (!chr.matches("(?i).*[Ꭰ-Ᏼ].*")) {
				iEntries.remove();
				continue;
			}
			
			//cleanup
			en = en.replace("---", " ");
			en = en.replace("–", " ");
			en = StringUtils.normalizeSpace(en);
			
			//try and remove "." off of abbreviations like "Jan." and "U.S."
			en = en.replaceAll("([A-Z][a-zA-Z]*)[.]", "$1");
			
			chr = chr.replace("---", " ");
			chr = chr.replace("–", " ");
			chr = StringUtils.normalizeSpace(chr);
			
			CorpusEntry entry = new CorpusEntry();
			entry.setEn(Arrays.asList(en));
			entry.setChr(Arrays.asList(chr));
			seedEntries.getCards().add(entry);}
		}
		
		/**
		 * Load the NT bitext, altering the data as needed to deal with not having '"' in the cherokee text, etc
		 * @throws IOException
		 */
		private static void loadNtWebCorpus() throws IOException {
			List<String[]> entries = new ArrayList<>();
			try (LineIterator l = IOUtils.lineIterator(Conjugator.class.getResourceAsStream(NT_WEB),
					StandardCharsets.UTF_8)) {
				while (l.hasNext()) {
					entries.add(Arrays.copyOf(l.next().split("\t"), 2));
				}
			}
			// remove entries with empty bitext values
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
				if (!en.matches("(?i).*[a-z].*")) {
					iEntries.remove();
					continue;
				}
				if (!chr.matches("(?i).*[Ꭰ-Ᏼ].*")) {
					iEntries.remove();
					continue;
				}

			}
			
			// import with filtering/adjustments
			iEntries = entries.iterator();
			while (iEntries.hasNext()) {
				String[] next = iEntries.next();
				String en = next[0];
				String chr = next[1];
				
				en = StringUtils.normalizeSpace(en);
				chr = StringUtils.normalizeSpace(chr);
				
				//The Cherokee text uses ';' where English uses '.' many times.
				chr = chr.replace(";", ".");
				en = en.replace(";", ".");
				
				//The Cherokee text doesn't use '"'
				en = en.replace("\"", "");
				en = en.replace(" '", " ");
				en = en.replace("' ", " ");
				
				CorpusEntry entry = new CorpusEntry();
				entry.setEn(Arrays.asList(en));
				entry.setChr(Arrays.asList(chr));
				
				//add result to seed entries for possible sentence splitting and inclusion in output
				seedEntries.getCards().add(entry);
			}
	}
		
		/**
		 * Load the NT bitext, altering the data as needed to deal with not having '"' in the cherokee text, etc
		 * @throws IOException
		 */
		private static void loadGenesisWebCorpus() throws IOException {
			List<String[]> entries = new ArrayList<>();
			try (LineIterator l = IOUtils.lineIterator(Conjugator.class.getResourceAsStream(GENESIS_WEB),
					StandardCharsets.UTF_8)) {
				while (l.hasNext()) {
					entries.add(Arrays.copyOf(l.next().split("\t"), 2));
				}
			}
			// remove entries with empty bitext values
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
				if (!en.matches("(?i).*[a-z].*")) {
					iEntries.remove();
					continue;
				}
				if (!chr.matches("(?i).*[Ꭰ-Ᏼ].*")) {
					iEntries.remove();
					continue;
				}

			}
			
			// import with filtering/adjustments
			iEntries = entries.iterator();
			while (iEntries.hasNext()) {
				String[] next = iEntries.next();
				String en = next[0];
				String chr = next[1];
				
				en = StringUtils.normalizeSpace(en);
				chr = StringUtils.normalizeSpace(chr);
				
				//The Cherokee text uses ';' where English uses '.' many times.
				chr = chr.replace(";", ".");
				en = en.replace(";", ".");
				
				//The Cherokee text doesn't use '"'
				en = en.replace("\"", "");
				en = en.replace(" '", " ");
				en = en.replace("' ", " ");
				
				CorpusEntry entry = new CorpusEntry();
				entry.setEn(Arrays.asList(en));
				entry.setChr(Arrays.asList(chr));
				
				//add result to seed entries for possible sentence splitting and inclusion in output
				seedEntries.getCards().add(entry);
			}
	}

}
