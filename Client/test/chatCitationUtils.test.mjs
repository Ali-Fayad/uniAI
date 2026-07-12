import test from "node:test";
import assert from "node:assert/strict";
import { buildCitationRows, dedupeCitations } from "../src/components/chat/citationUtils.js";

test("dedupeCitations removes duplicate labels and preserves order", () => {
  const citations = dedupeCitations([
    { label: "S1", title: "AUB CS", url: "https://aub.edu.lb/cs" },
    { label: "S1", title: "Duplicate", url: "https://example.com" },
    { label: "S2", title: "LAU MBA", url: "https://lau.edu.lb/mba" },
  ]);

  assert.equal(citations.length, 2);
  assert.deepEqual(citations.map((citation) => citation.label), ["S1", "S2"]);
});

test("buildCitationRows drops invalid or missing URLs", () => {
  const rows = buildCitationRows([
    { label: "S1", title: "AUB CS", url: "https://aub.edu.lb/cs" },
    { label: "S2", title: "Missing URL", url: "" },
    { label: "S3", title: "", url: "https://example.com" },
  ]);

  assert.equal(rows.length, 2);
  assert.deepEqual(rows[0], {
    label: "S1",
    title: "AUB CS",
    url: "https://aub.edu.lb/cs",
  });
  assert.deepEqual(rows[1], {
    label: "S3",
    title: "S3",
    url: "https://example.com",
  });
});
