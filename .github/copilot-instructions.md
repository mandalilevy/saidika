# Saidika — GitHub Copilot Instructions

## 1. Project Overview

Saidika is an individual entry for the micro1 Frontier Engineering Challenge 2026.

Saidika is a roadside assistance matching application designed to help stranded drivers quickly identify the assistance they need and connect with a suitable nearby service provider.

The core problem is:

> A stranded driver may waste significant time determining what assistance they need, finding a qualified nearby provider, and dealing with providers who are unavailable or unable to respond.

The project should demonstrate practical agentic AI engineering, reliability, measurable improvement, reproducibility, and thoughtful engineering decisions.

---

## 2. Current Scope

The evaluated MVP focuses exclusively on roadside vehicle assistance.

Supported service types:

* JUMP_START
* TYRE_ASSISTANCE
* MOBILE_MECHANIC
* TOWING
* FUEL_ASSISTANCE
* LOCKSMITH

Do not introduce unrelated features such as:

* Healthcare
* Medical emergency response
* Payments
* Authentication
* Social networking
* General marketplace functionality
* In-app messaging
* Reviews/ratings
* User accounts

unless explicitly requested.

Keep the project focused enough to be completed, tested, evaluated, documented, and demonstrated within the hackathon timeframe.

---

## 3. Technology Stack

The project uses:

* Java 21
* Spring Boot
* Maven
* Thymeleaf
* Spring Data JPA
* PostgreSQL
* RabbitMQ
* Docker / Docker Compose
* JUnit / Spring Boot Test
* Git / GitHub

Additional dependencies should only be introduced when there is a clear engineering reason.

Do not add libraries simply because they are convenient.

## Application Architecture

Saidika must be implemented as a **modular monolith**.

The entire application should run as a single Spring Boot application.

Do NOT introduce a microservices architecture, API gateway, service discovery, distributed configuration, or separate frontend/backend services.

The architecture should favor clear internal modularity while keeping deployment and execution simple.

The main application layers should follow clear separation of concerns, approximately:

* Web/MVC layer
* Application/service layer
* Domain/model layer
* Persistence/repository layer
* Infrastructure/integration layer
* Messaging layer
* AI/agent integration layer
* Evaluation layer

The exact package structure should be proposed based on the existing project before implementation.

---

## Existing Project

This project was freshly generated using **Spring Initializr**.

Treat the existing project as the starting point.

Before making changes:

1. Inspect the existing project structure.
2. Inspect `pom.xml`.
3. Inspect the main Spring Boot application class.
4. Inspect existing configuration and resources.
5. Determine which dependencies are already present.
6. Reuse the existing Spring Boot setup where appropriate.

Do NOT recreate, regenerate, replace, or reinitialize the Spring Boot project.

Do NOT create a second application.

Extend the existing Saidika codebase incrementally.

---

## Web Application

Saidika is a web-based application.

The user-facing interface should be implemented using:

* Spring MVC
* Thymeleaf
* HTML
* CSS
* Minimal JavaScript where necessary

Thymeleaf templates should be stored under:

`src/main/resources/templates`

Static assets should be stored under:

`src/main/resources/static`

The UI should remain intentionally simple and focused on the core roadside assistance workflow.

The initial user flow should eventually support:

1. Entering a natural-language roadside assistance request.
2. Providing or selecting a location.
3. Submitting the request.
4. Displaying the interpreted assistance requirement.
5. Displaying suitable nearby providers.
6. Showing the selected provider and relevant reasoning.
7. Demonstrating provider failure and fallback in the advanced workflow.

Do not introduce a separate frontend framework such as React, Angular, Vue, or Next.js.

---

## External Map/Location Integration

The conceptual product may use geographic location to identify nearby providers.

However, keep external mapping integrations isolated behind an abstraction.

Do not tightly couple core matching logic to Google Maps or another external mapping provider.

The application should be able to operate using coordinates and deterministic distance calculations for testing and evaluation.

External map APIs should only be introduced if they provide meaningful value and can be used reproducibly within the hackathon constraints.

Do not expose API keys or credentials in source code.

---

## Baseline and Advanced Architecture

The baseline and advanced implementations should share the same underlying provider data model and evaluation framework wherever practical.

