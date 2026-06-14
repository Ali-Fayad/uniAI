package com.uniai.admin.application.service;

import org.springframework.stereotype.Service;

/**
 * Thin admin application service placeholder.
 * Keeps the admin module in place without introducing domain or persistence concerns yet.
 */
@Service
public class AdminApplicationService {

    public String getHealthMessage() {
        return "Admin access granted";
    }
}
