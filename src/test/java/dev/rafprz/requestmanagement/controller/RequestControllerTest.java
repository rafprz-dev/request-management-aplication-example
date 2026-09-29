package dev.rafprz.requestmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rafprz.requestmanagement.exception.InvalidStateTransitionException;
import dev.rafprz.requestmanagement.exception.RequestNotFoundException;
import dev.rafprz.requestmanagement.generated.model.CreateRequestDto;
import dev.rafprz.requestmanagement.generated.model.ReasonDto;
import dev.rafprz.requestmanagement.generated.model.RequestDto;
import dev.rafprz.requestmanagement.generated.model.RequestPageDto;
import dev.rafprz.requestmanagement.generated.model.RequestState;
import dev.rafprz.requestmanagement.generated.model.StateChangeDto;
import dev.rafprz.requestmanagement.generated.model.TransitionAction;
import dev.rafprz.requestmanagement.generated.model.UpdateContentDto;
import dev.rafprz.requestmanagement.service.RequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RequestService requestService;

    private RequestDto sampleRequestDto(UUID id, RequestState state) {
        OffsetDateTime now = OffsetDateTime.now();
        return new RequestDto(id, "Name", "Content", state, now, now);
    }

    // ---------- createRequest ----------

    @Test
    void createRequest_returnsCreatedWithLocationHeader() throws Exception {
        UUID id = UUID.randomUUID();
        RequestDto created = sampleRequestDto(id, RequestState.CREATED);
        when(requestService.create("Name", "Content")).thenReturn(created);

        mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateRequestDto("Name", "Content"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/requests/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.state").value("CREATED"));
    }

    @Test
    void createRequest_returnsBadRequestWhenNameBlank() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateRequestDto("", "Content"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void createRequest_returnsBadRequestWhenContentBlank() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateRequestDto("Name", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ---------- listRequests ----------

    @Test
    void listRequests_returnsPageOfRequests() throws Exception {
        RequestDto dto = sampleRequestDto(UUID.randomUUID(), RequestState.CREATED);
        RequestPageDto page = new RequestPageDto(List.of(dto), 0, 10, 1L, 1);
        when(requestService.list(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void listRequests_returnsBadRequestWhenSizeExceedsMax() throws Exception {
        mockMvc.perform(get("/requests").param("size", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listRequests_returnsBadRequestWhenPageNegative() throws Exception {
        mockMvc.perform(get("/requests").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ---------- getRequest ----------

    @Test
    void getRequest_returnsRequestWhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.getOrThrow(id)).thenReturn(sampleRequestDto(id, RequestState.CREATED));

        mockMvc.perform(get("/requests/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getRequest_returnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.getOrThrow(id)).thenThrow(new RequestNotFoundException(id));

        mockMvc.perform(get("/requests/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Request not found: " + id));
    }

    // ---------- updateRequestContent ----------

    @Test
    void updateRequestContent_returnsUpdatedRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.updateContent(id, "New content")).thenReturn(sampleRequestDto(id, RequestState.CREATED));

        mockMvc.perform(patch("/requests/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateContentDto("New content"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void updateRequestContent_returnsBadRequestWhenContentBlank() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/requests/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateContentDto(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRequestContent_returnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.updateContent(eq(id), anyString())).thenThrow(new RequestNotFoundException(id));

        mockMvc.perform(patch("/requests/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateContentDto("New content"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRequestContent_returnsConflictWhenInvalidTransition() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.updateContent(eq(id), anyString()))
                .thenThrow(new InvalidStateTransitionException("Request " + id + " is in state PUBLISHED"));

        mockMvc.perform(patch("/requests/{id}", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateContentDto("New content"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ---------- verifyRequest ----------

    @Test
    void verifyRequest_returnsUpdatedRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.verify(id)).thenReturn(sampleRequestDto(id, RequestState.VERIFIED));

        mockMvc.perform(post("/requests/{id}/verify", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("VERIFIED"));
    }

    @Test
    void verifyRequest_returnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.verify(id)).thenThrow(new RequestNotFoundException(id));

        mockMvc.perform(post("/requests/{id}/verify", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void verifyRequest_returnsConflictWhenInvalidTransition() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.verify(id)).thenThrow(new InvalidStateTransitionException("invalid transition"));

        mockMvc.perform(post("/requests/{id}/verify", id))
                .andExpect(status().isConflict());
    }

    // ---------- acceptRequest ----------

    @Test
    void acceptRequest_returnsUpdatedRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.accept(id)).thenReturn(sampleRequestDto(id, RequestState.ACCEPTED));

        mockMvc.perform(post("/requests/{id}/accept", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACCEPTED"));
    }

    @Test
    void acceptRequest_returnsConflictWhenInvalidTransition() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.accept(id)).thenThrow(new InvalidStateTransitionException("invalid transition"));

        mockMvc.perform(post("/requests/{id}/accept", id))
                .andExpect(status().isConflict());
    }

    // ---------- publishRequest ----------

    @Test
    void publishRequest_returnsUpdatedRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.publish(id)).thenReturn(sampleRequestDto(id, RequestState.PUBLISHED));

        mockMvc.perform(post("/requests/{id}/publish", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PUBLISHED"));
    }

    @Test
    void publishRequest_returnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.publish(id)).thenThrow(new RequestNotFoundException(id));

        mockMvc.perform(post("/requests/{id}/publish", id))
                .andExpect(status().isNotFound());
    }

    // ---------- rejectRequest ----------

    @Test
    void rejectRequest_returnsUpdatedRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.reject(id, "Not good")).thenReturn(sampleRequestDto(id, RequestState.REJECTED));

        mockMvc.perform(post("/requests/{id}/reject", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ReasonDto("Not good"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("REJECTED"));
    }

    @Test
    void rejectRequest_returnsBadRequestWhenReasonBlank() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/requests/{id}/reject", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ReasonDto(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectRequest_returnsConflictWhenInvalidTransition() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.reject(eq(id), anyString()))
                .thenThrow(new InvalidStateTransitionException("invalid transition"));

        mockMvc.perform(post("/requests/{id}/reject", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ReasonDto("Not good"))))
                .andExpect(status().isConflict());
    }

    // ---------- deleteRequest ----------

    @Test
    void deleteRequest_returnsUpdatedRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.delete(id, "No longer needed")).thenReturn(sampleRequestDto(id, RequestState.DELETED));

        mockMvc.perform(post("/requests/{id}/delete", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ReasonDto("No longer needed"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("DELETED"));
    }

    @Test
    void deleteRequest_returnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.delete(eq(id), anyString())).thenThrow(new RequestNotFoundException(id));

        mockMvc.perform(post("/requests/{id}/delete", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ReasonDto("No longer needed"))))
                .andExpect(status().isNotFound());
    }

    // ---------- getRequestHistory ----------

    @Test
    void getRequestHistory_returnsHistoryList() throws Exception {
        UUID id = UUID.randomUUID();
        StateChangeDto change = new StateChangeDto(
                UUID.randomUUID(), id, TransitionAction.CREATE, RequestState.CREATED, OffsetDateTime.now());
        when(requestService.history(id)).thenReturn(List.of(change));

        mockMvc.perform(get("/requests/{id}/history", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].requestId").value(id.toString()))
                .andExpect(jsonPath("$[0].action").value("CREATE"));
    }

    @Test
    void getRequestHistory_returnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.history(id)).thenThrow(new RequestNotFoundException(id));

        mockMvc.perform(get("/requests/{id}/history", id))
                .andExpect(status().isNotFound());
    }
}