The key difference should be the mechanism used to interpret and resolve the assistance request.

Baseline:

Natural language → deterministic classification → provider matching.

Advanced:

Natural language → AI-assisted interpretation → validation → provider matching.

This separation must make it possible to compare the two approaches fairly using the same evaluation scenarios.

---

## Implementation Discipline

Because the project is being developed during a three-day hackathon, prioritize a working vertical slice over architectural completeness.

Do not implement an entire layer simply because the architecture contains that layer.

Implement only what is required by the current milestone.

A feature should be developed end-to-end when practical:

Database/domain
→ service
→ controller
→ Thymeleaf view
→ test

Avoid speculative abstractions.
Avoid premature optimization.
Avoid unnecessary design patterns.
Avoid unnecessary interfaces unless they provide a real architectural benefit.

## 4. Architecture Philosophy

Favor simple, modular, maintainable architecture over unnecessary complexity.

Use clear separation between:

* Controllers
* Services
* Domain/model objects
* Repositories
* Infrastructure/integrations
* Evaluation components

Business logic should not be placed directly inside controllers.

Prefer deterministic application logic where deterministic logic is sufficient.

AI should be used where it provides meaningful value, particularly natural-language understanding and reasoning about ambiguous roadside assistance requests.

Do not use AI merely for cosmetic or unnecessary functionality.

---

# 5. Baseline Solution

The baseline represents a simple deterministic approach.

The baseline should:

1. Receive a natural-language roadside assistance request.
2. Classify the request using deterministic rules/keywords.
3. Determine the required service type.
4. Find providers capable of providing that service.
5. Rank suitable providers primarily by distance.
6. Return the best available provider.

Example:

Input:

"My battery is dead and my car won't start."

Expected classification:

JUMP_START

The baseline must remain deterministic and must not depend on an LLM.

The baseline exists so that the advanced solution can be evaluated fairly against it.

---

# 6. Advanced Solution

The advanced solution should meaningfully improve on the baseline.

The advanced system should use an AI agent/LLM to interpret natural-language roadside assistance requests, particularly ambiguous or context-dependent requests.

The agent should produce structured information such as:

* Service type
* Urgency
* Relevant situation facts
* Confidence
* Possible fallback service where appropriate

AI output must be validated by deterministic application logic before it affects the final provider recommendation.

The LLM must not have unrestricted control over the application.

The application should remain responsible for:

* Validation
* Provider eligibility
* Provider capability
* Distance calculations
* Business rules
* Safety constraints
* Final action authorization

The goal is:

> Agent reasoning + deterministic engineering controls.

---

# 7. Provider Matching

Provider matching should consider more than simple geographic proximity when appropriate.

A provider should be considered eligible only if they:

* Provide the required service.
* Are available.
* Meet the required capabilities for the request.

Distance should be calculated consistently and transparently.

The nearest provider should not automatically win if another provider is better qualified for the specific situation.

---

# 8. RabbitMQ

RabbitMQ should be used for a meaningful asynchronous workflow rather than simply being included as a technology demonstration.

The intended use case is simulated provider dispatch and fallback.

Example:

1. Saidika selects Provider A.
2. A dispatch request is created.
3. Provider A is simulated as timing out or declining.
4. A provider failure event is published.
5. A fallback workflow consumes the event.
6. Saidika selects the next qualified provider.
7. The process ends with a simulated successful provider acceptance.

All provider dispatch actions must remain simulated.

Do not perform real-world consequential actions.

---

# 9. Safety and Human Control

This is a hackathon prototype.

Do not integrate real emergency dispatch or automatically contact real-world emergency services.

Do not make consequential decisions without appropriate safeguards.

Provider dispatch should be simulated.

Where the system encounters potentially dangerous situations, it should prefer safe recommendations and clearly communicate uncertainty.

AI recommendations must be validated before being acted upon.

---

# 10. Evaluation

The baseline and advanced solutions must be evaluated using the same fixed test scenarios.

Do not modify evaluation scenarios simply to make the advanced solution appear better.

The primary metric is:

## Correct Assistance Resolution Rate

This measures the percentage of evaluation scenarios in which Saidika identifies an appropriate assistance service and provider outcome.

Secondary metrics may include:

