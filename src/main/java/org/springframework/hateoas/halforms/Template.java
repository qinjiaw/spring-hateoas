/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.halforms;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Wither;

import org.springframework.hateoas.halforms.Jackson2HalFormsModule.MediaTypesDeserializer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Value object for a HAL-FORMS template. Describes the available state transition details.
 *
 * @author Dietrich Schulten
 * @see http://mamund.site44.com/misc/hal-forms/
 */
@JsonInclude(Include.NON_DEFAULT)
@JsonPropertyOrder({ "title", "method", "contentType", "properties" })
@JsonIgnoreProperties({ "key" })
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Template {

	public static final String DEFAULT_KEY = "default";

	private final @Getter List<Property> properties;
	private final @Wither @Getter String key;

	private @Setter HttpMethod method;

	@JsonDeserialize(using = MediaTypesDeserializer.class) //
	private @Setter List<MediaType> contentType;
	private @Setter @Getter String title;

	public Template() {
		this(Template.DEFAULT_KEY);
	}

	public Template(String key) {

		this.key = key != null ? key : Template.DEFAULT_KEY;
		this.properties = new ArrayList<Property>();
	}

	public Property getProperty(String propertyName) {

		for (Property property : properties) {
			if (property.getName().equals(propertyName)) {
				return property;
			}
		}

		return null;
	}

	public String getContentType() {
		return StringUtils.collectionToCommaDelimitedString(contentType);
	}

	@JsonProperty("method")
	public String getMethod() {
		return method == null ? null : method.toString();
	}
}
