# AGENTS.md

## Scope
These instructions apply to the entire repository unless a deeper `AGENTS.md` overrides them.

## Project Defaults
- This repository is for writing plugins for Apache Solr.
- Use Java 21.
- Use Gradle with Kotlin DSL only: `build.gradle.kts`, `settings.gradle.kts`.
- Prefer a simple project structure and keep configuration minimal.
- Use Lombok where it removes boilerplate cleanly.

## Design Principles
- Embrace simplicity first. Use design patterns only when they clearly reduce complexity, improve extensibility, or produce a cleaner design.
- Keep code short, readable, and direct. Do not create extra classes, wrappers, or helper methods without a clear payoff.
- Prefer JDK features that reduce verbosity, especially `record` classes, sealed types when appropriate, and modern collection/utility APIs.
- Do not reinvent common infrastructure or generic utilities. Prefer established libraries and framework features over custom implementations.
- Favor functional style where it improves clarity. Use Streams and `Optional` when they simplify the code, but avoid forcing them into code paths that become harder to read or debug.
- Prefer established Solr extension points and existing Solr plugin patterns over custom frameworks or wrappers.

## Coding Expectations
- Treat Apache Solr documentation and Solr source code as primary references when designing or implementing plugin behavior.
- Read the relevant Solr reference documentation before writing code in an area that touches Solr APIs, plugin lifecycles, configuration, request handling, indexing, or search behavior.
- Inspect the corresponding Solr source code and tests to confirm how extension points are intended to be used and to align with existing conventions.
- Write production code that is concise and maintainable.
- Avoid unnecessary abstraction, premature generalization, and speculative extension points.
- Prefer immutable data and straightforward control flow.
- Keep public APIs small and intentional.
- Add comments only when the intent is not obvious from the code itself.

## Dependencies
- Introduce libraries freely when they remove boilerplate, replace generic homegrown code, or provide standard behavior more reliably.
- Prefer mature, well-maintained libraries over custom utility code.
- When adding a dependency, keep the choice pragmatic and justified by simpler code or clearer behavior.

## Documentation
- Use the Context7 MCP server when you need library or framework documentation, API details, or usage examples.
- Resolve the library first with `mcp__context7__resolve-library-id`, then read the docs with `mcp__context7__query-docs`.
- Prefer Context7 over generic web search for implementation details, version-specific behavior, and official API guidance.
- Keep documentation queries specific to the library, version, and feature you are changing so the retrieved guidance is precise.
- When documentation materially informs a code change, mention the source briefly in the final handoff.

## Build And Testing
- Before submitting any change, verify it locally.
- Always run the relevant Gradle checks and make sure the project compiles.
- Always run the application or requested workflow and confirm the requested behavior works end to end.
- A task is not complete until the code has been compiled and exercised, not just statically edited.
- Use available MCP tools when helpful for validation, inspection, or running the system.
- Use the docker-compose.yml when you need to initialize a Solr instance to test your work. 

## Default Verification Flow
- Run formatting or linting if the project uses it.
- Run tests with Gradle.
- Build the project with Gradle.
- Run the application path affected by the change and verify the requested functionality.

## Output Standard
- Submit code that is simple, short, tested, and ready to run.
- If a tradeoff forces additional complexity, document the reason briefly in the final handoff.
