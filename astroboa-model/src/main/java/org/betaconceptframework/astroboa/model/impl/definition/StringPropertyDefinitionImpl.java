
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

package org.betaconceptframework.astroboa.model.impl.definition;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.definition.StringFormat;
import org.betaconceptframework.astroboa.api.model.definition.StringPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.CmsPasswordEncryptor;
import org.betaconceptframework.astroboa.model.impl.item.CmsDefinitionItem;
import org.betaconceptframework.astroboa.security.AstroboaPasswordEncryptor;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class StringPropertyDefinitionImpl extends SimpleCmsPropertyDefinitionImpl<String> implements StringPropertyDefinition, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3708205858642356172L;

	public final Pattern pattern;
	
	private final Integer maxLength;
	
	private final Integer minLength;
	
	private final StringFormat stringFormat;

	private final String passwordEncryptorClassName;

	private final boolean passwordType;

	private CmsPasswordEncryptor cmsPasswordEncryptor;

	public StringPropertyDefinitionImpl(QName qualifiedName, Localization description,
			Localization displayName, boolean obsolete, boolean multiple,
			boolean mandatory, Integer order, String restrictReadToRoles,
			String restrictWriteToRoles, CmsDefinition parentDefinition,
			String defaultValue, String repositoryObjectRestriction, 
			Integer maxLength,Integer minLength, StringFormat stringFormat, 
			Map<String, Localization> valueRange, 
			String passwordEncryptorClassName, boolean passwordType, String patternExpression) {
		super(qualifiedName, description, displayName, obsolete, multiple, mandatory,order, 
				restrictReadToRoles, restrictWriteToRoles, parentDefinition,
				defaultValue, repositoryObjectRestriction, valueRange);

		
		if (stringFormat == null){
			this.stringFormat = StringFormat.PlainText;
		}
		else{
			this.stringFormat = stringFormat;
		}

		if (StringUtils.isNotBlank(patternExpression)){
			this.pattern = Pattern.compile(patternExpression);
		}
		else{
			this.pattern = null;
		}
		
		this.minLength = minLength;

		this.maxLength = maxLength;
		
		
		if (this.minLength != null && this.maxLength != null && this.minLength > this.maxLength){
			throw new CmsException("Invalid min "+this.minLength +" and max "+this.maxLength+ " for property "+getName());
		}
		

		//Check if default is within range
		if (defaultValue!=null && !isValueValid(defaultValue)){
			throw new CmsException("Default value "+defaultValue + " is not within value range "+ getValueEnumeration());
		}

		this.passwordEncryptorClassName = passwordEncryptorClassName;

		this.passwordType = passwordType;

		if (!passwordType && StringUtils.isNotBlank(passwordEncryptorClassName)){
			throw new CmsException("Property "+getName()+ " is not of type "+ CmsDefinitionItem.passwordType.getJcrName() + " and yet "+
			" password encryptor class is defined");
		}

		if (passwordType){

			try{
				if (StringUtils.isBlank(passwordEncryptorClassName)){
					cmsPasswordEncryptor = new AstroboaPasswordEncryptor();
				}
				else{
					ClassLoader loader = Thread.currentThread().getContextClassLoader();
					Class clazz = loader.loadClass(passwordEncryptorClassName);

					cmsPasswordEncryptor= (CmsPasswordEncryptor) clazz.newInstance();

				}
			}
			catch(Exception e){
				throw new CmsException(e);
			}
		}

	}

	@Deprecated
	public Integer getMaxStringLength() {
		return getMaxLength();
	}

	public StringFormat getStringFormat() {
		return stringFormat;
	}

	public ValueType getValueType() {
		return ValueType.String;
	}

	@Override
	public StringPropertyDefinition clone(
			ComplexCmsPropertyDefinition parentDefinition) {

		return new StringPropertyDefinitionImpl(getQualifiedName(), cloneDescription(), cloneDisplayName(), isObsolete(), isMultiple(), isMandatory(),
				getOrder(),
				getRestrictReadToRoles(), getRestrictWriteToRoles(), parentDefinition,
				getDefaultValue(), getRepositoryObjectRestriction(), maxLength, minLength, stringFormat, 
				getValueEnumeration(), 
				passwordEncryptorClassName, passwordType, 
				pattern!=null? pattern.pattern() : null);
	}


	public CmsPasswordEncryptor getPasswordEncryptor() {
		return cmsPasswordEncryptor;
	}

	public boolean isPasswordType() {
		return passwordType;
	}

	@Override
	public Integer getMaxLength() {
		return maxLength;
	}


	@Override
	public Integer getMinLength() {
		return minLength;
	}
	
	@Override
	public boolean isValueValid(String value) {
		
		if (value == null){
			return false;
		}

		boolean valid = true;
		
		if (getValueEnumeration() != null && ! getValueEnumeration().isEmpty()){
			valid = super.isValueValid(value);
		}
		
		if (valid && pattern != null){
			valid = pattern.matcher(value).matches();
		}
		
		if (valid && minLength != null && value.length()<minLength){
			valid = false;
		}
		
		if (valid && maxLength != null && maxLength > 0 && value.length()>maxLength){
			valid = false;
		}
		
		return valid;
	}

	@Override
	public String getPattern() {
		return pattern != null ? pattern.pattern() : null;
	}


}
