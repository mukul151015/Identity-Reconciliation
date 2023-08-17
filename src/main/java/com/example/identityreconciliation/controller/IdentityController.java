package com.example.identityreconciliation.controller;

import com.example.identityreconciliation.dto.ConsolidatedContactResponse;
import com.example.identityreconciliation.dto.IdentifyRequest;
import com.example.identityreconciliation.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class IdentityController {

    @Autowired
    private ContactService contactService;

    @PostMapping("/identify")
    public ResponseEntity<ConsolidatedContactResponse> identifyContact(@RequestBody IdentifyRequest request) {
        ConsolidatedContactResponse response = contactService.identifyContact(request);
        return ResponseEntity.ok(response);
    }
}
