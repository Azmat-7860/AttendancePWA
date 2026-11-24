package com.attendance.worker.service;

import com.attendance.worker.dto.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WorkerService {

    WorkerResponse createWorker(CreateWorkerRequest request);

    WorkerResponse updateWorker(UUID id, UpdateWorkerRequest request);

    WorkerResponse deactivateWorker(UUID id);

    WorkerResponse getWorker(UUID id);

    List<WorkerResponse> listWorkers(boolean activeOnly);

    WorkerSiteResponse assignWorkerToSite(UUID workerId, AssignSiteRequest request);

    List<WorkerSiteResponse> listWorkerSiteAssignments(UUID workerId);

    List<WorkerResponse> listWorkersForSite(UUID siteId, LocalDate onDate);
}
