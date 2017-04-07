package org.springframework.hateoas.halforms;

import java.util.List;

/**
 * Interface to mark classes that contains a list of {@link Template}
 *
 * @author Dietrich Schulten
 */
interface TemplatesSupport {

	List<Template> getTemplates();
}
