# Graph Report - USER-SERVICE-ECOMMERCE  (2026-05-10)

## Corpus Check
- 36 files · ~8,147 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 213 nodes · 283 edges · 25 communities (7 shown, 18 thin omitted)
- Extraction: 69% EXTRACTED · 31% INFERRED · 0% AMBIGUOUS · INFERRED: 88 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1acc150d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]

## God Nodes (most connected - your core abstractions)
1. `KeycloakService` - 13 edges
2. `UserService` - 13 edges
3. `AuthService` - 9 edges
4. `USER & AUTH SERVICE` - 9 edges
5. `AddressController` - 8 edges
6. `GlobalExceptionHandler` - 8 edges
7. `UserMapper` - 8 edges
8. `AddressService` - 8 edges
9. `users` - 8 edges
10. `AuthController` - 7 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Communities (25 total, 18 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.1
Nodes (5): GlobalExceptionHandler, AddressMapper, AuthService, KeycloakService, users

### Community 1 - "Community 1"
Cohesion: 0.12
Nodes (4): AddressController, AuthController, UserController, ApiResponse

### Community 2 - "Community 2"
Cohesion: 0.1
Nodes (3): UserMapper, UserRepository, UserService

### Community 3 - "Community 3"
Cohesion: 0.1
Nodes (20): Address Endpoints (Authenticated), addresses, Admin Endpoints (Admin Role), Admin Features, API Documentation, API Endpoints, Authentication, Authentication Endpoints (Public) (+12 more)

### Community 5 - "Community 5"
Cohesion: 0.17
Nodes (12): Bước 1: Start Keycloak & PostgreSQL, Bước 2: Cấu hình Keycloak, Bước 3: Build & Run, Cài đặt & Chạy, Cấu hình môi trường (.env), code:bash (cp .env.example .env), code:bash (KAFKA_BOOTSTRAP_SERVERS=<host1:9092,host2:9092>), code:bash (mvn spring-boot:run) (+4 more)

### Community 7 - "Community 7"
Cohesion: 0.2
Nodes (4): AuthenticationException, ResourceNotFoundException, UserAlreadyExistsException, RuntimeException

### Community 8 - "Community 8"
Cohesion: 0.2
Nodes (10): 1. Register User, 2. Login, 3. Get Current User (với token), 4. Create Address, code:bash (curl -X POST http://localhost:8081/api/users/me/addresses \), code:bash (curl -X POST http://localhost:8081/api/auth/register \), code:bash (curl -X POST http://localhost:8081/api/auth/login \), code:json ({) (+2 more)

## Knowledge Gaps
- **38 isolated node(s):** `KafkaProducerProperties`, `Admin`, `UserRegisteredEvent`, `CreateAddressRequest`, `LoginRequest` (+33 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **18 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `USER & AUTH SERVICE` connect `Community 3` to `Community 8`, `Community 5`?**
  _High betweenness centrality (0.215) - this node is a cross-community bridge._
- **Why does `users` connect `Community 0` to `Community 3`?**
  _High betweenness centrality (0.198) - this node is a cross-community bridge._
- **Why does `Database Schema` connect `Community 3` to `Community 0`?**
  _High betweenness centrality (0.192) - this node is a cross-community bridge._
- **What connects `KafkaProducerProperties`, `Admin`, `UserRegisteredEvent` to the rest of the system?**
  _38 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._