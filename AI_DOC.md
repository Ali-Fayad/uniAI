# uniAI Graduate Knowledge AI Refactoring Summary

## Purpose

This document summarizes the current Graduate Knowledge AI architecture, the improvements made so far, the remaining problems, and the desired future architecture before production.

---

# Current AI Flow

The Graduate Knowledge pipeline currently follows this flow:

```text
User
    │
    ▼
Conversation History
    │
    ▼
Graduate Query Interpretation
    │
    ├── Deterministic Interpreter (preferred when possible)
    │
    └── AI Interpreter (Gemini Prompt)
            │
            ▼
CanonicalGraduateQueryDraft
            │
            ▼
Validator
            │
            ▼
GraduateKnowledgeQuery
            │
            ▼
Intent Resolution
            │
            ▼
SQL Retrieval
            │
            ▼
Retrieved Context
            │
            ▼
Final AI (Gemini)
            │
            ▼
Final Answer
```

The architecture intentionally separates:

- query understanding
- validation
- retrieval
- answer generation

This separation should be preserved.

---

# Current Components

## 1. Deterministic Interpreter

Responsible for simple predictable queries.

Examples:

- programs at AUB
- tuition at AUB
- admissions at LAU

Advantages

- very fast
- no AI cost
- deterministic

Problems

- duplicates AI logic
- increasingly difficult to maintain
- sometimes routes queries differently than the AI interpreter

Example

Engineering faculty tuition currently bypasses AI and is interpreted differently than intended.

---

## 2. AI Interpreter

Current prompt:

```
graduate-query-interpreter-prompt.txt
```

Produces:

```
CanonicalGraduateQueryDraft
```

Current improvements include:

- explicit JSON contract
- degree normalization
- faculty extraction
- department extraction
- program extraction
- tuition routing
- city extraction
- tuition scope protection
- regression examples

The prompt is in good condition for testing.

---

## 3. Validator

Responsible for:

- enum validation
- required fields
- unsupported combinations
- canonical query conversion

This remains the source of truth.

---

## 4. Query Factory

Produces:

```
GraduateKnowledgeQuery
```

Used by retrieval.

---

## 5. SQL Retrieval

Uses the interpreted query.

Different retrieval strategies exist for:

- tuition
- admissions
- program listing
- academic structure
- overview

---

## 6. Final AI

Receives:

- user question
- conversation
- retrieved context

Produces the final answer.

---

# Improvements Completed

## Tuition Scope

Originally

```
Computer Science tuition
```

became

```
scopeLevel = PROGRAM
```

which incorrectly filtered SQL.

Now

```
scopeLevel = null
```

unless explicitly requested.

Example

Correct:

```
Program-level tuition
```

Incorrect:

```
Computer Science tuition
```

---

## Degree Extraction

Expanded from:

- MASTER
- PHD

to

- CERTIFICATE
- DIPLOMA
- MASTER
- PHD

Also supports

- MBA
- MSc
- MA
- MS
- Doctorate
- etc.

---

## Faculty Extraction

Supports verified mappings such as

AUB

- Engineering
→ Maroun Semaan Faculty of Engineering and Architecture

- Business
→ Suliman S. Olayan School of Business

LAU

- Engineering
→ School of Engineering

- Business
→ Adnan Kassar School of Business

---

## Department Extraction

Now distinguishes

Program

```
Computer Science
```

from

Department

```
Computer Science Department
```

without inferring a faculty.

---

## City Extraction

Correctly avoids interpreting

```
Master's in Computer Science
```

as

```
city = Computer Science
```

---

# Current Problems

## 1. Prompt is too large

Current prompt contains:

- instructions
- JSON schema
- enums
- routing rules
- examples

Everything is mixed into one file.

This makes maintenance difficult.

---

## 2. Prompt duplicates Java

The prompt manually documents:

- enums
- operations
- resources
- comparisons
- aggregation values

These already exist inside Java.

Risk:

Prompt and code may eventually diverge.

---

## 3. No explicit schema file

Current JSON contract exists only inside the prompt.

It should become an independent artifact.

---

## 4. No operation specification

Current operation rules are written in English.

Example

```
PROGRAM supports
LIST
DETAILS
AGGREGATE
COMPARE
```

This should become machine-readable.

---

## 5. Retrieval inconsistency

Observed example

Question

```
Computer Science tuition at AUB
```

works correctly.

Question

```
Programs at AUB
```

does not return all expected programs.

Then

```
Is there Computer Science at AUB?
```

incorrectly reports no program.

This strongly suggests an inconsistency between

- tuition retrieval
- program listing retrieval

This is likely NOT an interpreter problem.

Needs investigation.

---

## 6. Deterministic vs AI Interpreter

Both currently contain overlapping logic.

Eventually they should share one canonical contract.

---

# Desired Future Architecture

Instead of one huge prompt:

```
graduate-query-interpreter-prompt.txt
```

split responsibilities.

Example

```
prompts/
    graduate-query-interpreter.txt

schemas/
    canonical-graduate-query.schema.json

specifications/
    graduate-operations.json

examples/
    graduate-query-examples.json
```

---

## 1. Interpreter Prompt

Contains only reasoning.

Examples

- extraction rules
- normalization
- aliases
- tuition rules
- semantic behavior

No enums.

No schema.

---

## 2. JSON Schema

Contains

- object structure
- required fields
- nullable fields
- enum values
- array types

Acts as the official contract.

---

## 3. Operations Specification

Example

```json
{
  "PROGRAM": {
    "allowedOperations": [
      "LIST",
      "DETAILS",
      "AGGREGATE",
      "COMPARE"
    ]
  }
}
```

Eventually expanded into

- required fields
- forbidden fields
- optional fields
- validation hints

---

## 4. Examples

Maintain regression examples separately.

Example

Input

```
Computer Science tuition at AUB
```

Expected JSON

```
...
```

Input

```
MBA tuition
```

Expected JSON

```
...
```

These become reusable for

- regression testing
- prompt tuning
- model comparison

---

# Long-Term Goal

Ideally the prompt should not manually define enums.

Instead

Java

```
GraduateKnowledgeOperation
GraduateKnowledgeResource
GraduateKnowledgeComparisonDimension
...
```

should generate

- schema
- enums
- operation specs

automatically.

This keeps AI contracts synchronized with code.

---

# Immediate Next Investigation

Investigate the inconsistency between

```
Program Listing
```

and

```
Tuition Retrieval
```

Questions

Why does

```
Computer Science tuition at AUB
```

retrieve successfully,

while

```
Programs at AUB
```

fails to include Computer Science?

Compare

- interpreted query
- SQL
- parameters
- joins
- retrieval context
- final AI prompt

Determine where the information disappears.

Do NOT modify code until the root cause is identified.

---

# Refactoring Goals

1. Separate reasoning from schema.
2. Separate schema from examples.
3. Separate operation definitions from prompt text.
4. Remove duplicated enum definitions.
5. Keep Java as the single source of truth.
6. Make prompt artifacts easier to maintain.
7. Keep validator authoritative.
8. Preserve the current layered architecture.
9. Investigate retrieval inconsistencies before additional prompt work.
10. Reduce prompt size while increasing maintainability.

---

# Current Status

| Area | Status |
|-------|--------|
| AI Interpreter | Stable enough for testing |
| Validator | Good |
| Query Factory | Good |
| Tuition Retrieval | Good |
| Program Listing | Needs investigation |
| Prompt Maintainability | Needs refactoring |
| Prompt Architecture | Needs modularization |
| Java ↔ Prompt Synchronization | Future improvement |
| Regression Coverage | Good foundation, should expand |