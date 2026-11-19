/**
 * Mock Server simulating the Spring Boot /sync/push endpoint
 * Implements processed_mutations table for idempotent deduplication.
 */
export class MockServer {
  constructor() {
    this.attendanceDb = new Map(); // id -> record
    this.processedMutations = new Map(); // mutationId -> { status, clientTime, processedAt }
    this.pushCallCount = 0;
  }

  handlePush(batch) {
    this.pushCallCount++;
    const results = [];

    for (const mutation of batch) {
      const { mutationId, entity, entityId, op, payload } = mutation;

      // Idempotency check: Have we already processed this mutationId?
      if (this.processedMutations.has(mutationId)) {
        const existing = this.processedMutations.get(mutationId);
        results.push({
          mutationId,
          status: existing.status,
          deduplicated: true,
          message: 'Mutation already processed. Safe replay ignored.'
        });
        continue;
      }

      // Process new mutation
      if (entity === 'attendance') {
        if (op === 'upsert') {
          this.attendanceDb.set(entityId, {
            ...payload,
            id: entityId,
            serverVersion: this.attendanceDb.size + 1,
            updatedAt: new Date().toISOString()
          });
        }
      }

      const result = {
        mutationId,
        status: 'applied',
        deduplicated: false
      };

      this.processedMutations.set(mutationId, {
        status: 'applied',
        processedAt: new Date().toISOString()
      });

      results.push(result);
    }

    return {
      success: true,
      results
    };
  }

  getRecordCount() {
    return this.attendanceDb.size;
  }

  getRecord(id) {
    return this.attendanceDb.get(id);
  }
}
