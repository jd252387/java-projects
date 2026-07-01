# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Custom Apache Solr **query parser plugins** (`solr-core` 9.8.1), built as a jar and loaded into a SolrCloud cluster. The README describes a "greeting app" scaffold and Java 21 — both are stale. There is no `App` class, so `./gradlew run` (mainClass `com.example.projects.App`) will fail; ignore it.

## Commands

- `./gradlew build` — compile, test, and produce the plugin jar at `build/libs/projects.jar`
- `./gradlew test` — run the JUnit 5 suite
- Single test class: `./gradlew test --tests "com.example.projects.BasicExactQParserTest"`
- Single method: `./gradlew test --tests "com.example.projects.BasicExactQParserTest.parseRejectsUnknownFields"`
- `docker compose up` — SolrCloud (ZooKeeper 3.9 + two Solr 9.8.1 nodes). Build the jar first; `build/libs` is mounted read-only into each node at `/opt/solr/lib`. Nodes: `solr1` on 8983, `solr2` on 8984; JDWP debug on 5005/5006.

Toolchain is Java 25 (Gradle 9.4.0 via wrapper); Gradle downloads the JDK if needed.

## Architecture

Every parser is three pieces plus a registration, following one pattern:

1. **`BasicQParser`** (abstract base) — the shared entry point. `parse()` reads the required `field` local param, expands aliases, and delegates the actual query building to `parseImpl(field)`. When a field is an alias, it fans out into a `BooleanQuery` of `SHOULD` clauses, one `parseImpl` per aliased field.
2. **`Basic{Exact,Range,Text}QParser`** — concrete subclasses implementing only `parseImpl(field)`. They validate the field type against Solr's schema (`req.getSchema().getField`) and build the Lucene `Query`. Type rules differ per parser (exact = non-tokenized text / numeric / date; range = numeric or date; text = tokenized `TextField`).
3. **`Basic{...}QParserPlugin`** — thin `QParserPlugin` factory with a `public static final String NAME`. This `NAME` is the `{!name ...}` handle used in queries.

**Alias mechanism** (in the base class): request params of the form `f.<alias>.qf=fieldA fieldB` define aliases. Resolution is recursive (an alias may target other aliases), detects cycles, and is cached in the request context under key `aliases` so it's computed once per request. This is why aliases work uniformly across all three parsers without each reimplementing it.

**Registration:** each plugin must be declared in `config/configsets/sample-qparser-plugins/solrconfig.xml` as `<queryParser name="..." class="..."/>`. `SampleConfigsetTest` reflectively discovers every `*QParserPlugin.java` and **fails the build if any is not registered** in solrconfig.xml. So adding a parser means: base subclass + plugin + solrconfig entry, or the suite breaks.

## Adding a new parser

1. Extend `BasicQParser`, implement `parseImpl(field)` with the field-type validation for that parser.
2. Add a `Basic{X}QParserPlugin extends QParserPlugin` with a unique `NAME` constant.
3. Register it in `conf/solrconfig.xml`.
4. Add the matching schema field type(s) to `conf/schema.xml` if the parser needs one, then rely on the per-parser test pattern.

## Testing conventions

Unit tests do **not** boot Solr. They use in-test fakes: `TestIndexSchema` (a stub `IndexSchema` returning hand-built `SchemaField`s) and `RecordingFieldType` (a `PrimitiveFieldType` whose `getFieldQuery`/`getRangeQuery` return a marker `TermQuery` and record invocations). Assertions compare against those marker queries rather than executing a search. `SampleConfigsetTest` is the exception — it loads the real configset (`SolrConfig` + `IndexSchemaFactory`) to verify the sample schema and that each parser instantiates.
