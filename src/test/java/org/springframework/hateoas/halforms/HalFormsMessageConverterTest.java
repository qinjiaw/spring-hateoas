package org.springframework.hateoas.halforms;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.springframework.hateoas.affordance.springmvc.AffordanceBuilder.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.io.IOException;
import java.util.List;

import org.springframework.hateoas.affordance.formaction.DTOParam;
import org.springframework.hateoas.affordance.Affordance;
import org.springframework.hateoas.affordance.springmvc.AffordanceBuilder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.affordance.Select;
import org.springframework.hateoas.affordance.Suggestions;
import org.springframework.hateoas.affordance.SuggestionsProvider;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.core.Relation;
import org.springframework.hateoas.affordance.formaction.Input;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.hateoas.halforms.Jackson2HalFormsModule.HalFormsHandlerInstantiator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class HalFormsMessageConverterTest {

	public static final Logger LOG = LoggerFactory.getLogger(HalFormsMessageConverterTest.class);

	ObjectMapper objectMapper = new ObjectMapper();
	RelProvider relProvider = new DefaultRelProvider();
	CurieProvider curieProvider = new DefaultCurieProvider("test",
			new UriTemplate("http://localhost:8080/profile/{rel}"));

	@Value
	@Relation("customer")
	static class Customer {

		String customerId = "pj123";
		String name = "Peter Joseph";
	}

	@RequestMapping("/customers")
	static class DummyCustomersController {

		@RequestMapping("/{customerId}")
		public ResponseEntity<Resource<Customer>> getCustomer(@PathVariable final String customerId) {
			return null;
		}
	}

	@Value
	static class Size {

		String value, text;

		@JsonCreator
		public Size(@JsonProperty("value") final String value, @JsonProperty("text") final String text) {

			this.value = value;
			this.text = text;
		}
	}

	static class SizeOptions implements SuggestionsProvider {

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.affordance.SuggestionsProvider#getSuggestions()
		 */
		@Override
		public Suggestions getSuggestions() {
			return Suggestions.values(new Size("small", "Small"), new Size("big", "Big")).withValueField("value")
					.withPromptField("text");
		}
	}

	static class RemoteOptions implements SuggestionsProvider {

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.affordance.SuggestionsProvider#getSuggestions()
		 */
		@Override
		public Suggestions getSuggestions() {
			return Suggestions.remote("http://localhost/orders/countries").withValueField("value").withPromptField("text");
		}
	}

	static class Country {

		int id;
		String name;
	}

	@Value
	static class OrderItem {

		int orderNumber;
		String productCode;
		Integer quantity;
		String size;
		Country country;

		@JsonCreator
		public OrderItem(@Input(required = true) @JsonProperty("orderNumber") final int orderNumber,
				@Input(required = true) @JsonProperty("productCode") final String productCode,
				@Input(editable = true, pattern = "%d") @JsonProperty("quantity") final Integer quantity,
				@Select(provider = SizeOptions.class /*, type = SuggestType.EXTERNAL*/) @JsonProperty("size") final String size,
				@Select(provider = RemoteOptions.class) @JsonProperty("country") final Country country) {
			this.orderNumber = orderNumber;
			this.productCode = productCode;
			this.quantity = quantity;
			this.size = size;
			this.country = country;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	static class Order extends ResourceSupport {

		int orderNumber = 42;
		int itemCount = 3;
		String status = "pending";
		Resource<Customer> customer = new Resource<Customer>(new Customer());

		public Order() {
			customer.add(linkTo(methodOn(DummyCustomersController.class).getCustomer("pj123")).withSelfRel());
		}
	}

	@Value
	public static class OrderFilter {

		String status;
		Integer count;

		@JsonCreator
		public OrderFilter(@Input @JsonProperty("count") Integer count, @Input @JsonProperty("status") String status) {

			this.status = status;
			this.count = count;
		}

		public OrderFilter() {
			this(null, null);
		}
	}

	@RequestMapping("/orders")
	static class DummyOrderController {

		@RequestMapping("/{orderNumber}")
		public ResponseEntity<Resource<Order>> getOrder(@PathVariable final int orderNumber) {
			return null;
		}

		@RequestMapping("/{orderNumber}/items")
		public ResponseEntity<Resource<OrderItem>> getOrderItems(@PathVariable final int orderNumber) {
			return null;
		}

		@RequestMapping(value = "/{orderNumber}/items", method = RequestMethod.GET, params = "rel",
				consumes = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<Void> addOrderItemsPrepareForm(@PathVariable final int orderNumber,
				@RequestParam final String rel) {
			return null;
		}

		@RequestMapping(value = "/{orderNumber}/items", method = RequestMethod.POST,
				consumes = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<Void> addOrderItems(@PathVariable final int orderNumber,
				@RequestBody final OrderItem orderItem) {
			return null;
		}

		@RequestMapping
		public ResponseEntity<Resources<Order>> getOrders(@RequestParam List<String> attr) {
			return null;
		}

		@RequestMapping("/filtered")
		public ResponseEntity<Resources<Order>> getOrdersFiltered(@DTOParam OrderFilter filter) {
			return null;
		}

		@RequestMapping("/filteredWithRP")
		public ResponseEntity<Resources<Order>> getOrdersFilteredWithRequestParam(@RequestParam OrderFilter filter) {
			return null;
		}

	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = HypermediaType.HAL_FORMS)
	static class WebConfig extends WebMvcConfigurerAdapter {

		@Bean
		public DummyOrderController orderController() {
			return new DummyOrderController();
		}

		@Bean
		public DummyCustomersController customersController() {
			return new DummyCustomersController();
		}
	}

	@Autowired private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		mockMvc = webAppContextSetup(wac).build();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		objectMapper.registerModule(new Jackson2HalModule());
		objectMapper.registerModule(new Jackson2HalFormsModule());
		objectMapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(relProvider, curieProvider, null, true));

	}

	@Test
	public void testTemplatesWithRequestBody() throws JsonProcessingException {

		AffordanceBuilder builder = linkTo(
				methodOn(DummyOrderController.class).addOrderItems(42, new OrderItem(42, null, null, null, null)));
		Link link = linkTo(methodOn(DummyOrderController.class).addOrderItemsPrepareForm(42, null)).and(builder)
				.withSelfRel();

		Order order = new Order();
		order.add(link);

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.valueToTree(entity).toString();

		assertThat(json, hasJsonPath("$._templates"));
		assertThat(json, hasJsonPath("$._templates.default"));
		assertThat(json, hasJsonPath("$._templates.default.method", equalTo("POST")));
		assertThat(json, hasJsonPath("$._templates.default.contentType", equalTo("application/json")));
		assertThat(json, hasJsonPath("$._templates.default.properties", hasSize(4)));
	}

	@Test
	public void testTemplatesFromRequestParamSimple() throws JsonProcessingException {

		Order order = new Order();
		order.add(linkTo(methodOn(DummyOrderController.class).getOrders(null)).withRel("orders"));

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.valueToTree(entity).toString();

		assertThat(json, hasJsonPath("$._links['test:orders'].href", equalTo("http://localhost/orders{?attr}")));

		assertThat(json, hasNoJsonPath("$._templates"));
	}

	@Test
	public void testTemplatesFromRequestParamComplexWithoutRequestParamAnnotation() throws JsonProcessingException {

		Order order = new Order();
		Affordance affordance = linkTo(methodOn(DummyOrderController.class).getOrdersFiltered(new OrderFilter()))
				.withRel("orders");
		Assert.assertArrayEquals(new String[] { "count", "status" },
				affordance.getActionDescriptors().get(0).getRequestParamNames().toArray(new String[0]));

		order.add(affordance);

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.valueToTree(entity).toString();

		// If there are no @RequestParam AffordanceBuilder doesn't declare a UriTemplate variable
		assertThat(json,
				hasJsonPath("$._links['test:orders'].href", equalTo("http://localhost/orders/filtered{?count,status}")));
	}

	@Test
	public void testTemplatesFromRequestParamComplexWithRequestParamAnnotation() throws JsonProcessingException {

		Order order = new Order();
		order.add(linkTo(methodOn(DummyOrderController.class).getOrdersFilteredWithRequestParam(null)).withRel("orders"));

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.valueToTree(entity).toString();

		assertThat(json,
				hasJsonPath("$._links['test:orders'].href", equalTo("http://localhost/orders/filteredWithRP{?filter}")));

	}

	@Test
	public void testRequestWithStatusRequestParamNotFound() throws Exception {

		try {

			mockMvc.perform(get("http://localhost/orders/filteredWithRP?status=accepted"))//
					.andExpect(status().is4xxClientError());

			// Spring waits a @RequestParam called "filter"
		} catch (MissingServletRequestParameterException e) {
			assertThat(e.getParameterName(), equalTo("filter"));
		}

	}

	@Test
	public void testRequestWithStatusFound() throws Exception {

		// If @RequestParam annotation is not present the request is correct
		mockMvc.perform(get("http://localhost/orders/filtered?status=accepted").accept(MediaType.APPLICATION_JSON))//
				.andExpect(status().isOk());

	}

	@Test
	public void testReadHalFormDocument() throws JsonParseException, JsonMappingException, IOException {

		AffordanceBuilder builder = linkTo(
				methodOn(DummyOrderController.class).addOrderItems(42, new OrderItem(42, null, null, null, null)));
		Link link = linkTo(methodOn(DummyOrderController.class).addOrderItemsPrepareForm(42, null)).and(builder)
				.withSelfRel();

		Order order = new Order();
		order.add(link);

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.valueToTree(entity).toString();

		HalFormsDocument doc = objectMapper.readValue(json, HalFormsDocument.class);

		assertThat(doc.getTemplates().size(), equalTo(1));
		assertThat(doc.getTemplate().getProperty("size").getSuggest(), notNullValue());
	}

	@Test
	public void testReadHalFormDocumentWithLinkArrays() throws JsonParseException, JsonMappingException, IOException {

		Link link = linkTo(methodOn(DummyOrderController.class).addOrderItemsPrepareForm(42, null)).withRel("orders");
		Link link2 = linkTo(methodOn(DummyOrderController.class).addOrderItemsPrepareForm(42, null)).withRel("orders");

		Order order = new Order();
		order.add(link, link2);

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.valueToTree(entity).toString();

		HalFormsDocument doc = objectMapper.readValue(json, HalFormsDocument.class);

		assertThat(doc.getLinks().size(), equalTo(3));
		assertThat(doc.getLinks().get(0).getRel(), equalTo(doc.getLinks().get(1).getRel()));
	}
}