* Service classification accuracy
* Provider suitability
* Provider attempts before successful resolution
* Fallback success rate
* Processing latency

Do not introduce unnecessary metrics.

All claimed improvements must be supported by actual evaluation results.

Never invent, estimate, or fabricate performance numbers.

---

# 11. Evaluation Dataset

Use synthetic evaluation data.

The evaluation scenarios should cover:

* Straightforward requests
* Ambiguous requests
* Requests containing multiple clues
* Provider capability conflicts
* Provider unavailability
* Provider timeout/decline
* Difficult edge cases
* At least one challenging scenario

The same scenarios must be used for baseline and advanced evaluation.

Once evaluation begins, do not change scenarios simply because they produce unfavorable results.

---

# 12. Testing Requirements

Meaningful business logic must have automated tests.

At minimum, test:

* Service classification
* Provider filtering
* Provider matching
* Distance calculation
* Provider ranking
* Invalid input
* Ambiguous input
* Provider unavailability
* Provider fallback
* AI output validation

Tests should be deterministic and reproducible.

Do not remove a test merely because an implementation fails it.

Investigate the failure first.

---

# 13. Development Workflow

For every meaningful implementation task:

1. Inspect the existing code first.
2. Explain the proposed approach before making large changes.
3. Make the smallest reasonable change.
4. Do not modify unrelated files.
5. Add or update tests.
6. Run the relevant tests.
7. Report failures clearly.
8. Do not hide or suppress test failures.
9. Do not introduce unnecessary dependencies.
10. Preserve existing working functionality.

Do not implement future phases unless explicitly instructed.

---

# 14. Current Development Phase

The current phase is:

## Phase 1 — Deterministic Baseline

At this stage, focus only on:

* Domain model
* Provider data
* Service classification
* Provider matching
* Distance calculation
* Baseline tests
* Baseline evaluation

Do NOT implement yet:

* AI/LLM integration
* Agentic workflow
* RabbitMQ fallback
* Final UI
* External maps integration

Those will be implemented in later phases.

---

# 15. Git and Change Discipline

Keep changes logically separated into commits.

Prefer meaningful commit messages such as:

* `chore: initialize Saidika project`
* `feat: add roadside assistance domain model`
* `feat: implement baseline service classification`
* `feat: implement provider matching`
* `test: add baseline evaluation scenarios`
* `feat: add agent-assisted classification`
* `feat: add provider fallback workflow`

Do not rewrite or squash history unless explicitly requested.

---

# 16. Secrets and Data

Never commit:

* API keys
* Passwords
* Tokens
* Private credentials
* Personal data

Use environment variables for secrets.

Provide `.env.example` where appropriate.

Use synthetic provider and evaluation data.

---

# 17. Agent Behavior

You are acting as a coding agent assisting the human engineer.

Do not assume that the largest or most sophisticated implementation is the best implementation.

Prioritize:

1. Correctness
2. Testability
3. Reproducibility
4. Simplicity
5. Maintainability
6. Measurable improvement

When uncertain about a requirement that could materially affect architecture or scope, explain the uncertainty before proceeding.

Do not silently make major architectural decisions.

---

# 18. Hackathon Evidence

The project must ultimately document:

* Baseline implementation
* Advanced implementation
* Improvement iterations
* Evaluation results
* Important engineering decisions
* Failed experiments
* Removed experiments
* Tests
* Reproduction instructions
* Agent-use evidence/trajectories

When implementing a meaningful change, provide a concise explanation of:

* What changed
* Why it changed
* What was tested
* Whether the tests passed
* Any trade-offs or limitations

The human engineer will use this information to maintain the hackathon improvement changelog.

---

# 19. Definition of Done

A feature is not considered complete merely because the code compiles.

A meaningful feature should have:

* Correct implementation
* Appropriate tests
* Passing tests
* Clear separation of responsibilities
* No unnecessary dependencies
* No exposed secrets
* Documentation where appropriate
* Reproducible behavior

Before declaring a task complete, verify the relevant tests.

---

## Final Principle

Build the smallest system that convincingly demonstrates the engineering idea.

Do not optimize for the number of technologies used.

Optimize for:

> A real problem → a clear baseline → a meaningful agentic improvement → measurable evidence → reliable engineering → reproducible results.
