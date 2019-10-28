package com.cherokeelessons.syncorpus;

import java.io.IOError;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

public class DataUtil {
	private static String PRONOUNS_RSC = "/com/cherokeelessons/syncorpus/pronouns.tsv";
	private static String ENTRIES_RSC = "/com/cherokeelessons/syncorpus/entries.tsv";

	private static List<String[]> pronouns=null;
	public static List<String[]> getPronouns() {
		if (pronouns==null) {
			try {
				loadPronouns();
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		return new ArrayList<>(pronouns);
	}
	protected static void loadPronouns() throws IOException {
		pronouns = new ArrayList<>();
		try (LineIterator l = IOUtils.lineIterator(ClassLoader.getSystemResourceAsStream(PRONOUNS_RSC),
				StandardCharsets.UTF_8)) {
			while (l.hasNext()) {
				pronouns.add(Arrays.copyOf(l.next().split("\t"), 9));
			}
		}
		Iterator<String[]> iter=pronouns.iterator();
		while (iter.hasNext()) {
			String[] pronoun=iter.next();
			String vtmode = StringUtils.strip(pronoun[0]);
			String syllabary = StringUtils.strip(pronoun[1]);
			if (StringUtils.isBlank(vtmode)) {
				// game.log(this, "Skipping: "+vtmode+" - "+syllabary);
				iter.remove();
				continue;
			}
			if (vtmode.startsWith("#")) {
				// game.log(this, "Skipping: "+vtmode+" - "+syllabary);
				iter.remove();
				continue;
			}
			if (syllabary.startsWith("#")) {
				// game.log(this, "Skipping: "+vtmode+" - "+syllabary);
				iter.remove();
				continue;
			}
		}
	}
	
	private static List<String[]> entries=null;
	public static List<String[]> getEntries() {
		if (entries==null) {
			try {
				loadPronouns();
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		return new ArrayList<>(entries);
	}
	protected static void loadEntries() throws IOException {
		entries = new ArrayList<>();
		try (LineIterator l = IOUtils.lineIterator(ClassLoader.getSystemResourceAsStream(ENTRIES_RSC),
				StandardCharsets.UTF_8)) {
			while (l.hasNext()) {
				entries.add(Arrays.copyOf(l.next().split("\t"), 9));
			}
		}
		Iterator<String[]> iter = entries.iterator();
		while (iter.hasNext()) {
			String[] challenge = iter.next();
			String vtmode = StringUtils.strip(challenge[0]);
			if (StringUtils.isBlank(vtmode)) {
				// game.log(this, "Skipping: "+vtmode);
				iter.remove();
				continue;
			}
			if (vtmode.startsWith("#")) {
				// game.log(this, "Skipping: "+vtmode);
				iter.remove();
				continue;
			}
		}
	}
}
