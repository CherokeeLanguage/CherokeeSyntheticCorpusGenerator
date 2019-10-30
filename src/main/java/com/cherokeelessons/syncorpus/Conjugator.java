package com.cherokeelessons.syncorpus;

import java.io.IOError;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import com.cherokeelessons.syncorpus.models.CorpusEntry;
import com.cherokeelessons.syncorpus.models.CorpusEntries;
import com.cherokeelessons.syncorpus.models.SpecialChars;

public class Conjugator {
	private static String PRONOUNS_RSC = "pronouns.tsv";
	private static String ENTRIES_RSC = "entries.tsv";

	public static CorpusEntries getCorpusEntries() {
		if (deck.getCards().isEmpty()) {
			addChallengesToDeck();
		}
		assert !deck.getCards().isEmpty();
		return deck;
	}

	private static final CorpusEntries deck = new CorpusEntries();

	private static List<String[]> pronouns = null;

	private static List<String[]> getPronouns() {
		if (pronouns == null) {
			try {
				loadPronouns();
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		assert pronouns!=null;
		assert !pronouns.isEmpty();
		return new ArrayList<>(pronouns);
	}

	private static void loadPronouns() throws IOException {
		pronouns = new ArrayList<>();
		try (LineIterator l = IOUtils.lineIterator(Conjugator.class.getResourceAsStream(PRONOUNS_RSC),
				StandardCharsets.UTF_8)) {
			while (l.hasNext()) {
				String[] copyOf = Arrays.copyOf(l.next().split("\t"), 9);
				for (int ix=0; ix<copyOf.length; ix++) {
					if (copyOf[ix]==null) {
						copyOf[ix]="";
					}
				}
				pronouns.add(copyOf);
			}
		}
		Iterator<String[]> iter = pronouns.iterator();
		while (iter.hasNext()) {
			String[] pronoun = iter.next();
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

	private static List<String[]> entries = null;

	private static List<String[]> getEntries() {
		if (entries == null) {
			try {
				loadEntries();
			} catch (IOException e) {
				throw new IOError(e);
			}
		}
		return new ArrayList<>(entries);
	}

	private static void loadEntries() throws IOException {
		entries = new ArrayList<>();
		try (LineIterator l = IOUtils.lineIterator(Conjugator.class.getResourceAsStream(ENTRIES_RSC),
				StandardCharsets.UTF_8)) {
			while (l.hasNext()) {
				String[] copyOf = Arrays.copyOf(l.next().split("\t"), 9);
				for (int ix=0; ix<copyOf.length; ix++) {
					if (copyOf[ix]==null) {
						copyOf[ix]="";
					}
				}
				entries.add(copyOf);
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

	private static void addChallengesToDeck() {
		DataSet d = new DataSet();
		StringBuilder vroot = new StringBuilder();
		StringBuilder vroot_chr = new StringBuilder();
		Set<String> vtypes = new HashSet<>();
		Set<String> ptypes = new HashSet<>();
		final Iterator<String[]> ichallenge = getEntries().iterator();
		while (ichallenge.hasNext()) {
			String[] challenge = ichallenge.next();
			ichallenge.remove();
			vtypes.clear();
			vtypes.addAll(Arrays.asList(challenge[0].split(",\\s*")));

			if (vtypes.contains("n")) {
				String term = challenge[2];
				CorpusEntry c = getCardByChallenge(term, deck);
				if (c == null) {
					c = new CorpusEntry();
					deck.getCards().add(c);
				}
				c.setVgroup(term);
				c.setPgroup("");
				// chr
				c.getChr().add(term);
				// latin
				c.getChr().add(challenge[3]);
				for (String def : challenge[5].split(";")) {
					d.def=def;
					definitionEnglishFixer(d);
					c.getEn().add(StringUtils.strip(d.def));
				}
				continue;
			}

			boolean v_g3rd = false;
			if (vtypes.contains("g")) {
				v_g3rd = true;
				vtypes.remove("g");
			}
			if (vtypes.contains("xde") && vtypes.contains("xwi")) {
				vtypes.remove("xde");
				vtypes.add("xdi");
			}
			boolean vSetB = challenge[3].startsWith("u") || challenge[3].startsWith("ụ")
					|| challenge[3].startsWith("j");
			String vroot_set = challenge[4];
			String vroot_chr_set = challenge[2];
			String vdef_active = challenge[5];
			String vdef_passive = challenge[6];
			String vdef_objects = challenge[7];
			String vroot_h = StringUtils.substringBefore(vroot_set, ",");
			String vroot_h_chr = StringUtils.substringBefore(vroot_chr_set, ",");
			String vroot_alt = StringUtils.substringAfter(vroot_set, ",");
			String vroot_alt_chr = StringUtils.substringAfter(vroot_chr_set, ",");
			if (StringUtils.isBlank(vroot_alt)) {
				vroot_alt = vroot_h;
			}
			if (StringUtils.isBlank(vroot_alt_chr)) {
				vroot_alt_chr = vroot_h_chr;
			}
			vroot_h = StringUtils.strip(vroot_h);
			vroot_alt = StringUtils.strip(vroot_alt);
			vroot_h_chr = StringUtils.strip(vroot_h_chr);
			vroot_alt_chr = StringUtils.strip(vroot_alt_chr);

			boolean v_imp = vdef_active.toLowerCase().startsWith("let");
			boolean v_inf = vdef_active.toLowerCase().startsWith("for");
			if (StringUtils.isBlank(vdef_active)) {
				v_imp = vdef_passive.toLowerCase().startsWith("let");
				v_inf = vdef_passive.toLowerCase().startsWith("for");
			}
			boolean use_di_prefixed_forms = vtypes.contains("adj") || v_imp || v_inf;

			boolean aStem = vroot_h.matches("[ạaẠA].*");
			boolean eStem = vroot_h.matches("[ẹeẸE].*");
			boolean iStem = vroot_h.matches("[ịiỊI].*");
			boolean oStem = vroot_h.matches("[ọoỌO].*");
			boolean uStem = vroot_h.matches("[ụuỤU].*");
			boolean vStem = vroot_h.matches("[ṿvṾV].*");
			boolean cStem = !(aStem | eStem | iStem | oStem | uStem | vStem);

			String vgroup = vroot_h_chr;

			final Iterator<String[]> ipro = getPronouns().iterator();
			while (ipro.hasNext()) {
				String[] pronoun = ipro.next();
				assert pronoun!=null;
				boolean pSetB = pronoun[5].equalsIgnoreCase("b");
				boolean pSetA = pronoun[5].equalsIgnoreCase("a");
				if (pSetB && !vSetB) {
					continue;
				}
				if (pSetA && vSetB) {
					continue;
				}
				String vtmode = pronoun[0];
				String syllabary = pronoun[1];
				ptypes.clear();
				ptypes.addAll(Arrays.asList(vtmode.split(",\\s*")));

				boolean p_g3rd = false;
				if (ptypes.contains("g")) {
					p_g3rd = true;
					ptypes.remove("g");
				}

				if (Collections.disjoint(vtypes, ptypes)) {
					continue;
				}
				vroot.setLength(0);
				vroot_chr.setLength(0);
				if (ptypes.contains("alt")) {
					vroot.append(vroot_alt);
					vroot_chr.append(vroot_alt_chr);
				} else {
					vroot.append(vroot_h);
					vroot_chr.append(vroot_h_chr);
				}

				d.chr = syllabary;
				d.latin = pronoun[2];
				d.def = "";

				String pgroup = d.chr;

				if (use_di_prefixed_forms) {
					if (!StringUtils.isBlank(pronoun[6])) {
						d.chr = pronoun[6];
						d.latin = pronoun[7];
					}
				}

				/*
				 * a vs ga select
				 */
				if (v_g3rd && p_g3rd) {
					d.chr = StringUtils.substringAfter(d.chr, ",");
					d.chr = StringUtils.strip(d.chr);
					d.latin = StringUtils.substringAfter(d.latin, ",");
					d.latin = StringUtils.strip(d.latin);
				}

				/*
				 * a vs ga select
				 */
				if (!v_g3rd && p_g3rd) {
					d.chr = StringUtils.substringBefore(d.chr, ",");
					d.chr = StringUtils.strip(d.chr);
					d.latin = StringUtils.substringBefore(d.latin, ",");
					d.latin = StringUtils.strip(d.latin);
				}

				if (!cStem && d.chr.contains(",")) {
					/*
					 * select vowel stem pronoun
					 */
					d.chr = StringUtils.substringAfter(d.chr, ",");
					d.chr = StringUtils.substringBefore(d.chr, "-");
					d.chr = StringUtils.strip(d.chr);

					d.latin = StringUtils.substringAfter(d.latin, ",");
					d.latin = StringUtils.substringBefore(d.latin, "-");
					d.latin = StringUtils.strip(d.latin);

				} else {
					/*
					 * select consonent stem pronoun
					 */
					d.chr = StringUtils.substringBefore(d.chr, ",");
					d.chr = StringUtils.substringBefore(d.chr, "-");
					d.chr = StringUtils.strip(d.chr);
					d.latin = StringUtils.substringBefore(d.latin, ",");
					d.latin = StringUtils.substringBefore(d.latin, "-");
					d.latin = StringUtils.strip(d.latin);
				}

				if ((v_imp || v_inf) && aStem) {
					if (d.chr.equals("Ꮨ̣²")) {
						// game.log(this, "ti -> t");
						d.chr = "Ꮤ͓";
						d.latin = "t";
					}
					if (d.chr.equals("Ꮧ̣²")) {
						// game.log(this, "di -> d");
						d.chr = "Ꮣ͓";
						d.latin = "d";
					}
				}

				/*
				 * pronoun munge for vowel verb stems where we selected single use case
				 * consonent stem
				 */
				if (!cStem) {
					d.chr = d.chr.replaceAll(SpecialChars.UNDERDOT + "?[¹²³⁴]$", SpecialChars.UNDERX);
					d.latin = d.latin.replaceAll("[ẠAạaẸEẹeỊIịiỌOọoỤUụuṾVṿv][¹²³⁴]$", "");
				}

				/*
				 * "[d]" reflexive form fixups
				 */
				if (cStem) {
					d.chr = d.chr.replaceAll("\\[Ꮣ͓\\]$", "");
					d.latin = d.latin.replaceAll("\\[d\\]$", "");
				}
				if (aStem) {
					d.chr = d.chr.replaceAll("Ꮣ[¹²³⁴]\\[Ꮣ͓\\]$", "Ꮣ͓");
					d.latin = d.latin.replaceAll("da[¹²³⁴]\\[d\\]$", "d");
				}
				if (eStem || iStem || oStem || uStem) {
					d.chr = d.chr.replaceAll("\\[Ꮣ͓\\]$", "Ꮣ͓");
					d.chr = d.chr.replace("Ꮣ͓Ꭱ", "Ꮥ");
					d.chr = d.chr.replace("Ꮣ͓Ꭲ", "Ꮧ");
					d.chr = d.chr.replace("Ꮣ͓Ꭳ", "Ꮩ");
					d.chr = d.chr.replace("Ꮣ͓Ꭴ", "Ꮪ");

					d.latin = d.latin.replaceAll("\\[d\\]$", "d");
				}
				if (vStem) {
					d.chr = d.chr.replaceAll("\\[Ꮣ͓\\]$", "Ꮣ͓");
					d.chr = d.chr.replace("Ꮣ͓Ꭵ", "Ꮫ");
					d.latin = d.latin.replaceAll("\\[d\\]$", "d");
				}

				/*
				 * combine stem and pronouns together
				 */

				if (cStem) {

					if (vroot.toString().matches("[TtDdSs].*")) {
						if (d.latin.equalsIgnoreCase("A¹gị²")) {
							d.latin = "a¹k";
							d.chr = "Ꭰ¹Ꭹ͓";
						}
						if (d.latin.equalsIgnoreCase("Jạ²")) {
							d.latin = "ts";
							d.chr = "Ꮳ͓";
						}
					}
					if (vroot.toString().matches("[Tt].*")) {
						if (d.latin.equalsIgnoreCase("I¹ji²")) {
							d.latin = "i¹jch";
							d.chr = "Ꭲ¹Ꮵ͓";
						}
					}

					if (vroot.length() > 1) {
						char vroot_0 = vroot.charAt(0);
						char vroot_1 = vroot.charAt(1);
						if (vroot_0 == 'ɂ' && // glottal stop followed by tone marking
								(vroot_1 == '¹' || vroot_1 == '²' || vroot_1 == '³' || vroot_1 == '⁴')) {
							d.chr = d.chr.replaceAll("[¹²³⁴]+$", "");
							if (!d.chr.endsWith(SpecialChars.UNDERDOT)) {
								d.chr += SpecialChars.UNDERDOT;
							}
						}
					}

					d.chr += vroot_chr;
					// d.chr = d.chr.replaceAll("[¹²³⁴](?=ɂ?[¹²³⁴])", "");
					d.chr = d.chr.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");

					d.latin += vroot;
					// d.latin = d.latin.replaceAll("[¹²³⁴](?=ɂ?[¹²³⁴])", "");
					d.latin = d.latin.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
				}

				if (aStem) {
					u_check: {
						if (d.chr.equals("Ꭴ¹Ꮹ͓")) {
							d.chr = "Ꭴ" + vroot_chr.substring(1);
							d.latin = "u" + vroot.substring(1);
							break u_check;
						}
						if (d.chr.equals("Ꮷ²Ꮹ͓")) {
							d.chr = "Ꮷ" + vroot_chr.substring(1);
							d.latin = "ju" + vroot.substring(1);
							break u_check;
						}
						if (d.chr.equals("Ꮪ²Ꮹ͓")) {
							d.chr = "Ꮪ" + vroot_chr.substring(1);
							d.latin = "du" + vroot.substring(1);
							break u_check;
						}
						d.chr += vroot_chr;
						d.latin += vroot;
					}
					d.chr = d.chr.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
					d.latin = d.latin.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
				}

				if (eStem || iStem || oStem || uStem) {
					d.chr += vroot_chr;
					// d.chr = d.chr.replaceAll("[¹²³⁴](?=ɂ?[¹²³⁴])", "");
					d.chr = d.chr.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");

					d.latin += vroot;
					// d.latin = d.latin.replaceAll("[¹²³⁴](?=ɂ?[¹²³⁴])", "");
					d.latin = d.latin.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
				}

				if (vStem) {
					u_check: {
						if (d.chr.equals("Ꭴ¹Ꮹ͓")) {
							d.chr = "Ꭴ̣²Ꮹ" + vroot_chr.substring(1);
							d.chr = d.chr.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
							d.latin = "ụ²wa" + vroot.substring(1);
							d.latin = d.latin.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
							break u_check;
						}
						if (d.chr.equals("Ꮷ²Ꮹ͓")) {
							d.chr = "Ꮷ̣²Ꮹ" + vroot_chr.substring(1);
							d.chr = d.chr.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
							d.latin = "jụ²wa" + vroot.substring(1);
							d.latin = d.latin.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
							break u_check;
						}
						if (d.chr.equals("Ꮪ²Ꮹ͓")) {
							d.chr = "Ꮪ̣²Ꮹ" + vroot_chr.substring(1);
							d.chr = d.chr.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
							d.latin = "dụ²wa" + vroot.substring(1);
							d.latin = d.latin.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
							break u_check;
						}
						d.chr += vroot_chr;
						d.latin += vroot;
					}
					d.chr = d.chr.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
					d.latin = d.latin.replaceAll("[¹²³⁴](?=ɂ[¹²³⁴])", "");
				}

				doSyllabaryConsonentVowelFixes(d);

				d.latin = d.latin.toLowerCase();
				String subj = pronoun[3];
				String obj = pronoun[4];

				if (!StringUtils.isBlank(subj) && isPluralSubj(subj)) {
					if (vtypes.contains("xde")) {
						addDePrefix(d);
					}
					if (vtypes.contains("xdi")) {
						addDiPrefix(d, aStem);
					}
				} else if (isPluralSubj(obj)) {
					if (vtypes.contains("xde")) {
						addDePrefix(d);
					}
					if (vtypes.contains("xdi")) {
						addDiPrefix(d, aStem);
					}
				}

				if (vtypes.contains("xwi")) {
					addWiPrefix(d);
				}

				if (v_imp && !vtypes.contains("xwi")) {
//					if (!isOnlyYou(subj, obj)) {
//						addWiPrefix(d);
//					}
					if (!isIncludesYou(subj, obj)) {
						addWiPrefix(d);
					}
				}

				d.def = null;
				if (!StringUtils.isEmpty(subj)) {
					d.def = vdef_active;
					if (d.def.startsWith("he ") || d.def.startsWith("He ")) {
						d.def = d.def.replaceFirst("^[hH]e ", pronoun[3] + " ");
					}
					if (d.def.contains("self")) {
						d.def = d.def.replaceFirst("^[Hh]im", pronoun[3] + "-");
						d.def = d.def.replace("I-self", "Myself");
						d.def = d.def.replace("You one-self", "Your one self");
						d.def = d.def.replace("He-self", "Himself");
						d.def = d.def.replace("We-self", "Ourselves");
						d.def = d.def.replace("we-self", "ourselves");
						d.def = d.def.replace("You two-self", "Your two selves");
						d.def = d.def.replace("You all-self", "Your all selves");
						d.def = d.def.replace("They-self", "Themselves");
						d.def = d.def.replace("our-self", "ourselves");
					}
					if (d.def.matches("^His\\b.*")) {
						String replaceFirst = d.def.replaceFirst("^His\\b", "");
						d.def = pronoun[8] + replaceFirst;
					}
					if (subj.contains("I")) {
						d.def = d.def.replace("[s]", "");
					}
					if (subj.contains("You one")) {
						d.def = d.def.replace("[s]", "");
					}
					if (isPluralSubj(subj)) {
						d.def = d.def.replace("[s]", "");
					} else {
						d.def = d.def.replace("[s]", "s");
					}
					if (d.def.startsWith("for him ") || d.def.startsWith("For him ")) {
						if (!subj.startsWith("I")) {
							subj = StringUtils.left(subj, 1).toLowerCase() + StringUtils.substring(subj, 1);
						}
						d.def = d.def.replaceFirst("^[Ff]or him ", "For " + subj + " ");
					}
					if (d.def.matches("[Ll]et him.*")) {
						if (!subj.startsWith("I")) {
							subj = StringUtils.left(subj, 1).toLowerCase() + StringUtils.substring(subj, 1);
						}
						d.def = d.def.replaceFirst("^[Ll]et him ", "Let " + subj + " ");
					}
					if (!StringUtils.isBlank(vdef_objects)) {
						String[] o = vdef_objects.split(",\\s*");
						String vobj = o[0];
						if (o.length > 1 && obj.contains("them")) {
							vobj = o[1];
						}
						d.def = d.def.replaceAll("\\bx\\b", vobj);
					} else {
						d.def = d.def.replaceAll("\\bx\\b", obj);
					}

				} else {
					d.def = vdef_passive;
					if (d.def.startsWith("he ") || d.def.startsWith("He ")) {
						d.def = d.def.replaceFirst("^[hH]e ", obj + " ");
					}
					if (obj.contains("I")) {
						d.def = d.def.replace("[s]", "");
					}
					if (isPluralSubj(obj)) {
						d.def = d.def.replace("[s]", "");
					} else {
						d.def = d.def.replace("[s]", "s");
					}
					if (d.def.startsWith("for him ") || d.def.startsWith("For him ")) {
						d.def = d.def.replaceFirst("^[Ff]or him ", "For " + obj + " ");
					}
					if (d.def.matches("[Ll]et him.*")) {
						if (!obj.startsWith("I")) {
							obj = StringUtils.left(obj, 1).toLowerCase() + StringUtils.substring(obj, 1);
						}
						d.def = d.def.replaceFirst("^[Ll]et him ", "Let " + obj + " ");
					}
				}
				CorpusEntry c = getCardByChallenge(d.chr, deck);
				if (c == null) {
					c = new CorpusEntry();
					deck.getCards().add(c);
					c.setVgroup(vgroup);
					c.setPgroup(pgroup);
					c.getChr().add(d.chr);
					c.getChr().add(d.latin);
				}

				definitionEnglishFixer(d);

				if (c.getEn().contains(d.def)) {
					System.err.println("=== WARNING! DUPLICATE DEFINITION: " + d.chr + ", " + d.def);
				} else {
					c.getEn().add(d.def);
				}

			}
		}
	}

	public static class DataSet {
		public String chr;
		public String latin;
		public String def;
	}

	private static CorpusEntry getCardByChallenge(String chr, CorpusEntries deck) {
		for (CorpusEntry card : deck.getCards()) {
			if (card.getChr().get(0).equalsIgnoreCase(chr)) {
				return card;
			}
		}
		return null;
	}

	private static void doSyllabaryConsonentVowelFixes(DataSet d) {
		final String x = SpecialChars.UNDERX;
		if (!d.chr.contains(x)) {
			return;
		}
		if (!d.chr.matches(".*" + x + "[ᎠᎡᎢᎣᎤᎥ].*")) {
			return;
		}
		// special case for u + v => uwa
		d.chr = d.chr.replace("Ꭴ¹Ꮹ͓Ꭵ", "Ꭴ̣²Ꮹ");

		String set;
		set = "[Ꭰ-Ꭵ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꭰ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ꭱ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ꭲ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ꭳ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ꭴ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ꭵ");

		set = "[Ꭷ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꭷ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ꭸ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ꭹ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ꭺ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ꭻ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ꭼ");

		set = "[ᎦᎨᎩᎪᎫᎬ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꭶ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ꭸ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ꭹ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ꭺ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ꭻ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ꭼ");

		set = "[ᎭᎮᎯᎰᎱᎲ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꭽ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ꭾ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ꭿ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ꮀ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ꮁ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ꮂ");

		set = "[ᎾᏁᏂᏃᏄᏅ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꮎ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ꮑ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ꮒ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ꮓ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ꮔ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ꮕ");

		set = "[Ꮏ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꮏ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ꮑ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ꮒ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ꮓ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ꮔ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ꮕ");

		set = "[ᏣᏤᏥᏦᏧᏨ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꮳ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ꮴ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ꮵ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ꮶ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ꮷ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ꮸ");

		set = "[ᏆᏇᏈᏉᏊᏋ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꮖ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ꮗ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ꮘ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ꮙ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ꮚ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ꮛ");

