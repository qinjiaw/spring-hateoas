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
package org.springframework.hateoas.affordance.springmvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.affordance.ActionDescriptor;
import org.springframework.hateoas.affordance.ActionInputParameter;
import org.springframework.hateoas.affordance.ActionInputParameterVisitor;
import org.springframework.hateoas.affordance.formaction.Action;
import org.springframework.hateoas.affordance.formaction.DTOParam;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Dietrich Schulten
 */
class ActionDescriptorBuilder {

	static ActionDescriptor createActionDescriptor(Method invokedMethod, Map<String, Object> values, Object... arguments) {

		SpringActionDescriptor actionDescriptor = new SpringActionDescriptor(invokedMethod);
		Action actionAnnotation = AnnotationUtils.getAnnotation(invokedMethod, Action.class);

		if (actionAnnotation != null) {
			actionDescriptor.setSemanticActionType(actionAnnotation.value());
		}

		// the action descriptor needs to know the param type, value and name
		Map<String, ActionInputParameter> requestParamMap = getRequestParams(invokedMethod, arguments);
		for (Map.Entry<String, ActionInputParameter> entry : requestParamMap.entrySet()) {

			ActionInputParameter value = entry.getValue();

			if (value == null) {
				continue;
			}

			String key = entry.getKey();
			actionDescriptor.addRequestParam(key, value);

			if (!value.isRequestBody()) {
				values.put(key, value.getValueFormatted());
			}
		}

		Map<String, ActionInputParameter> pathVariableMap = getActionInputParameters(PathVariable.class, invokedMethod,
				arguments);
		for (Map.Entry<String, ActionInputParameter> entry : pathVariableMap.entrySet()) {

			ActionInputParameter actionInputParameter = entry.getValue();

			if (actionInputParameter == null) {
				continue;
			}

			String key = entry.getKey();
			actionDescriptor.addPathVariable(key, actionInputParameter);

			if (!actionInputParameter.isRequestBody()) {
				values.put(key, actionInputParameter.getValueFormatted());
			}
		}

		Map<String, ActionInputParameter> requestHeadersMap = getActionInputParameters(RequestHeader.class, invokedMethod,
				arguments);

		for (Map.Entry<String, ActionInputParameter> entry : requestHeadersMap.entrySet()) {
			ActionInputParameter actionInputParameter = entry.getValue();
			if (actionInputParameter != null) {
				String key = entry.getKey();
				actionDescriptor.addRequestHeader(key, actionInputParameter);
				if (!actionInputParameter.isRequestBody()) {
					values.put(key, actionInputParameter.getValueFormatted());
				}
			}
		}

		Map<String, ActionInputParameter> requestBodyMap = getActionInputParameters(RequestBody.class, invokedMethod,
				arguments);
		Assert.state(requestBodyMap.size() < 2, "found more than one request body on " + invokedMethod.getName());
		for (ActionInputParameter value : requestBodyMap.values()) {
			actionDescriptor.setRequestBody(value);
		}

		return actionDescriptor;
	}

	/**
	 * Returns {@link ActionInputParameter}s contained in the method link.
	 *
	 * @param annotation to inspect
	 * @param method must not be {@literal null}.
	 * @param arguments to the method link
	 * @return maps parameter names to parameter info
	 */
	private static Map<String, ActionInputParameter> getActionInputParameters(Class<? extends Annotation> annotation,
			Method method, Object... arguments) {

		Assert.notNull(method, "MethodInvocation must not be null!");

		MethodParameters parameters = new MethodParameters(method);
		Map<String, ActionInputParameter> result = new LinkedHashMap<String, ActionInputParameter>();

		for (MethodParameter parameter : parameters.getParametersWith(annotation)) {

			int parameterIndex = parameter.getParameterIndex();
			Object argument = parameterIndex < arguments.length ? arguments[parameterIndex] : null;

			result.put(parameter.getParameterName(),
					new SpringActionInputParameter(parameter, argument, parameter.getParameterName()));
		}

		return result;
	}

	/**
	 * Returns {@link ActionInputParameter}s contained in the method link.
	 *
	 * @param method must not be {@literal null}.
	 * @param arguments to the method link
	 * @return maps parameter names to parameter info
	 */
	private static Map<String, ActionInputParameter> getDtoActionInputParameters(Method method, Object... arguments) {

		Assert.notNull(method, "MethodInvocation must not be null!");

		final Map<String, ActionInputParameter> result = new HashMap<String, ActionInputParameter>();

		for (MethodParameter parameter : new MethodParameters(method).getParametersWith(DTOParam.class)) {

			int parameterIndex = parameter.getParameterIndex();
			Object argument = parameterIndex < arguments.length ? arguments[parameterIndex] : null;

			if (argument == null) {
				continue;
			}

			SpringActionDescriptor.recurseBeanCreationParams(argument.getClass(), null, argument, "", new HashSet<String>(),
					new ActionInputParameterVisitor() {

						@Override
						public void visit(ActionInputParameter inputParameter) {
							result.put(inputParameter.getParameterName(), inputParameter);
						}
					}, new ArrayList<ActionInputParameter>());

		}

		return result;
	}

	static Map<String, ActionInputParameter> getRequestParams(Method invokedMethod, Object[] arguments) {
		
		// the action descriptor needs to know the param type, value and name
		Map<String, ActionInputParameter> requestParamMap = getActionInputParameters(RequestParam.class, invokedMethod,
				arguments);
		requestParamMap.putAll(getDtoActionInputParameters(invokedMethod, arguments));

		return requestParamMap;
	}
}
