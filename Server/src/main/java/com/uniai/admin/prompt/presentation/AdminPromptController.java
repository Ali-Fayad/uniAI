package com.uniai.admin.prompt.presentation;

import com.uniai.admin.prompt.dto.AdminPromptResponse;
import com.uniai.admin.prompt.service.AdminPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/prompts")
@RequiredArgsConstructor
public class AdminPromptController {

    private final AdminPromptService service;

    @GetMapping
    public ResponseEntity<List<AdminPromptResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{key}")
    public ResponseEntity<AdminPromptResponse> get(@PathVariable String key) {
        return ResponseEntity.ok(service.get(key));
    }
}
