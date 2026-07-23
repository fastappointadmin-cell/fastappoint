# FastAppoint API Reference - Capability Registry

## Base URL
```
http://localhost:8080/api
```

## Capability Endpoints

### 1. Get All Capabilities for a Business

**Request**
```http
GET /capabilities?businessId=<uuid>
```

**Response (200 OK)**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "businessId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "name": "speaks_english",
    "description": "Can communicate in English"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "businessId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "name": "wheelchair_accessible",
    "description": "Wheelchair accessible entry"
  }
]
```

---

### 2. Create a New Capability

**Request**
```http
POST /capabilities?businessId=<uuid>&name=speaks_english&description=Can communicate in English
```

**Response (201 Created)**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "businessId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "speaks_english",
  "description": "Can communicate in English"
}
```

**Error (409 Conflict)**
```json
{
  "message": "Capability 'speaks_english' already exists for this business"
}
```

---

### 3. Get Capability by ID

**Request**
```http
GET /capabilities/<id>
```

**Response (200 OK)**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "businessId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "speaks_english",
  "description": "Can communicate in English"
}
```

---

### 4. Update Capability

**Request**
```http
PATCH /capabilities/<id>?description=Updated description
```

**Response (200 OK)**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "businessId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "speaks_english",
  "description": "Updated description"
}
```

---

### 5. Delete Capability

**Request**
```http
DELETE /capabilities/<id>
```

**Response (204 No Content)**

---

## Resource Endpoints (Updated)

### Create Resource with Capabilities

**Request**
```http
POST /resources?businessId=<uuid>
Content-Type: application/json

{
  "name": "Marius",
  "typeName": "Barber",
  "capacity": 1,
  "capabilityIds": [
    "550e8400-e29b-41d4-a716-446655440000",
    "550e8400-e29b-41d4-a716-446655440001"
  ]
}
```

**Response (200 OK)**
```json
{
  "id": "660f9511-f30c-52e5-b827-557766550111",
  "businessId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "typeId": "770g0622-g41d-63f6-c938-668877660222",
  "name": "Marius",
  "typeName": "Barber",
  "capacity": 1,
  "capabilities": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "speaks_english"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "senior_haircuts"
    }
  ]
}
```

---

### Get Resources for a Business

**Request**
```http
GET /resources?businessId=<uuid>
```

**Response (200 OK)**
```json
[
  {
    "id": "660f9511-f30c-52e5-b827-557766550111",
    "businessId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "typeId": "770g0622-g41d-63f6-c938-668877660222",
    "name": "Marius",
    "typeName": "Barber",
    "capacity": 1,
    "capabilities": [
      { "id": "550e8400-e29b-41d4-a716-446655440000", "name": "speaks_english" },
      { "id": "550e8400-e29b-41d4-a716-446655440001", "name": "senior_haircuts" }
    ]
  },
  {
    "id": "660f9511-f30c-52e5-b827-557766550112",
    "businessId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "typeId": "770g0622-g41d-63f6-c938-668877660222",
    "name": "Anna",
    "typeName": "Barber",
    "capacity": 1,
    "capabilities": [
      { "id": "550e8400-e29b-41d4-a716-446655440000", "name": "speaks_english" },
      { "id": "550e8400-e29b-41d4-a716-446655440002", "name": "speaks_spanish" }
    ]
  }
]
```

---

## Service Endpoints (Updated)

### Add Service Requirement with Capabilities

**Request**
```http
POST /services/<serviceId>/requirements
Content-Type: application/json

{
  "resourceTypeIds": ["770g0622-g41d-63f6-c938-668877660222"],
  "allocationMode": "SINGLE",
  "quantity": 1,
  "requiredCapabilityIds": [
    "550e8400-e29b-41d4-a716-446655440000"
  ],
  "occupationDurationSeconds": null
}
```

**Response (200 OK)**
```json
{
  "id": "880h1733-h52e-74g7-d949-779988771333",
  "businessId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "Haircut",
  "durationSeconds": 3600,
  "requirements": [
    {
      "id": "990i2844-i63f-85h8-e050-880099882444",
      "serviceId": "880h1733-h52e-74g7-d949-779988771333",
      "resourceTypeName": "Barber",
      "allocationMode": "SINGLE",
      "quantity": 1,
      "demandParameter": null,
      "requiredCapabilities": [
        {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "name": "speaks_english"
        }
      ],
      "occupationDurationSeconds": null
    }
  ]
}
```

### Complex Service: Team Lunch (Multiple Requirements)

**Request**
```http
POST /services/<serviceId>/requirements
Content-Type: application/json

{
  "resourceTypeIds": ["table-type-id"],
  "allocationMode": "MERGE",
  "demandParameter": "partySize",
  "requiredCapabilityIds": [
    "outdoor-seating-cap-id"
  ]
}
```

Then add another requirement:

