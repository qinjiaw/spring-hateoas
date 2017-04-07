package org.springframework.hateoas.halforms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.EmbeddedWrapper;
import org.springframework.hateoas.core.EmbeddedWrappers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Dietrich Schulten
 */
@JsonPropertyOrder({ "embeddeds", "links", "templates" })
@JsonDeserialize(using = HalFormsDocumentDeserializer.class)
public class HalFormsDocument extends ResourceSupport implements TemplatesSupport {

	private final List<Template> templates = new ArrayList<Template>();

	private final EmbeddedWrappers wrappers = new EmbeddedWrappers(true);
	private final List<EmbeddedWrapper> embeddedWrappers;
	List<Object> embedded = new ArrayList<Object>();

	public HalFormsDocument() {
		this(Collections.<Link>emptyList(), Collections.<Template>emptyList(), Collections.<EmbeddedWrapper>emptyList());
	}

	public HalFormsDocument(Iterable<Link> links, List<Template> templates, List<EmbeddedWrapper> embeddeds) {

		super.add(links);

		for (Template template : templates) {
			add(template);
		}

		this.embeddedWrappers = embeddeds;
	}

	@Override
	@JsonProperty("_templates")
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = Jackson2HalFormsModule.HalTemplateListSerializer.class)
	public List<Template> getTemplates() {
		return templates;
	}

	@JsonProperty("_embedded")
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY,
			using = Jackson2HalFormsModule.HalEmbeddedResourcesSerializer.class)
	public Collection<EmbeddedWrapper> getEmbeddeds() {
		return embeddedWrappers;
	}

	@Override
	@JsonProperty("_links")
	@JsonSerialize(using = Jackson2HalFormsModule.HalFormsLinkLinkSerializer.class)
	public List<Link> getLinks() {
		return super.getLinks();
	}

	public void add(Template template) {
		templates.add(template);

		if (template.getProperties() == null) {
			return;
		}
	}

	@JsonIgnore
	public Template getTemplate() {
		return getTemplate(Template.DEFAULT_KEY);
	}

	@JsonIgnore
	public Template getTemplate(String key) {

		for (Template template : templates) {
			if (template.getKey().equals(key)) {
				return template;
			}
		}

		return null;

	}
}
