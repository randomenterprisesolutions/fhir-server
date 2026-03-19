# fhir-admin-webapp — Tenant Management Admin API

Dynamic tenant management via a REST admin API, backed by a JDBC database and integrated with `fhir-config` to replace static file-based tenant configuration.

## Background

Tenants are currently fully file-based: each tenant requires a `config/<tenantId>/fhir-server-config.json` file deployed to the server before a request can be made. This plan replaces that with a database-backed config store and a REST admin API.

---

## Database-backed Tenant Store

- `TenantConfig` — Java model for tenant configuration
- `TenantRepository` — interface: `create`, `findById`, `findAll`, `update`, `delete`
- `InMemoryTenantRepository` — default impl (no env var required; data lost on restart)
- `JdbcTenantRepository` — JDBC impl backed by the `TENANT_CONFIG` table (active when `FHIR_ADMIN_DB_URL` is set)
- `TenantRepositoryProducer` — CDI `@Produces` that selects the correct impl at startup
- `TenantAdminSchema` — idempotent DDL: creates `TENANT_CONFIG` table if absent

```sql
CREATE TABLE TENANT_CONFIG (
    TENANT_ID         VARCHAR(64)   PRIMARY KEY,
    DISPLAY_NAME      VARCHAR(256),
    DATASTORE_TYPE    VARCHAR(64)   NOT NULL,
    DATASTORE_HOST    VARCHAR(256),
    DATASTORE_PORT    INTEGER,
    DATASTORE_DATABASE VARCHAR(256),
    FHIR_SERVER_CONFIG CLOB,
    ENABLED           SMALLINT      NOT NULL DEFAULT 1,
    CONFIG_VERSION    BIGINT        NOT NULL DEFAULT 0,
    CREATED_AT        BIGINT        NOT NULL,
    UPDATED_AT        BIGINT        NOT NULL
);
```

---

## DB-backed Config Loading

**Modified `fhir-config`:**

- `DatabaseTenantConfigSource` — reads `FHIR_SERVER_CONFIG` JSON and `CONFIG_VERSION` from the admin DB via `DriverManager`; no driver compile-time dependency
- `DatabaseAwareTenantPropertyGroupCache` — extends `TenantSpecificPropertyGroupCache`; DB-first resolution with per-tenant version tracking; falls back to on-disk files for tenants not in the DB (backward-compatible)
- `FHIRConfiguration.createPropertyGroupCache()` — factory method that returns the DB-aware cache when `FHIR_ADMIN_DB_URL` is set, otherwise returns the original file-based cache (no breaking change)

---

## Schema Provisioning

When a new tenant is created, `TenantService.createTenant()` calls `SchemaProvisioner.provision(TenantConfig)`.

- `SchemaProvisioner` — CDI interface; called synchronously after every successful tenant creation
- `NoOpSchemaProvisioner` — default `@ApplicationScoped` implementation; logs the equivalent `fhir-persistence-schema` CLI command so operators can run it manually
- To wire full automatic provisioning: implement `SchemaProvisioner` and expose it as a CDI `@ApplicationScoped` bean; the producer will inject it into `TenantService` automatically

---

## Admin REST API

**New module: `fhir-admin-webapp`** (separate WAR, separate port e.g. 9444)

```
POST   /admin/tenants                      Create tenant + provision schema
GET    /admin/tenants                      List all tenants
GET    /admin/tenants/{tenantId}           Get tenant config
PUT    /admin/tenants/{tenantId}           Update tenant config
DELETE /admin/tenants/{tenantId}           Disable/delete tenant
POST   /admin/tenants/{tenantId}/reload    Force config cache reload
```

Request body for `POST /admin/tenants`:
```json
{
  "tenantId": "acme",
  "displayName": "Acme Corp",
  "datastoreType": "postgresql",
  "datastoreHost": "...",
  "fhirServerConfig": { ... }
}
```

**Security:** Admin API protected separately from the FHIR API — mTLS or a static admin token via Liberty's admin role, NOT via `X-FHIR-TENANT-ID`.

---

## Cache Invalidation

- `TENANT_CONFIG.CONFIG_VERSION` — a monotonic counter stored in the admin DB; starts at 0 on create
- `POST /admin/tenants/{tenantId}/reload` bumps `CONFIG_VERSION` in the DB and returns 202
- `DatabaseAwareTenantPropertyGroupCache` checks the DB version on every `getCachedObjectForTenant()` call; forces a reload from `FHIR_SERVER_CONFIG` when the version has advanced
- Multi-node propagation is poll-based: each node's next FHIR request that touches the tenant will detect the version change and reload from the DB — no direct node-to-node communication is required

---

## Files Created / Modified

