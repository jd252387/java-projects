# AGENTS.md

## Scope
These instructions apply to the entire repository unless a deeper `AGENTS.md` overrides them.

## Project Defaults
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

## Coding Expectations
- Write production code that is concise and maintainable.
- Avoid unnecessary abstraction, premature generalization, and speculative extension points.
- Prefer immutable data and straightforward control flow.
- Keep public APIs small and intentional.
- Add comments only when the intent is not obvious from the code itself.

## Dependencies
- Introduce libraries freely when they remove boilerplate, replace generic homegrown code, or provide standard behavior more reliably.
- Prefer mature, well-maintained libraries over custom utility code.
- When adding a dependency, keep the choice pragmatic and justified by simpler code or clearer behavior.

## Build And Testing
- Before submitting any change, verify it locally.
- Always run the relevant Gradle checks and make sure the project compiles.
- Always run the application or requested workflow and confirm the requested behavior works end to end.
- A task is not complete until the code has been compiled and exercised, not just statically edited.
- Use available MCP tools when helpful for validation, inspection, or running the system.

## Default Verification Flow
- Run formatting or linting if the project uses it.
- Run tests with Gradle.
- Build the project with Gradle.
- Run the application path affected by the change and verify the requested functionality.

## Output Standard
- Submit code that is simple, short, tested, and ready to run.
- If a tradeoff forces additional complexity, document the reason briefly in the final handoff.
