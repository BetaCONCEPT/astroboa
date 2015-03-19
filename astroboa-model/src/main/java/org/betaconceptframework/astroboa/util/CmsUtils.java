/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.betaconceptframework.astroboa.util;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 */
public class CmsUtils {

  public static String replaceLast(String find, String replace, String source) {
    String result = "";

    if (source.indexOf(find) >= 0) {
      result += source.substring(0, source.lastIndexOf(find)) + replace;
      source = source.substring(source.lastIndexOf(find) + find.length());
    }
    result += source;

    return result;
  }

  public static int getNextDepth(int depth) {
    int nextDepth = TreeDepth.ZERO.asInt();
    if (depth == TreeDepth.FULL.asInt())
      nextDepth = TreeDepth.FULL.asInt();
    else if (depth != TreeDepth.ZERO.asInt())
      nextDepth = depth - 1;
    return nextDepth;
  }

  public static String filterGreekCharacters(String greekLocalizedLabel) {
    char[] chArray = greekLocalizedLabel.toCharArray();
    for (int i = 0; i < chArray.length; i++) {
      chArray[i] = toLowerCase(chArray[i]);
    }

    return new String(chArray);
  }


  public static char toLowerCase(char letter) {
    int codePoint = String.valueOf(letter).codePointAt(0);

    // First deal with lower case, not accented letters
    if (letter >= '\u03B1' && letter <= '\u03C9') {
      // Special case 'small final sigma', where we return 'small sigma'
      if (letter == '\u03C2') {
        return '\u03C3';
      } else {
        return letter;
      }
    }

    // "ῖ" "Ἰ" "ἱ" "Ἱ" "ἴ"
    if (codePoint == 8150 ||
        codePoint == 7992 ||
        codePoint == 7985 ||
        codePoint == 7993 ||
        codePoint == 7988) {
      return "ι".charAt(0);
    }

    // "Ἀ" "ἁ" "Ἄ" "ἄ" "Ἅ" "ἀ"
    if (codePoint == 7944 ||
        codePoint == 7937 ||
        codePoint == 7948 ||
        codePoint == 7940 ||
        codePoint == 7949 ||
        codePoint == 7936) {
      return "α".charAt(0);
    }

    // "ἐ" "Ἐ" "ἑ" "Ἑ" "ἔ" "ἕ"
    if (codePoint == 7952 ||
        codePoint == 7960 ||
        codePoint == 7953 ||
        codePoint == 7961 ||
        codePoint == 7956 ||
        codePoint == 7957) {
      return "ε".charAt(0);
    }

    // "Ἡ" "ἡ" "ἥ"
    if (codePoint == 7977 ||
        codePoint == 7969 ||
        codePoint == 7973) {
      return "η".charAt(0);
    }

    // "Ὁ" "ὁ" "ὅ"
    if (codePoint == 8009 ||
        codePoint == 8001 ||
        codePoint == 8005) {
      return "ο".charAt(0);
    }

    // "Ὑ" "ὕ"
    if (codePoint == 8025 ||
        codePoint == 8021) {
      return "υ".charAt(0);
    }

    // "ὡ"
    if (codePoint == 8033) {
      return "υ".charAt(0);
    }

    if (letter == '\u03AC') {
      return '\u03B1';
    }
    // epsilon with acute
    if (letter == '\u03AD') {
      return '\u03B5';
    }
    // eta with acute
    if (letter == '\u03AE') {
      return '\u03B7';
    }
    // iota with acute, iota with diaeresis, iota with acute and diaeresis
    if (letter == '\u03AF' || letter == '\u03CA' || letter == '\u0390') {
      return '\u03B9';
    }
    // upsilon with acute, upsilon with diaeresis, upsilon with acute and diaeresis
    if (letter == '\u03CD' || letter == '\u03CB' || letter == '\u03B0') {
      return '\u03C5';
    }
    // omicron with acute
    if (letter == '\u03CC') {
      return '\u03BF';
    }
    // omega with acute
    if (letter == '\u03CE') {
      return '\u03C9';
    }
    // After that, deal with upper case, not accented letters
    if (letter >= '\u0391' && letter <= '\u03A9') {
      return (char) (letter + 32);
    }
    // Finally deal with upper case, accented letters
    // alpha with acute
    if (letter == '\u0386') {
      return '\u03B1';
    }
    // epsilon with acute
    if (letter == '\u0388') {
      return '\u03B5';
    }
    // eta with acute
    if (letter == '\u0389') {
      return '\u03B7';
    }
    // iota with acute, iota with diaeresis
    if (letter == '\u038A' || letter == '\u03AA') {
      return '\u03B9';
    }
    // upsilon with acute, upsilon with diaeresis
    if (letter == '\u038E' || letter == '\u03AB') {
      return '\u03C5';
    }
    // omicron with acute
    if (letter == '\u038C') {
      return '\u03BF';
    }
    // omega with acute
    if (letter == '\u038F') {
      return '\u03C9';
    }


    return Character.toLowerCase(letter);
  }

}
