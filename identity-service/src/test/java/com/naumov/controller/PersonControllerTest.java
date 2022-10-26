package com.naumov.controller;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        String personCreateRequestBody = """
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

        MockHttpServletRequestBuilder requestBuilder = post("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content(personCreateRequestBody);

        mvc.perform(requestBuilder)
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

    // TODO add more test: negative scenarios
}