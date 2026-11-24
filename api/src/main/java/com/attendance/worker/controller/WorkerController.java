package com.attendance.worker.controller;

import com.attendance.worker.dto.*;
import com.attendance.worker.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping
    public ResponseEntity<WorkerResponse> createWorker(@Valid @RequestBody CreateWorkerRequest request) {
        WorkerResponse response = workerService.createWorker(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkerResponse>> listWorkers(
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<WorkerResponse> workers = workerService.listWorkers(activeOnly);
        return ResponseEntity.ok(workers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkerResponse> getWorker(@PathVariable UUID id) {
        WorkerResponse response = workerService.getWorker(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkerResponse> updateWorker(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkerRequest request
    ) {
        WorkerResponse response = workerService.updateWorker(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WorkerResponse> deactivateWorker(@PathVariable UUID id) {
        WorkerResponse response = workerService.deactivateWorker(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/sites")
    public ResponseEntity<WorkerSiteResponse> assignWorkerToSite(
            @PathVariable UUID id,
            @Valid @RequestBody AssignSiteRequest request
    ) {
        WorkerSiteResponse response = workerService.assignWorkerToSite(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/sites")
    public ResponseEntity<List<WorkerSiteResponse>> listWorkerSiteAssignments(@PathVariable UUID id) {
        List<WorkerSiteResponse> assignments = workerService.listWorkerSiteAssignments(id);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/by-site/{siteId}")
    public ResponseEntity<List<WorkerResponse>> listWorkersForSite(
            @PathVariable UUID siteId,
            @RequestParam(name = "onDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onDate
    ) {
        List<WorkerResponse> workers = workerService.listWorkersForSite(siteId, onDate);
        return ResponseEntity.ok(workers);
    }
}
