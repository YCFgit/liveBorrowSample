package com.ycf.liveborrowsample.interfaces.http;

import java.nio.charset.StandardCharsets;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dingtalk")
public class DingTalkPageController {

    private static final String BORROW_ASSISTANT_PAGE = "static/dingtalk/borrow-assistant.html";
    private static final MediaType HTML_UTF8 = MediaType.parseMediaType("text/html;charset=UTF-8");

    @GetMapping(value = "/borrow-assistant.html", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> getBorrowAssistantPage() throws java.io.IOException {
        ClassPathResource resource = new ClassPathResource(BORROW_ASSISTANT_PAGE);
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(HTML_UTF8)
                .body("<html><body><h1>borrow assistant demo not found</h1></body></html>");
        }

        String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(HTML_UTF8);
        headers.setCacheControl(CacheControl.noStore().mustRevalidate().getHeaderValue());
        return new ResponseEntity<>(html, headers, HttpStatus.OK);
    }
}
