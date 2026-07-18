# Graduate Retrieval

Graduate retrieval uses `GraduateKnowledgeQuery`: typed intent, resource, operation, filters, aggregation, sort, limit, and follow-up context. AI interpretation is validated; deterministic interpretation is a fallback. `GraduateFollowUpResolver` handles explicit names, safe pronouns, ordered comparison references, and clarification instead of unsafe broadening.

Location retrieval reads the normalized `campus` table. University-by-city results use an existence predicate over campuses; campus-by-city, campus counts, and institution-campus existence queries use the same canonical relation.

Resources include university, campus, programme, faculty, department, and graduate overview. Filters compose with strict AND semantics; language and admission predicates are program-owned. Program and tuition SQL are bounded. Known review notes: academic detail paths need an explicit database-side bound, and unsupported comparison/resource combinations should be rejected before generic fallback output.