```http
POST /services/<serviceId>/requirements
Content-Type: application/json

{
  "resourceTypeIds": ["server-type-id"],
  "allocationMode": "SINGLE",
  "quantity": 1,
  "requiredCapabilityIds": [
    "speaks-english-cap-id"
  ]
}
```

**Final Service Response**
```json
{
  "id": "880h1733-h52e-74g7-d949-779988771333",
  "businessId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "Team Lunch",
  "durationSeconds": 7200,
  "requirements": [
    {
      "id": "req-001",
      "serviceId": "880h1733-h52e-74g7-d949-779988771333",
      "resourceTypeName": "Table",
      "allocationMode": "MERGE",
      "quantity": null,
      "demandParameter": "partySize",
      "requiredCapabilities": [
        { "id": "outdoor-cap-id", "name": "outdoor_seating" }
      ],
      "occupationDurationSeconds": null
    },
    {
      "id": "req-002",
      "serviceId": "880h1733-h52e-74g7-d949-779988771333",
      "resourceTypeName": "Server",
      "allocationMode": "SINGLE",
      "quantity": 1,
      "demandParameter": null,
      "requiredCapabilities": [
        { "id": "english-cap-id", "name": "speaks_english" }
      ],
      "occupationDurationSeconds": null
    }
  ]
}
```

---

## TypeScript Usage Examples

### 1. Create a Capability

```typescript
// In a component
constructor(private backend: BackendService) {}

createCapability(businessId: string) {
  this.backend.createCapability(
    businessId,
    'speaks_english',
    'Can communicate in English'
  ).subscribe({
    next: (capability) => {
      console.log('Created:', capability);
      // Update UI
    },
    error: (err) => {
      console.error('Failed to create capability:', err);
    }
  });
}
```

### 2. Get and Display Capabilities

```typescript
export class CapabilityListComponent implements OnInit {
  capabilities$ = this.route.parent!.params.pipe(
    switchMap(params => this.backend.getCapabilities(params['businessId']))
  );

  constructor(
    private backend: BackendService,
    private route: ActivatedRoute
  ) {}
}
```

### 3. Create Resource with Capabilities

```typescript
createResource(businessId: string, capabilityIds: string[]) {
  const request: CreateResourceRequest = {
    name: 'Marius',
    typeName: 'Barber',
    capacity: 1,
    capabilityIds: capabilityIds
  };

  this.backend.createResource(businessId, request).subscribe({
    next: (resource) => {
      console.log('Resource created with capabilities:', resource.capabilities);
    },
    error: (err) => {
      console.error('Failed to create resource:', err);
    }
  });
}
```

### 4. Add Service Requirement with Capabilities

```typescript
addRequirement(
  serviceId: string,
  resourceTypeId: string,
  capabilityIds: string[]
) {
  const request: AddServiceRequirementRequest = {
    resourceTypeIds: [resourceTypeId],
    allocationMode: 'SINGLE',
    quantity: 1,
    requiredCapabilityIds: capabilityIds
  };

  this.backend.addServiceRequirement(serviceId, request).subscribe({
    next: (service) => {
      console.log('Requirement added:', service.requirements);
    },
    error: (err) => {
      console.error('Failed to add requirement:', err);
    }
  });
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "message": "Capability name cannot be empty"
}
```

### 404 Not Found
```json
{
  "message": "Business not found with ID: <uuid>"
}
```

### 409 Conflict
```json
{
  "message": "Capability 'speaks_english' already exists for this business"
}
```

---

## Migration Path from Old API

### Old Request (String-based Capabilities)
```http
POST /resources?businessId=<uuid>
{
  "name": "Marius",
  "typeName": "Barber",
  "capacity": 1,
  "capabilities": ["speaks_english", "senior_haircuts"]  // Strings
}
```

### New Request (UUID-based Capabilities)
```http
POST /resources?businessId=<uuid>
{
  "name": "Marius",
  "typeName": "Barber",
  "capacity": 1,
  "capabilityIds": [
    "550e8400-e29b-41d4-a716-446655440000",
    "550e8400-e29b-41d4-a716-446655440001"
  ]  // UUIDs from capability registry
}
```

**Migration Steps:**
1. First, create all capability definitions via POST /capabilities
2. Get capability IDs from the responses
3. Update requests to use `capabilityIds` instead of `capabilities`
4. Responses will now return `CapabilityRefDto` objects instead of strings

---

## Summary

The new architecture with managed capabilities provides:

✅ **Type Safety**: UUIDs instead of strings
✅ **Centralized Management**: Single source of truth for capabilities
✅ **Reusability**: Define once, reference everywhere
✅ **Better UI/UX**: Dropdowns instead of free-form input
✅ **Consistency**: No duplicates or typos
✅ **Scalability**: Easy to add new capabilities

Backend fully implemented and ready for frontend consumption!

