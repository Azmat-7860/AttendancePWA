import { EventEmitter } from 'events';

/**
 * Client sync engine managing the outbox, debounced sync, and online listener.
 */
export class SyncEngine extends EventEmitter {
  constructor(clientStore, server, options = {}) {
    super();
    this.store = clientStore;
    this.server = server;
    this.isOnline = options.isOnline ?? true;
    this.debounceMs = options.debounceMs ?? 2000;
    this.debounceTimer = null;
    this.isSyncing = false;
  }

  setOnlineStatus(online) {
    const wasOffline = !this.isOnline;
    this.isOnline = online;
    this.emit('network_change', online);

    if (wasOffline && online) {
      // Reconnected! Schedule automatic sync
      this.scheduleSync(100); // Trigger quick sync on reconnect
    }
  }

  scheduleSync(delayMs = this.debounceMs) {
    if (this.debounceTimer) {
      clearTimeout(this.debounceTimer);
    }

    this.debounceTimer = setTimeout(async () => {
      this.debounceTimer = null;
      await this.runSync();
    }, delayMs);
  }

  async runSync() {
    if (!this.isOnline || this.isSyncing) {
      return { skipped: true, reason: !this.isOnline ? 'offline' : 'in_progress' };
    }

    const pending = this.store.getPendingOutbox();
    if (pending.length === 0) {
      return { skipped: true, reason: 'empty_outbox' };
    }

    this.isSyncing = true;
    this.emit('sync_start', pending.length);

    try {
      // Send batch to server
      const response = this.server.handlePush(pending);

      if (response && response.success) {
        const appliedIds = response.results
          .filter(r => r.status === 'applied')
          .map(r => r.mutationId);

        await this.store.markSynced(appliedIds);
        this.emit('sync_success', appliedIds.length);
        return { success: true, count: appliedIds.length, results: response.results };
      }
    } catch (err) {
      this.emit('sync_error', err);
      return { success: false, error: err };
    } finally {
      this.isSyncing = false;
    }
  }

  stop() {
    if (this.debounceTimer) {
      clearTimeout(this.debounceTimer);
      this.debounceTimer = null;
    }
  }
}
