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

import lombok.AllArgsConstructor;
import lombok.Value;

import org.springframework.hateoas.affordance.Suggestions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Describe a parameter for the associated state transition in a HAL-FORMS document. A {@link Template} may contain a
 * list of {@link Property}
 * 
 * @see http://mamund.site44.com/misc/hal-forms/
 */
@JsonInclude(Include.NON_DEFAULT)
@Value
@AllArgsConstructor
public class Property {

	String name;
	Boolean readOnly;
	String value, prompt, regex;
	boolean templated, required, multi;
	Suggestions suggest;

	Property() {
		this(null, null, null, null, null, false, false, false, null);
	}
}
