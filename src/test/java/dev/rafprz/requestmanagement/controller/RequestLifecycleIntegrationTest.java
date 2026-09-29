package dev.rafprz.requestmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rafprz.requestmanagement.generated.model.CreateRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test exercising the full request lifecycle through the real REST endpoints,
 * backed by the actual service/repository layers and an H2 database (no mocks).
 */
@SpringBootTest
@AutoConfigureMockMvc
class RequestLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void requestGoesFromCreationToPublication() throws Exception {
        final var createRequestDto = new CreateRequestDto("Lifecycle request", "Initial content");

        final var createResponse = mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.state").value("CREATED"))
                .andExpect(jsonPath("$.name").value("Lifecycle request"))
                .andExpect(jsonPath("$.content").value("Initial content"))
                .andReturn();

        final var id = objectMapper.readTree(createResponse.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/requests/{id}/verify", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.state").value("VERIFIED"));

        mockMvc.perform(post("/requests/{id}/accept", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.state").value("ACCEPTED"));

        final var publishResponse = mockMvc.perform(post("/requests/{id}/publish", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.state").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedNumber").isNumber())
                .andReturn();

        final var publishedNumber = objectMapper.readTree(publishResponse.getResponse().getContentAsString())
                .get("publishedNumber").asLong();
        assertThat(publishedNumber).isGreaterThanOrEqualTo(1L);

        mockMvc.perform(get("/requests/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PUBLISHED"));

        mockMvc.perform(get("/requests/{id}/history", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(4)))
                .andExpect(jsonPath("$[0].action").value("CREATE"))
                .andExpect(jsonPath("$[0].toState").value("CREATED"))
                .andExpect(jsonPath("$[1].action").value("VERIFY"))
                .andExpect(jsonPath("$[1].fromState").value("CREATED"))
                .andExpect(jsonPath("$[1].toState").value("VERIFIED"))
                .andExpect(jsonPath("$[2].action").value("ACCEPT"))
                .andExpect(jsonPath("$[2].fromState").value("VERIFIED"))
                .andExpect(jsonPath("$[2].toState").value("ACCEPTED"))
                .andExpect(jsonPath("$[3].action").value("PUBLISH"))
                .andExpect(jsonPath("$[3].fromState").value("ACCEPTED"))
                .andExpect(jsonPath("$[3].toState").value("PUBLISHED"));
    }
}
