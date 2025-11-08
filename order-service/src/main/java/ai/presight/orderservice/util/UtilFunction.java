package ai.presight.orderservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class UtilFunction {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT); // pretty print

    public static String objectToJSON(Object obj) {
        if (obj == null) {
            return "{ \"message\": \"null\" }";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{ \"error\": \"Failed to convert object to JSON: " + e.getMessage() + "\" }";
        }
    }
}
 