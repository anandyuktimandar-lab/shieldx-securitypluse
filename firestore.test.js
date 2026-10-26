const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require("@firebase/rules-unit-testing");

const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "user_alice";
const BOB_UID = "user_bob";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

let testEnv;

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Watchlist: unauthenticated access is denied, owner CRUD succeeds, cross-user access is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();

  const now = new Date(Date.now() - 1000);
  const validCoin = {
    userId: ALICE_UID,
    coinId: "bitcoin",
    symbol: "BTC",
    name: "Bitcoin",
    createdAt: now,
    updatedAt: now,
  };

  // Unauthenticated create fails
  await assertFails(
    unauthDb.collection(`users/${ALICE_UID}/watchlist`).doc("bitcoin").set(validCoin)
  );

  // Owner create succeeds
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/watchlist`).doc("bitcoin").set(validCoin)
  );

  // Owner get succeeds
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/watchlist`).doc("bitcoin").get()
  );

  // Cross-user get fails
  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/watchlist`).doc("bitcoin").get()
  );

  // Owner update with valid mutable fields succeeds
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/watchlist`).doc("bitcoin").update({
      name: "Bitcoin Core",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Owner update attempting to mutate immutable userId fails
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/watchlist`).doc("bitcoin").update({
      userId: BOB_UID,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Owner delete succeeds
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/watchlist`).doc("bitcoin").delete()
  );
});

test("Portfolio: enforces positive amount, owner-only access, and immutable fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const now = new Date(Date.now() - 1000);
  const validHolding = {
    userId: ALICE_UID,
    coinId: "ethereum",
    symbol: "ETH",
    name: "Ethereum",
    amount: 2.5,
    buyPriceUsd: 3100.0,
    createdAt: now,
    updatedAt: now,
  };

  // Create with valid positive amount succeeds
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/portfolio`).doc("hold_eth_1").set(validHolding)
  );

  // Create with negative amount fails
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/portfolio`).doc("hold_eth_bad").set({
      ...validHolding,
      amount: -1.0,
    })
  );

  // Cross-user delete fails
  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/portfolio`).doc("hold_eth_1").delete()
  );

  // Owner update amount succeeds
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/portfolio`).doc("hold_eth_1").update({
      amount: 3.0,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Cloud Scan Reports: enforces valid riskLevel enum and owner-only access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  const now = new Date(Date.now() - 1000);
  const validScan = {
    userId: ALICE_UID,
    url: "https://example-login-verify.com",
    domain: "example-login-verify.com",
    riskLevel: "SUSPICIOUS",
    riskScore: 65,
    summary: "Suspicious credential harvesting keywords detected.",
    createdAt: now,
    updatedAt: now,
  };

  // Valid create succeeds
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/scans`).doc("scan_001").set(validScan)
  );

  // Invalid riskLevel enum fails
  await assertFails(
    aliceDb.collection(`users/${ALICE_UID}/scans`).doc("scan_002").set({
      ...validScan,
      riskLevel: "INVALID_ENUM",
    })
  );

  // Valid list query scoped to owner succeeds
  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/scans`).where("userId", "==", ALICE_UID).get()
  );
});
