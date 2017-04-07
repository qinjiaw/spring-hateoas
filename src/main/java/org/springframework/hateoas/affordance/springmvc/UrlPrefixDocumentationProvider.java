package org.springframework.hateoas.affordance.springmvc;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.hateoas.affordance.ActionInputParameter;
import org.springframework.util.Assert;

/**
 * Provides documentation URLs by applying an URL prefix. Created by Dietrich on 27.04.2016.
 */
public class UrlPrefixDocumentationProvider implements DocumentationProvider {

	private String defaultUrlPrefix;

	public UrlPrefixDocumentationProvider(String defaultUrlPrefix) {
		Assert.isTrue(defaultUrlPrefix.endsWith("/") || defaultUrlPrefix.endsWith("#"),
				"URL prefix should end with separator / or #");
		this.defaultUrlPrefix = defaultUrlPrefix;
	}

	public UrlPrefixDocumentationProvider() {
		defaultUrlPrefix = "";
	}

	/*
	 * (non-Javadoc)
	 * @see de.escalon.hypermedia.spring.DocumentationProvider#getDocumentationUrl(de.escalon.hypermedia.affordance.ActionInputParameter, java.lang.Object)
	 */
	@Override
	public String getDocumentationUrl(ActionInputParameter annotatedParameter, Object content) {
		return defaultUrlPrefix + annotatedParameter.getParameterName();
	}

	/*
	 * (non-Javadoc)
	 * @see de.escalon.hypermedia.spring.DocumentationProvider#getDocumentationUrl(java.lang.reflect.Field, java.lang.Object)
	 */
	@Override
	public String getDocumentationUrl(Field field, Object content) {
		return defaultUrlPrefix + field.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see de.escalon.hypermedia.spring.DocumentationProvider#getDocumentationUrl(java.lang.reflect.Method, java.lang.Object)
	 */
	@Override
	public String getDocumentationUrl(Method getter, Object content) {

		String methodName = getter.getName();
		String propertyName = Introspector.decapitalize(methodName.substring(methodName.startsWith("is") ? 2 : 3));
		return defaultUrlPrefix + propertyName;
	}

	/*
	 * (non-Javadoc)
	 * @see de.escalon.hypermedia.spring.DocumentationProvider#getDocumentationUrl(java.lang.Class, java.lang.Object)
	 */
	@Override
	public String getDocumentationUrl(Class<?> clazz, Object content) {
		return defaultUrlPrefix + clazz.getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * @see de.escalon.hypermedia.spring.DocumentationProvider#getDocumentationUrl(java.lang.String, java.lang.Object)
	 */
	@Override
	public String getDocumentationUrl(String name, Object content) {
		return defaultUrlPrefix + name;
	}
}