		set = "[ᏓᏕᏗᏙᏚᏛ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꮣ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ꮥ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ꮧ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ꮩ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ꮪ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ꮫ");

		set = "[ᏔᏖᏘ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꮤ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ꮦ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ꮨ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ꮩ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ꮪ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ꮫ");

		set = "[ᏩᏪᏫᏬᏭᏮ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꮹ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ꮺ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ꮻ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ꮼ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ꮽ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ꮾ");

		set = "[ᏯᏰᏱᏲᏳᏴ]";
		d.chr = d.chr.replaceAll(set + x + "Ꭰ", "Ꮿ");
		d.chr = d.chr.replaceAll(set + x + "Ꭱ", "Ᏸ");
		d.chr = d.chr.replaceAll(set + x + "Ꭲ", "Ᏹ");
		d.chr = d.chr.replaceAll(set + x + "Ꭳ", "Ᏺ");
		d.chr = d.chr.replaceAll(set + x + "Ꭴ", "Ᏻ");
		d.chr = d.chr.replaceAll(set + x + "Ꭵ", "Ᏼ");
	}

	private static boolean isPluralSubj(String subj) {
		boolean pluralSubj = subj.contains(" and");
		pluralSubj |= subj.startsWith("they");
		pluralSubj |= subj.startsWith("They");
		pluralSubj |= subj.contains(" two");
		pluralSubj |= subj.contains(" all");
		return pluralSubj;
	}

	private static void addDiPrefix(DataSet d, boolean aStem) {
		if (d.latin.matches("[ạ].*") && aStem) {
			d.latin = "dạ" + d.latin.substring(1);
			d.chr = "Ꮣ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[a].*") && aStem) {
			d.latin = "da" + d.latin.substring(1);
			d.chr = "Ꮣ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ạa].*")) {
			d.latin = "dị" + d.latin.substring(1);
			d.chr = "Ꮧ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ẹe].*")) {
			d.latin = "j" + d.latin;
			d.chr = "Ꮴ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ọo].*")) {
			d.latin = "j" + d.latin;
			d.chr = "Ꮶ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ụu].*")) {
			d.latin = "j" + d.latin;
			d.chr = "Ꮷ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ṿv].*")) {
			d.latin = "j" + d.latin;
			d.chr = "Ꮸ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ịi].*")) {
			d.latin = "d" + d.latin;
			d.chr = "Ꮧ" + d.chr.substring(1);
			return;
		}

		d.latin = "dị²" + d.latin;
		d.chr = "Ꮧ̣²" + d.chr;

	}

	private static void addWiPrefix(DataSet d) {
		if (d.latin.matches("[ạa].*")) {
			d.latin = "w" + d.latin;
			d.chr = "Ꮹ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ẹe].*")) {
			d.latin = "w" + d.latin;
			d.chr = "Ꮺ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ọo].*")) {
			d.latin = "w" + d.latin;
			d.chr = "Ꮼ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ụu].*")) {
			d.latin = "w" + d.latin;
			d.chr = "Ꮽ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ṿv].*")) {
			d.latin = "w" + d.latin;
			d.chr = "Ꮾ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ịi].*")) {
			d.latin = "w" + d.latin;
			d.chr = "Ꮻ" + d.chr.substring(1);
			return;
		}
		d.latin = "wị²" + d.latin;
		d.chr = "Ꮻ̣²" + d.chr;
	}

	private static void addDePrefix(DataSet d) {
		if (d.latin.matches("[ạa].*")) {
			d.latin = "d" + d.latin;
			d.chr = "Ꮣ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ẹe].*")) {
			d.latin = "d" + d.latin;
			d.chr = "Ꮥ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ọo].*")) {
			d.latin = "d" + d.latin;
			d.chr = "Ꮩ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ụu].*")) {
			d.latin = "d" + d.latin;
			d.chr = "Ꮪ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ṿv].*")) {
			d.latin = "d" + d.latin;
			d.chr = "Ꮫ" + d.chr.substring(1);
			return;
		}
		if (d.latin.matches("[ịi].*")) {
			d.latin = "de³" + d.latin.substring(2);
			d.chr = "Ꮥ³" + d.chr.substring(2);
			return;
		}

		d.latin = "de²" + d.latin;
		d.chr = "Ꮥ²" + d.chr;

	}

	private static boolean isIncludesYou(String subj, String obj) {
		if (StringUtils.isBlank(subj)) {
			subj = obj;
		}
		subj = subj.toLowerCase();
		return subj.matches(".*\\byou\\b.*");
	}

	private static void definitionEnglishFixer(DataSet d) {
		d.def = d.def.replace("xHe", "He");
		d.def = d.def.replace("xShe", "She");
		d.def = d.def.replace("xhe", "he");
		d.def = d.def.replace("xshe", "she");
		
		d.def = StringUtils.left(d.def, 1).toUpperCase() + StringUtils.substring(d.def, 1);

		d.def = d.def.replaceAll("\\b([Uu]s)(, .*?)( recently)", "$1$3$2");

		d.def = d.def.replaceAll("([Ww]e)( .*? | )is ", "$1$2are ");
		d.def = d.def.replaceAll("([Ww]e)( .*? | )was ", "$1$2were ");
		d.def = d.def.replaceAll("([Ww]e)( .*? | )has ", "$1$2have ");

		d.def = d.def.replace("and I is", "and I are");
		d.def = d.def.replace("I is", "I am");
		d.def = d.def.replace("You one is", "You one are");
		d.def = d.def.replace("You two is", "You two are");
		d.def = d.def.replace("You all is", "You all are");
		d.def = d.def.replace("They is", "They are");

		d.def = d.def.replace("and I was", "and I were");
		d.def = d.def.replace("You one was", "You one were");
		d.def = d.def.replace("You two was", "You two were");
		d.def = d.def.replace("You all was", "You all were");
		d.def = d.def.replace("They was", "They were");

		d.def = d.def.replace("and I often is", "and I often are");
		d.def = d.def.replace("I often is", "I often am");
		d.def = d.def.replace("You one often is", "You one often are");
		d.def = d.def.replace("You two often is", "You two often are");
		d.def = d.def.replace("You all often is", "You all often are");
		d.def = d.def.replace("They often is", "They often are");

		d.def = d.def.replace("and I has", "and I have");
		d.def = d.def.replace("I has", "I have");
		d.def = d.def.replace("You one has", "You one have");
		d.def = d.def.replace("You two has", "You two have");
		d.def = d.def.replace("You all has", "You all have");
		d.def = d.def.replace("They has", "They have");

		d.def = d.def.replace("and I often has", "and I often have");
		d.def = d.def.replace("I often has", "I often have");
		d.def = d.def.replace("You one often has", "You one often have");
		d.def = d.def.replace("You two often has", "You two often have");
		d.def = d.def.replace("You all often has", "You all often have");
		d.def = d.def.replace("They often has", "They often have");

		if (d.def.startsWith("For")) {
			d.def = d.def.replaceAll("\\b[Hh]e\\b", "him");
			d.def = d.def.replaceAll("\\b[Tt]hey\\b", "them");
			d.def = d.def.replaceAll("\\bI\\b", "me");
			d.def = d.def.replaceAll("\\bYou\\b", "you");
			d.def = d.def.replaceAll("For (.*?), [Ww]e\\b", "For us, $1,");
		}

		if (d.def.startsWith("Let")) {
			d.def = d.def.replaceAll("Let [Hh]e\\b", "Let him");
			d.def = d.def.replaceAll("Let [Tt]hey\\b", "Let them");
			d.def = d.def.replaceAll("Let You\\b", "Let you");
			d.def = d.def.replaceAll("Let I\\b", "Let me");
			d.def = d.def.replaceAll("and I\\b", "and me");
			d.def = d.def.replaceAll("Let You\\b", "Let you");
			d.def = d.def.replaceAll("Let (.*?), we\\b", "Let us, $1,");
		}
	}
	
}
