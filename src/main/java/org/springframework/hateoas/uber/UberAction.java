/*
 * Copyright 2014-2017 the original author or authors.
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

package org.springframework.hateoas.uber;

import org.springframework.http.HttpMethod;

/**
 * @author Dietrich Schulten
 */
public enum UberAction {

	/**
	 * POST
	 */
	APPEND(HttpMethod.POST),
	/**
	 * PATCH
	 */
	PARTIAL(HttpMethod.PATCH),
	/**
	 * GET
	 */
	READ(HttpMethod.GET),
	/**
	 * DELETE
	 */
	REMOVE(HttpMethod.DELETE),
	/**
	 * PUT
	 */
	REPLACE(HttpMethod.PUT);

	private final HttpMethod httpMethod;

	private UberAction(HttpMethod method) {
		this.httpMethod = method;
	}

	/**
	 * Maps given request method to uber action, GET will be mapped as null since it is the default.
	 *
	 * @param method to map
	 * @return action, or null for GET
	 */
	public static UberAction forRequestMethod(HttpMethod method) {

		if (HttpMethod.GET == method) {
			return null;
		}

		for (UberAction action : UberAction.values()) {
			if (action.httpMethod == method) {
				return action;
			}
		}

		throw new IllegalArgumentException("unsupported method: " + method);
	}
}
