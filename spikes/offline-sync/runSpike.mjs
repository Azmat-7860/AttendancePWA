import { MockServer } from './mockServer.mjs';
import { ClientStore } from './clientStore.mjs';
import { SyncEngine } from './syncEngine.mjs';

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function run() {
  console.log('===============================================================');
  console.log('SPIKE T0-04: Offline-First Sync & Idempotency Test');
  console.log('===============================================================\n');

  const server = new MockServer();
  let clientStore = new ClientStore();
  await clientStore.cleanupDisk(); // Start fresh

  // --------------------------------------------------------------------------
  // STEP 1: Mark attendance while OFFLINE
  // --------------------------------------------------------------------------
  console.log('[Step 1] Initializing client in OFFLINE mode...');
  let syncEngine = new SyncEngine(clientStore, server, { isOnline: false, debounceMs: 1500 });

  const record = {
    id: 'att-worker-101-2026-11-19',
    workerId: 'worker-101',
    siteId: 'site-alpha',
    workDate: '2026-11-19',
    status: 'PRESENT',
    inTime: '08:00',
    outTime: '17:00',
    otMinutes: 60
  };

  console.log('-> Marking attendance for Worker 101...');
  const mutation = await clientStore.markAttendance(record);
  console.log(`   Created local mutation: ${mutation.mutationId}`);

  // Assert local state
  console.log(`   Local outbox pending count: ${clientStore.getPendingOutbox().length}`);
  console.log(`   Server database count: ${server.getRecordCount()} (expected 0, since offline)`);
  if (server.getRecordCount() !== 0 || clientStore.getPendingOutbox().length !== 1) {
    throw new Error('FAILED Step 1: Data leaked to server while offline or outbox missing.');
  }
  console.log('   ✓ PASS: Data saved locally to outbox, 0 records on server.\n');

  // --------------------------------------------------------------------------
  // STEP 2: Simulate App Close & Restart
  // --------------------------------------------------------------------------
  console.log('[Step 2] Simulating app close & reopen (reboot)...');
  syncEngine.stop();
  clientStore = null; // discard in-memory state

  // Re-instantiate from disk
  clientStore = new ClientStore();
  await clientStore.loadFromDisk();
  console.log(`   Loaded client from storage. Outbox count: ${clientStore.getPendingOutbox().length}`);
  if (clientStore.getPendingOutbox().length !== 1) {
    throw new Error('FAILED Step 2: Outbox did not survive app restart.');
  }
  if (clientStore.getPendingOutbox()[0].mutationId !== mutation.mutationId) {
    throw new Error('FAILED Step 2: MutationId changed after restart.');
  }
  console.log(`   ✓ PASS: Outbox survived restart with intact mutationId: ${mutation.mutationId}\n`);

  // --------------------------------------------------------------------------
  // STEP 3: Reconnect Network & Verify Automatic Upload (~2 seconds)
  // --------------------------------------------------------------------------
  console.log('[Step 3] Reconnecting network (going ONLINE)...');
  syncEngine = new SyncEngine(clientStore, server, { isOnline: false, debounceMs: 1500 });

  let syncTriggered = false;
  syncEngine.on('sync_success', (count) => {
    syncTriggered = true;
    console.log(`   -> Auto-sync event fired! Uploaded ${count} mutation(s).`);
  });

  const startTime = Date.now();
  syncEngine.setOnlineStatus(true);

  // Wait for auto-sync (target ~2 seconds)
  while (!syncTriggered && Date.now() - startTime < 4000) {
    await sleep(100);
  }

  const elapsedMs = Date.now() - startTime;
  console.log(`   Elapsed time until sync completed: ${elapsedMs} ms`);

  if (!syncTriggered) {
    throw new Error('FAILED Step 3: Auto-sync did not trigger within 4 seconds.');
  }
  if (server.getRecordCount() !== 1) {
    throw new Error(`FAILED Step 3: Server record count is ${server.getRecordCount()}, expected 1.`);
  }
  if (clientStore.getPendingOutbox().length !== 0) {
    throw new Error('FAILED Step 3: Outbox was not cleared after sync.');
  }
  console.log('   ✓ PASS: Data reached server automatically within ~2s. Outbox cleared.\n');

  // --------------------------------------------------------------------------
  // STEP 4: Idempotency Test (Send Duplicate Mutation)
  // --------------------------------------------------------------------------
  console.log('[Step 4] Testing duplicate mutation (idempotency)...');
  console.log('   Sending the exact same mutation batch to server a second time...');
  const duplicateResponse = server.handlePush([mutation]);

  console.log('   Server response to duplicate:', JSON.stringify(duplicateResponse.results[0]));
  console.log(`   Server database count: ${server.getRecordCount()} (expected STILL 1)`);

  if (server.getRecordCount() !== 1) {
    throw new Error('FAILED Step 4: Duplicate mutation created a duplicate record!');
  }
  if (!duplicateResponse.results[0].deduplicated) {
    throw new Error('FAILED Step 4: Server did not flag mutation as deduplicated.');
  }
  console.log('   ✓ PASS: Server safely deduplicated replay via processed_mutations. Exact 1 record kept.\n');

  // --------------------------------------------------------------------------
  // Clean up
  // --------------------------------------------------------------------------
  syncEngine.stop();
  await clientStore.cleanupDisk();

  console.log('===============================================================');
  console.log('SPIKE T0-04 COMPLETED SUCCESSFULLY: ALL 4 ACCEPTANCE CRITERIA MET');
  console.log('===============================================================');
}

run().catch((err) => {
  console.error('\n❌ SPIKE TEST FAILED:', err);
  process.exit(1);
});
