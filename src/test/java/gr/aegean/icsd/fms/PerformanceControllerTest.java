package gr.aegean.icsd.fms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PerformanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndListPerformances() throws Exception {
        Map<String, Object> festivalPayload = Map.of(
                "name", "PerfFest",
                "description", "Festival",
                "location", "Athens"
        );

        String festivalResponse = mockMvc.perform(post("/api/festivals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(festivalPayload)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long festivalId = objectMapper.readTree(festivalResponse).get("id").asLong();

        Map<String, Object> perfPayload = Map.of(
                "name", "My Band",
                "description", "Rocking",
                "genre", "Rock",
                "duration", 60,
                "bandMembers", List.of("Alice", "Bob")
        );

        mockMvc.perform(post("/api/festivals/" + festivalId + "/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(perfPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        mockMvc.perform(get("/api/festivals/" + festivalId + "/performances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("My Band"));
    }
}
