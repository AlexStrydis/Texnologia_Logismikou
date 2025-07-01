package gr.aegean.icsd.fms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FestivalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createFestival() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", "RockFest",
                "description", "Annual rock festival",
                "startDate", LocalDate.now().toString(),
                "endDate", LocalDate.now().plusDays(2).toString(),
                "location", "Athens"
        );

        mockMvc.perform(post("/api/festivals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("RockFest"));
    }

    @Test
    void updateAndDeleteFestival() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", "JazzFest",
                "description", "Annual jazz festival",
                "startDate", LocalDate.now().toString(),
                "endDate", LocalDate.now().plusDays(1).toString(),
                "location", "Athens"
        );

        String response = mockMvc.perform(post("/api/festivals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        Map<String, Object> updatePayload = Map.of(
                "description", "Updated"
        );

        mockMvc.perform(put("/api/festivals/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"));

        mockMvc.perform(delete("/api/festivals/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/festivals/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchFestivalsByName() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", "SearchFest",
                "description", "Test festival",
                "startDate", LocalDate.now().toString(),
                "endDate", LocalDate.now().plusDays(1).toString(),
                "location", "Patras"
        );

        mockMvc.perform(post("/api/festivals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/festivals")
                        .param("name", "SearchFest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("SearchFest"));
    }
}
