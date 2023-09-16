package de.dreierschach.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dreierschach.app.model.Board;
import org.springframework.stereotype.Component;

@Component
public class BoardExporter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(Board board) {
        try {
            return objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Fehler beim Erzeugen des JSON-Strings", e);
        }
    }

    public Board toBoard(String json) {
        try {
            return objectMapper.readValue(json, Board.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Fehler beim Parsen des JSON-Strings", e);
        }
    }
}
