package com.naumov.controller;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.naumov.Application;
import com.naumov.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Application.class
)
@AutoConfigureMockMvc
// TODO explore separate configurations for tests segregation
//@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class PersonControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PersonService personService;

    @Test
    @Transactional
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
    @Transactional
    public void createPersonWithId() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();
        json = json.put("$", "id", 3);

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void createPersonWithContactId() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();
        json = json.put("$.contacts[*]", "id", 3);

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void createPersonWithAddressId() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();
        json = json.put("$.addresses[*]", "id", 3);

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void createPersonWithIdentityDocumentId() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();
        json = json.put("$.identity_documents[*]", "id", 3);

        mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void successfullyUpdatePerson() throws Exception {
        DocumentContext json = defaultPersonCreateUpdateRequestJson();

        String createResponse = mvc.perform(postPersonCreateUpdateRequest(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = JsonPath.parse(createResponse).read("$.id", Long.class);

        String getResponse = mvc.perform(get("/people/" + id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        DocumentContext createdPerson = JsonPath.parse(getResponse);
        createdPerson = createdPerson.set("$.name", "New name");

        mvc.perform(putPersonCreateUpdateRequest(createdPerson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", equalTo("New name")));
    }

    private MockHttpServletRequestBuilder postPersonCreateUpdateRequest(DocumentContext personCreateRequestJson) {
        return post("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content(personCreateRequestJson.jsonString());
    }

    private MockHttpServletRequestBuilder putPersonCreateUpdateRequest(DocumentContext personCreateRequestJson) {
        return put("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content(personCreateRequestJson.jsonString());
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

    // TODO add more test: negative scenarios
}