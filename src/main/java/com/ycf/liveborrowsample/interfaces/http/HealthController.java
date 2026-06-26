package com.ycf.liveborrowsample.interfaces.http;

import com.ycf.liveborrowsample.interfaces.http.response.ApiResponse;
import com.ycf.liveborrowsample.interfaces.http.response.HealthResponse;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<HealthResponse> health() {
        HealthResponse response = new HealthResponse("live-borrow-sample", "UP", LocalDateTime.now());
        return ApiResponse.success(response);
    }
}
