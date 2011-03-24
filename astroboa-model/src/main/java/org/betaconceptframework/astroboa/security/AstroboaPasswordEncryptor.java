/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
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
package org.betaconceptframework.astroboa.security;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.CmsPasswordEncryptor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.commons.CommonUtils;
import org.jasypt.digest.StandardStringDigester;
import org.jasypt.salt.FixedStringSaltGenerator;

/**
 * Excerpt taken from http://www.jasypt.org/howtoencryptuserpasswords.html
 * 
 *  
 *
 * I. Encrypt passwords using one-way techniques, this is, digests.
 * II. Match input and stored passwords by comparing digests, not unencrypted strings.
 * III. Use a salt containing at least 8 random bytes, and attach these random bytes, undigested, to the result.
 * IV. Iterate the hash function at least 1,000 times.
 * V. Prior to digesting, perform string-to-byte sequence translation using a fixed encoding, preferably UTF-8.
 * VI. Finally, apply BASE64 encoding and store the digest as an US-ASCII character string.

 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaPasswordEncryptor implements CmsPasswordEncryptor{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6294490471953503116L;

	public static final String DEFAULT_BETACONCEPT_SALT = "@BetaCONCEPT!";
	public static final String DEFAULT_ALGORITHM = "SHA-256";

	private transient StandardStringDigester digester;

	
	public AstroboaPasswordEncryptor() {
		initDigester();
	}


	private void initDigester()  {

		try{
			final FixedStringSaltGenerator fixedStringSaltGenerator = new FixedStringSaltGenerator();
			fixedStringSaltGenerator.setSalt(DEFAULT_BETACONCEPT_SALT);

			digester= new StandardStringDigester();
			digester.setProvider(new BouncyCastleProvider());
			digester.setAlgorithm(DEFAULT_ALGORITHM);
			digester.setIterations(1000);
			digester.setSaltSizeBytes(DEFAULT_BETACONCEPT_SALT.getBytes(digester.DIGEST_CHARSET).length);
			digester.setSaltGenerator(fixedStringSaltGenerator);
			//This is the default value but we set it any way
			digester.setStringOutputType(CommonUtils.STRING_OUTPUT_TYPE_BASE64);
			digester.initialize();

			/**
			 * digester.setUnicodeNormalizationIgnored(unicodeNormalizationIgnored)
			 * 
			 * Excerpt taken from method documentation on why this should be left to its default
			 * 
			 * <p>
			 * Sets whether the unicode text normalization step should be ignored.
			 * </p>
			 * <p>
			 * The Java Virtual Machine internally handles all Strings as UNICODE. When
			 * digesting or matching digests in jasypt, these Strings are first 
			 * <b>normalized to 
			 * its NFC form</b> so that digest matching is not affected by the specific
			 * form in which the messages where input.
			 * </p>
			 * <p>
			 * <b>It is normally safe (and recommended) to leave this parameter set to 
			 * its default FALSE value (and thus DO perform normalization 
			 * operations)</b>. But in some specific cases in which issues with legacy
			 * software could arise, it might be useful to set this to TRUE.
			 * </p>
			 * <p>
			 * For more information on unicode text normalization, see this issue of 
			 * <a href="http://java.sun.com/mailers/techtips/corejava/2007/tt0207.html">Core Java Technologies Tech Tips</a>.
			 * </p>
			 * 
			 */

			this.digester.initialize();
		}
		catch(Exception e){
			throw new CmsException(e);
		}
	}


	public boolean checkPassword(String plainPassword, String encryptedPassword) {

		return this.digester.matches(plainPassword, encryptedPassword);
	}

	public String encrypt(String password) {

		return this.digester.digest(password);
	}

}
