import fs from 'fs/promises';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const STORAGE_FILE = path.join(__dirname, '.spike_client_storage.json');

/**
 * Simulates IndexedDB with disk-backed JSON to test restart survival.
 */
export class ClientStore {
  constructor() {
    this.attendance = new Map();
    this.outbox = []; // list of mutation objects
  }

  async loadFromDisk() {
    try {
      const data = await fs.readFile(STORAGE_FILE, 'utf-8');
      const parsed = JSON.parse(data);
      this.attendance = new Map(Object.entries(parsed.attendance || {}));
      this.outbox = parsed.outbox || [];
    } catch {
      // File does not exist initially; start clean
      this.attendance = new Map();
      this.outbox = [];
    }
  }

  async persistToDisk() {
    const serialized = {
      attendance: Object.fromEntries(this.attendance),
      outbox: this.outbox
    };
    await fs.writeFile(STORAGE_FILE, JSON.stringify(serialized, null, 2), 'utf-8');
  }

  async cleanupDisk() {
    try {
      await fs.unlink(STORAGE_FILE);
    } catch {}
  }

  async markAttendance(record) {
    const entityId = record.id;
    const mutationId = crypto.randomUUID();

    // 1. Write to local store (IndexedDB equivalent)
    this.attendance.set(entityId, {
      ...record,
      syncStatus: 'pending',
      updatedAt: new Date().toISOString()
    });

    // 2. Append to persistent outbox
    const mutation = {
      mutationId,
      entity: 'attendance',
      entityId,
      op: 'upsert',
      payload: record,
      clientTime: new Date().toISOString(),
      status: 'pending'
    };
    this.outbox.push(mutation);

    await this.persistToDisk();
    return mutation;
  }

  getPendingOutbox() {
    return this.outbox.filter(m => m.status === 'pending');
  }

  async markSynced(mutationIds) {
    const idSet = new Set(mutationIds);
    this.outbox = this.outbox.filter(m => !idSet.has(m.mutationId));

    for (const [id, rec] of this.attendance.entries()) {
      rec.syncStatus = 'synced';
      this.attendance.set(id, rec);
    }

    await this.persistToDisk();
  }
}