| File | Change |
|---|---|
| `fhir-admin-webapp/.../admin/db/TenantAdminSchema.java` | New — idempotent DDL for `TENANT_CONFIG` |
| `fhir-admin-webapp/.../admin/repository/TenantRepository.java` | New — DAO interface |
| `fhir-admin-webapp/.../admin/repository/InMemoryTenantRepository.java` | New — in-memory default |
| `fhir-admin-webapp/.../admin/repository/JdbcTenantRepository.java` | New — JDBC implementation |
| `fhir-admin-webapp/.../admin/repository/TenantRepositoryProducer.java` | New — CDI producer |
| `fhir-admin-webapp/.../admin/schema/SchemaProvisioner.java` | New — interface |
| `fhir-admin-webapp/.../admin/schema/NoOpSchemaProvisioner.java` | New — default impl |
| `fhir-admin-webapp/.../admin/model/TenantConfig.java` | Modified — added `configVersion` field |
| `fhir-admin-webapp/.../admin/service/TenantService.java` | Modified — schema provisioning + config-version wiring |
| `fhir-config/.../config/DatabaseTenantConfigSource.java` | New — DB config reader |
| `fhir-config/.../config/DatabaseAwareTenantPropertyGroupCache.java` | New — DB-first cache |
| `fhir-config/.../config/FHIRConfiguration.java` | Modified — factory for cache impl |
| `fhir-admin-webapp/pom.xml` | Modified — Derby test dependency |

---

## Migration Path

1. Default config source remains file-based — no breaking change; set `FHIR_ADMIN_DB_URL` to activate JDBC persistence
2. Existing on-disk configs continue to work even when JDBC is active (file fallback in `DatabaseAwareTenantPropertyGroupCache`)
3. `POST /admin/tenants` with `fhirServerConfig` field populates the DB; use `POST /admin/tenants/{id}/reload` to push the change to all nodes without a restart

---

## Authentication

The Admin API supports two authentication mechanisms that can operate side-by-side.

### 1 — HTTP Basic (default)

All endpoints are protected by HTTP Basic authentication with the `FHIRAdminRealm` realm.

Liberty's `basicRegistry` defines a single `fhiradmin` user (member of the `FHIRAdmins` group).
The password is read from the environment variable `FHIR_ADMIN_PASSWORD`.

```bash
curl -u fhiradmin:$FHIR_ADMIN_PASSWORD \
     https://localhost:9444/fhir-admin/admin/tenants
```

No additional Liberty configuration is required for HTTP Basic.

---

### 2 — JWT Bearer (optional)

The Admin API supports MicroProfile JWT (mpJwt-1.2) as an alternative to HTTP Basic.
JWT Bearer is **not active by default** — enable it by moving the pre-built configuration
dropin from `configDropins/disabled/` to `configDropins/overrides/`.

#### How scope-based authorisation works

Liberty's `mpJwt` is configured with `groupNameAttribute="scope"`.
It splits the space-delimited `scope` string from the JWT into individual group names.

A token that carries the `fhir_admin` scope:

```json
{
  "sub": "ci-pipeline",
  "iss": "https://keycloak.example.com/auth/realms/admin",
  "scope": "openid fhir_admin"
}
```

is automatically mapped to the Liberty group `fhir_admin`, which the
`<application-bnd>` block in the dropin maps to the `FHIRAdmins` application role.
All endpoints are annotated `@RolesAllowed("FHIRAdmins")`, so only tokens
that carry the `fhir_admin` scope are authorised.

#### Enabling JWT Bearer

1. **Supply the three required environment variables:**

   | Variable | Example value |
   |---|---|
   | `FHIR_ADMIN_JWT_JWKS_URI` | `https://keycloak:8443/auth/realms/admin/protocol/openid-connect/certs` |
   | `FHIR_ADMIN_JWT_ISSUER` | `https://keycloak:8443/auth/realms/admin` |
   | `FHIR_ADMIN_JWT_AUDIENCES` | `https://fhir-admin:9444/fhir-admin` |

2. **Move the dropin from `disabled/` to `overrides/`:**

   ```bash
   cp src/main/liberty/config/configDropins/disabled/jwtRS.xml \
      src/main/liberty/config/configDropins/overrides/jwtRS.xml
   ```

   At runtime Liberty picks up dropin changes live, so no restart is required.

3. **Make a request with a Bearer token:**

   ```bash
   TOKEN=$(curl -s -X POST \
     https://keycloak:8443/auth/realms/admin/protocol/openid-connect/token \
     -d grant_type=client_credentials \
     -d client_id=fhir-admin-client \
     -d client_secret=$CLIENT_SECRET \
     -d scope="fhir_admin" | jq -r .access_token)

   curl -H "Authorization: Bearer $TOKEN" \
        https://localhost:9444/fhir-admin/admin/tenants
   ```

#### Customising the scope or claim name

The dropin uses `groupNameAttribute="scope"` (space-delimited scopes) by default.
If your authorization server puts admin grants in a `groups` array claim, adjust
the dropin:

```xml
<!-- use a "groups" array claim instead of scope -->
<mpJwt id="fhirAdminJwtConsumer"
       ...
       groupNameAttribute="groups"
       .../>

<security-role name="FHIRAdmins">
    <group name="fhir-admins"
           access-id="group:${env.FHIR_ADMIN_JWT_ISSUER}/fhir-admins"/>
</security-role>
```

