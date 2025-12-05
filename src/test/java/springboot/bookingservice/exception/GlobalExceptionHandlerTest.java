package springboot.bookingservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestErrorController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @RestController
    @Validated
    public static class TestController {
        @GetMapping("/test/illegal-argument")
        public void illegalArgument() {
            throw new IllegalArgumentException("Bad argument provided");
        }

        @GetMapping("/test/runtime")
        public void runtime() {
            throw new RuntimeException("Unexpected failure");
        }

        @GetMapping("/test/missing-param")
        public String missingParam(@RequestParam("q") String q) {
            return q;
        }

        @GetMapping("/test/constraint")
        public int constraint(@RequestParam("x") @Min(value = 5, message = "must be >= 5") int x) {
            return x;
        }

        @PostMapping("/test/badjson")
        public void badJson(@RequestBody TestDto dto) {
        }

        @PostMapping("/test/validate-body")
        public void validateBody(@Valid @RequestBody BodyDto dto) {
        }
    }

    static class TestDto {
        public int n;
    }

    static class BodyDto {
        @NotNull(message = "name is required")
        public String name;
    }

    @Test
    @DisplayName("IllegalArgumentException -> 400 with structured ErrorResponse")
    void illegalArgument_returnsStructuredError() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Bad argument provided")))
                .andExpect(jsonPath("$.path", is("/test/illegal-argument")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("RuntimeException -> 500 with structured ErrorResponse")
    void runtime_returnsStructuredError() throws Exception {
        mockMvc.perform(get("/test/runtime"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("Unexpected failure")))
                .andExpect(jsonPath("$.path", is("/test/runtime")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("MissingServletRequestParameter -> 400 with structured ErrorResponse")
    void missingParam_returnsStructuredError() throws Exception {
        mockMvc.perform(get("/test/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Missing required parameter 'q'")))
                .andExpect(jsonPath("$.path", is("/test/missing-param")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("ConstraintViolationException -> 400 with structured ErrorResponse")
    void constraintViolation_returnsStructuredError() throws Exception {
        mockMvc.perform(get("/test/constraint").param("x", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("x")))
                .andExpect(jsonPath("$.message", containsString(">= 5")))
                .andExpect(jsonPath("$.path", is("/test/constraint")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("HttpMessageNotReadableException -> 400 with structured ErrorResponse")
    void badJson_returnsStructuredError() throws Exception {
        mockMvc.perform(post("/test/badjson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not-a-valid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Malformed JSON request")))
                .andExpect(jsonPath("$.path", is("/test/badjson")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException -> 400 with aggregated field errors")
    void bodyValidation_returnsAggregatedErrors() throws Exception {
        // missing required field 'name'
        mockMvc.perform(post("/test/validate-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", anyOf(containsString("name: name is required"), containsString("name: must not be null"))))
                .andExpect(jsonPath("$.path", is("/test/validate-body")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}
