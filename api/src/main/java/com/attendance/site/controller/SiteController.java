package com.attendance.site.controller;

import com.attendance.site.dto.CreateSiteRequest;
import com.attendance.site.dto.SiteResponse;
import com.attendance.site.dto.UpdateSiteRequest;
import com.attendance.site.service.SiteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @PostMapping
    public ResponseEntity<SiteResponse> createSite(@Valid @RequestBody CreateSiteRequest request) {
        SiteResponse response = siteService.createSite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SiteResponse>> listSites(
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<SiteResponse> sites = siteService.listSites(activeOnly);
        return ResponseEntity.ok(sites);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteResponse> getSite(@PathVariable UUID id) {
        SiteResponse response = siteService.getSite(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteResponse> updateSite(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSiteRequest request
    ) {
        SiteResponse response = siteService.updateSite(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SiteResponse> deactivateSite(@PathVariable UUID id) {
        SiteResponse response = siteService.deactivateSite(id);
        return ResponseEntity.ok(response);
    }
}
