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
package org.betaconceptframework.astroboa.security;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.CmsPasswordEncryptor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.commons.CommonUtils;
import org.jasypt.digest.StandardStringDigester;
import org.jasypt.salt.FixedStringSaltGenerator;
import org.slf4j.LoggerFactory;

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
	
	/*
	 * This is the default line separator (CRLF ("\r\n")) used by class
	 * org.apache.commons.codec.binary.Base64 in Apache Commons Codec v1.4
	 * which is used by the StandardStringDigester.
	 * 
	 * There is no method in StandardStringDigester which provides this value
	 * and therefore we have to define it. See method encrypt() for more info about
	 * its use. 
	 * 
	 */
	private static final String Base64_LINE_SEPARATOR = new String(new byte[]{'\r', '\n'});

	
	public AstroboaPasswordEncryptor() {
		initDigester();
	}


	private void initDigester()  {

		if (digester != null){
			return;
		}
		
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

		initDigester();
		
		return this.digester.matches(plainPassword, encryptedPassword);
	}

	public String encrypt(String password) {
		
		initDigester();
		
		String encryptedPass = this.digester.digest(password);
		
		/* Current setup with StandardStringDigester and Base64 from Apache Commons Codec v1.4
		 * adds the Base64_LINE_SEPARATOR  (that is a CRLF ("\r\n"))
		 * at the end of the digested result. 
		 * 
		 *  For example, for the value 'myPassword' the digester will produce
		 *  the following  '2rnNFkiLbEDM+S46twlB3VTwtafMOexbQARYRGLb5aM=\r\n'
		 *  
		 *  If this value is saved as a value of a property, 
		 *  Jackrabbit (JCR implementation used by Astroboa) truncates the CRLF
		 *  and thus, when we get the value back, we get  
		 *  
		 *  '2rnNFkiLbEDM+S46twlB3VTwtafMOexbQARYRGLb5aM='
		 *  
		 *  However, if we call method checkPassword('myPassword', '2rnNFkiLbEDM+S46twlB3VTwtafMOexbQARYRGLb5aM='),
		 *  we get the same result with the call checkPassword('myPassword', '2rnNFkiLbEDM+S46twlB3VTwtafMOexbQARYRGLb5aM=\r\n'), 
		 *  which is TRUE.
		 *  
		 *  This is happening because the digester silently ignores Base64_LINE_SEPARATOR upon decoding!!!
		 *  
		 *  This abnormally, however, may cause problems to every one who wants to check a password (which is a value of a property) 
		 *  without the use of the method checkPassword(). 
		 *  
		 *  In this case, she will get the encrypted value from this method (2rnNFkiLbEDM+S46twlB3VTwtafMOexbQARYRGLb5aM=\r\n), 
		 *  she will get the value stored in the property (2rnNFkiLbEDM+S46twlB3VTwtafMOexbQARYRGLb5aM=) and she will get no match.
		 *  
		 *  Since the CRLF is ignored and does not play any role to decoding, it is safe to return the encrypted value without the CRLF. 
		 *  This way, password checking can be done with the use of this digester.     
		 *  
		 */
		
		if (encryptedPass != null && encryptedPass.endsWith(Base64_LINE_SEPARATOR)){
			return StringUtils.removeEnd(encryptedPass, Base64_LINE_SEPARATOR);
		}
		
		return encryptedPass;
	}

	//Override deserialization process to log any exception thrown 
	//during deserialization
	private void readObject(ObjectInputStream ois)
	throws ClassNotFoundException, IOException {

		//Deserialize bean normally
		try{
			ois.defaultReadObject();
		}
		catch(Exception e){
			LoggerFactory.getLogger(getClass()).warn("Exception thrown while deserializing password encryptor. Digester will be initialized the first time any of the AstroboaPasswordEncryptor method is called");
		}
	}
}
