package com.naumov.identityservice.controller;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.naumov.identityservice.IdentityServiceApplication;
import com.naumov.identityservice.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = IdentityServiceApplication.class
)
@AutoConfigureMockMvc
@Transactional
// TODO explore separate configurations for tests segregation
//@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class PersonControllerTest {
    private static final String peopleUrl = "/people";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private PersonService personService;

    @Test
    public void successfullyCreatePerson() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.is_hidden", is(false)))
                .andExpect(jsonPath("$.addresses", notNullValue()))
                .andExpect(jsonPath("$.addresses", hasSize(1)))
                .andExpect(jsonPath("$.addresses[0]", notNullValue()))
                .andExpect(jsonPath("$.addresses[0].id", notNullValue()))
                .andExpect(jsonPath("$.contacts", notNullValue()))
                .andExpect(jsonPath("$.contacts", hasSize(1)))
                .andExpect(jsonPath("$.contacts[0]", notNullValue()))
                .andExpect(jsonPath("$.contacts[0].id", notNullValue()))
                .andExpect(jsonPath("$.identity_documents", notNullValue()))
                .andExpect(jsonPath("$.identity_documents", hasSize(1)))
                .andExpect(jsonPath("$.identity_documents[0]", notNullValue()))
                .andExpect(jsonPath("$.identity_documents[0].id", notNullValue()));
    }

    @Test
    public void createPersonWithoutName() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();
        json.delete("$.name");

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createPersonWithWrongDateOfBirthFormat() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();
        json.set("$.date_of_birth", "2000.01.12");

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createPersonWithId() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();
        json.put("$", "id", 3);

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createPersonWithContactId() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();
        json.put("$.contacts[*]", "id", 3);

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createPersonWithAddressId() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();
        json.put("$.addresses[*]", "id", 3);

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createPersonWithIdentityDocumentId() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();
        json.put("$.identity_documents[*]", "id", 3);

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getPerson() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();

        String createResponse = mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long personId = JsonPath.parse(createResponse).read("$.id", Long.class);

        mvc.perform(get(peopleUrl + "/" + personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Person name")))
                .andExpect(jsonPath("$.date_of_birth", is("12-12-1996")))
                .andExpect(jsonPath("$.is_hidden", is(false)))
                .andExpect(jsonPath("$.addresses[0].id", notNullValue()))
                .andExpect(jsonPath("$.addresses[0].region", is("Иркутская область")))
                .andExpect(jsonPath("$.addresses[0].address", is("Address line")))
                .andExpect(jsonPath("$.addresses[0].registration_address", is(true)))
                .andExpect(jsonPath("$.contacts[0].id", notNullValue()))
                .andExpect(jsonPath("$.contacts[0].phone_number", is("+71234567890")))
                .andExpect(jsonPath("$.identity_documents[0].id", notNullValue()))
                .andExpect(jsonPath("$.identity_documents[0].type", is("INNER_PASSPORT")))
                .andExpect(jsonPath("$.identity_documents[0].full_number", is("123456789")))
                .andExpect(jsonPath("$.identity_documents[0].issue_date", is("12-12-2007")))
                .andExpect(jsonPath("$.identity_documents[0].is_primary", is(true)));
    }

    @Test
    public void getNonExistingPerson() throws Exception {
        mvc.perform(get(peopleUrl + "/" + 12))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updatePersonAddAddress() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();

        String createResponse = mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long personId = JsonPath.parse(createResponse).read("$.id", Long.class);

        String getResponse = mvc.perform(get(peopleUrl + "/" + personId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        DocumentContext createdPerson = JsonPath.parse(getResponse);

        Map<String, Object> newAddress = new HashMap<>();
        newAddress.put("region", "Москва");
        newAddress.put("address", "New address line");
        newAddress.put("registration_address", false);

        createdPerson.add("$.addresses", newAddress);

        mvc.perform(putPersonCreateUpdateRequest(createdPerson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.addresses", hasSize(2)));
    }

    @Test
    public void updatePersonWithoutId() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();

        mvc.perform(putPersonCreateUpdateRequest(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updatePersonRemoveAddresses() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();

        String createResponse = mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long personId = JsonPath.parse(createResponse).read("$.id", Long.class);

        String getResponse = mvc.perform(get(peopleUrl + "/" + personId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        DocumentContext createdPerson = JsonPath.parse(getResponse);
        createdPerson.delete("$.addresses");

        mvc.perform(putPersonCreateUpdateRequest(createdPerson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.addresses", hasSize(0)));
    }

    @Test
    public void getPeople() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isCreated());

        MockHttpServletRequestBuilder requestBuilder = get(peopleUrl)
                .param("page_number", "0")
                .param("page_size", "2");

        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));

        requestBuilder = get(peopleUrl)
                .param("page_number", "0")
                .param("page_size", "2")
                .param("region", "Иркутская область");

        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));

        requestBuilder = get(peopleUrl)
                .param("page_number", "0")
                .param("page_size", "2")
                .param("region", "Something");

        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        requestBuilder = get(peopleUrl)
                .param("page_number", "0");

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());

        requestBuilder = get(peopleUrl)
                .param("page_number", "0")
                .param("page_size", "-2")
                .param("region", "Иркутская область");

        mvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void verifyPerson() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isCreated());

        mvc.perform(get(peopleUrl + "/verify")
                        .param("name", "Person name")
                        .param("passport", "123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", equalTo(true)));

        mvc.perform(get(peopleUrl + "/verify")
                        .param("name", "name")
                        .param("passport", "passport"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", equalTo(false)));
    }

    private DocumentContext defaultPersonCreateUpdateRequestJson() {
        String jsonString = """
                {
                  "name": "Person name",
                  "date_of_birth": "12-12-1996",
                  "is_hidden": "false",
                  "addresses": [
                    {
                      "region": "Иркутская область",
                      "address": "Address line",
                      "registration_address": true
                    }
                  ],
                  "contacts": [
                    {
                      "phone_number": "+71234567890"
                    }
                  ],
                  "identity_documents": [
                    {
                      "type": "INNER_PASSPORT",
                      "full_number": "123456789",
                      "issue_date": "12-12-2007",
                      "is_primary": true
                    }
                  ]
                }
                """;

        return JsonPath.parse(jsonString);
    }

    private MockHttpServletRequestBuilder postPersonCreateUpdateRequest(DocumentContext personCreateRequestJson) {
        return post(peopleUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(personCreateRequestJson.jsonString());
    }

    private MockHttpServletRequestBuilder putPersonCreateUpdateRequest(DocumentContext personCreateRequestJson) {
        return put(peopleUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(personCreateRequestJson.jsonString());
    }
}