#### Relationship between Basic and JWT auth

When the JWT dropin is active both mechanisms remain enabled simultaneously.
Liberty selects the authenticator based on the `Authorization` header:

| Header prefix | Mechanism |
|---|---|
| `Basic …` | `basicRegistry` (fhiradmin user) |
| `Bearer …` | mpJwt token validation |

HTTP Basic is convenient for operator scripts and local testing; JWT Bearer is
recommended for automated CI pipelines and service-to-service calls.

---

## Running the admin webapp

**Prerequisites:** JDK 11+, Maven 3.8+, Open Liberty (the `server.xml` ships in `src/main/liberty/config/`).

**Environment variables:**

| Variable | Required | Description |
|---|---|---|
| `FHIR_ADMIN_PASSWORD` | Yes | Password for the built-in `fhiradmin` user |
| `FHIR_ADMIN_DB_URL` | No | JDBC URL for the admin database. When set, activates `JdbcTenantRepository` and `DatabaseAwareTenantPropertyGroupCache`. Omit to use the in-memory store. |

**Build and run (in-memory store):**

```bash
cd fhir-admin-webapp
export FHIR_ADMIN_PASSWORD=changeme
mvn package liberty:run
```

**Build and run (JDBC / Derby in-memory for development):**

```bash
export FHIR_ADMIN_PASSWORD=changeme
export FHIR_ADMIN_DB_URL="jdbc:derby:memory:fhir-admin;create=true"
mvn package liberty:run
```

The API is then available at `https://localhost:9444/fhir-admin/admin/tenants`.

The admin webapp does **not** need to co-locate with `fhir-server-webapp` at build time.
Both are independent WARs that can run in the same Liberty server (different context roots)
or as completely separate processes.

---

## Implementation status

| Item | Status |
|---|---|
| REST API — all 6 endpoints | ✅ complete |
| HTTP Basic auth (Liberty basicRegistry) | ✅ complete |
| JWT Bearer auth (mpJwt-1.2 dropin) | ✅ complete |
| OpenAPI 3.0 specification | ✅ complete |
| JDBC-backed tenant repository | ✅ complete (`JdbcTenantRepository` + `TenantRepositoryProducer`) |
| DB-backed config loading in `fhir-config` | ✅ complete (`DatabaseTenantConfigSource` + `DatabaseAwareTenantPropertyGroupCache`) |
| Schema provisioning on tenant create | ✅ complete (`SchemaProvisioner` interface + `NoOpSchemaProvisioner`) |
| Config-cache reload across cluster nodes | ✅ complete (`CONFIG_VERSION` bump on reload; version-aware cache in `fhir-config`) |
| Unit tests | ✅ 54 passing |
| Integration tests (E2E tenant lifecycle with Derby) | ✅ 26 passing |

---

## Interaction with the main FHIR server

All five phases are complete. The two services share state through the database — no direct service-to-service calls are ever required:

```
Client
  ├── HTTPS :9443  →  fhir-server-webapp   (FHIR R4/R4B REST API)
  └── HTTPS :9444  →  fhir-admin-webapp    (Admin REST API)
                          │
                     TENANT_CONFIG table (shared DB)
                          │
                    fhir-server-webapp reads config via
                    DatabaseAwareTenantPropertyGroupCache
                    (when FHIR_ADMIN_DB_URL is set)
```

1. `POST /admin/tenants` persists the config to the shared `TENANT_CONFIG` table and calls `SchemaProvisioner.provision()` (no-op by default; wire a real provisioner for automatic schema creation).
2. `fhir-config` reads tenant configuration from the same table via `DatabaseAwareTenantPropertyGroupCache` (file configs remain as a fallback).
3. `POST /admin/tenants/{tenantId}/reload` bumps `CONFIG_VERSION` in the DB.
4. Each FHIR server node detects the version change on its next request for that tenant and reloads the config from `FHIR_SERVER_CONFIG` in the DB — no restart required.

---

## Known limitations

| Limitation | Workaround |
|---|---|
| **`NoOpSchemaProvisioner`** — a new tenant's FHIR persistence schema is not created automatically | Run the `fhir-persistence-schema` CLI manually after `POST /admin/tenants`, or implement `SchemaProvisioner` and expose it as a CDI `@ApplicationScoped` bean |
| **Single built-in user** — the `basicRegistry` defines one `fhiradmin` user | Replace with an LDAP registry or use JWT Bearer with an external IdP for multi-user scenarios |
| **Separate Liberty config required** — the admin webapp needs its own `server.xml` (port 9444) | Deploy as a second application inside the same Liberty server instance or as a separate process |
| **Poll-based invalidation only** — config reload is driven by the next FHIR request for a given tenant, not by a push notification | Acceptable for most deployments; for sub-second propagation, add a periodic background poll in `DatabaseAwareTenantPropertyGroupCache` |
