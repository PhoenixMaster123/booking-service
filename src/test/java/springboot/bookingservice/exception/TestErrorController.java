package springboot.bookingservice.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
class TestErrorController {

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

    static class TestDto {
        public int n;
    }

    static class BodyDto {
        @NotNull(message = "name is required")
        public String name;
    }
}
