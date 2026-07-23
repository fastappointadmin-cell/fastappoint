# FastAppoint: Capability Registry Architecture

## Overview

This document describes the improved architecture for managing capabilities, resources, and services in FastAppoint. The system now features a clear **separation of concerns** with managed capabilities as a first-class registry entity.

## Key Components

### 1. **Capability Registry** (New)
- **Purpose**: Central registry for all capabilities available in a business
- **Scope**: Per-business (tenant-scoped)
- **Implementation**: `Capability` entity with find-or-create pattern (like ResourceType)
- **API Endpoints**:
  - `GET /api/capabilities?businessId=<uuid>` - List all capabilities for a business
  - `POST /api/capabilities?businessId=<uuid>&name=<name>&description=<desc>` - Create a capability
  - `PATCH /api/capabilities/<id>?description=<desc>` - Update capability description
  - `DELETE /api/capabilities/<id>` - Delete a capability

**Example Capabilities:**
- `speaks_english`, `speaks_spanish`, `speaks_french` (language skills)
- `handicap_accessible`, `wheelchair_accessible` (accessibility)
- `wifi`, `parking`, `outdoor_seating` (amenities)
- `certified_senior_care`, `pediatric_specialist` (expertise)

### 2. **Resource Types**
- **Purpose**: Categories of resources (e.g., "Barber", "Table", "Hairdryer")
- **Scope**: Per-business (auto-created on-demand)
- **Key Point**: Separate from resource instances
- **Usage**: Specify what kind of resources a service requires

**Example Resource Types:**
- "Barber" (a person who cuts hair)
- "Table" (seating area)
- "Treatment Room" (location)

### 3. **Resources**
- **Purpose**: Concrete, bookable instances (e.g., "Marius" the Barber, "Table 12")
- **Attributes**:
  - `name`: Display name
  - `type`: Reference to ResourceType
  - `capacity`: Optional numeric attribute (seats, sqft, etc.) for MERGE mode
  - `capabilities`: Set of managed Capability references (e.g., "speaks_english", "wheelchair_accessible")
  - `availability`: Recurring weekly windows when available
  
**Example Resources:**
- Resource: name="Marius", type="Barber", capabilities=["speaks_english", "senior_haircuts"]
- Resource: name="Table 12", type="Table", capacity=4, capabilities=["window_view", "outdoor"]

### 4. **Service Requirements** (Part of Service Definition)
- **Purpose**: Specify what resources and capabilities a service needs
- **Attributes**:
  - `resourceType`: Which type of resource is needed
  - `allocationMode`: SINGLE (1 resource), MULTIPLE (>1 resources), or MERGE (capacity-based)
  - `quantity`: Fixed count of resources needed (for SINGLE/MULTIPLE)
  - `demandParameter`: Dynamic parameter name (for MERGE, e.g., "partySize")
  - `requiredCapabilities`: Set of managed Capability references the resource must have
  - `occupationDuration`: How long this resource is occupied (defaults to service duration)

**Example Service Requirements:**
```
Service: "Haircut" (60 minutes)
├── Requirement 1: 1 Barber with capabilities ["speaks_english"]
└── (No other requirements)

Service: "Team Lunch" (120 minutes)
├── Requirement 1: MERGE Tables (capacity >= partySize parameter)
│   with capabilities ["outdoor"] (preferred outdoor seating)
└── Requirement 2: 1 Server with capabilities ["speaks_english"]
```

## Architecture Diagram

```
Business (Aggregate Root)
├── ResourceTypes (find-or-create)
│   └── Example: "Barber", "Table", "Room"
├── Resources (concrete instances)
│   └── Example: "Marius" (type="Barber"), "Table 12" (type="Table")
├── Capabilities (managed registry)
│   └── Example: "speaks_english", "wheelchair_accessible", "wifi"
└── Services (what we offer)
    └── ServiceRequirements
        ├── Resource Type needed
        ├── Allocation Mode (SINGLE/MULTIPLE/MERGE)
        └── Required Capabilities (from capability registry)
```

## Frontend Separation of Concerns

### Layer 1: **Capability Management** (Admin Setup)
- Create/list/edit business capabilities
- This is a one-time setup per business
- Capabilities are the vocabulary for resource matching

**UI Components:**
- Capability Manager: Create/edit/delete capabilities
- Show capability name, description, usage count

### Layer 2: **Resource Type Definition** (Infrastructure)
- Auto-created when first resource is added with that type
- List all resource types in the business

**UI Components:**
- Resource Type selector (dropdown when creating resources)

### Layer 3: **Resource Creation** (Resource Inventory)
- Create concrete resource instances
- Assign capabilities from the capability registry
- Set availability schedules

**UI Components:**
- Resource form: name, type, capacity, select capabilities from multi-select
- Show resources grouped by type
- Capability indicators on each resource

### Layer 4: **Service Definition** (Business Logic)
- Create services and define requirements
- For each requirement: select resource type + select required capabilities from registry

**UI Components:**
- Service form: name, duration
- Requirements section:
  - Resource type selector
  - Allocation mode (SINGLE/MULTIPLE/MERGE)
  - Quantity or demand parameter
  - Multi-select for required capabilities (from capability registry)

## Data Flow Example: Creating a "Haircut" Service

1. **Setup Phase** (Admin)
   - Create capability: "speaks_english"
   - Create capability: "senior_haircuts"
   - (Auto-created) Resource Type: "Barber"

2. **Resource Inventory Phase**
   - Create Resource: "Marius" (type="Barber", capabilities=["speaks_english", "senior_haircuts"])
   - Create Resource: "Anna" (type="Barber", capabilities=["speaks_english"])

3. **Service Definition Phase**
   - Create Service: "Haircut" (60 minutes)
   - Add Requirement: 
     - ResourceType="Barber"
     - Mode=SINGLE, Quantity=1
     - RequiredCapabilities=["speaks_english"]
   - (Result: Can use either Marius or Anna)

4. **Booking Phase**
   - Customer books "Haircut" for Monday 10:00 AM
   - System matches "speaks_english" requirement → finds Marius or Anna
   - Allocation: Marius 10:00-11:00 → Haircut

## Backend DTOs

### Request DTOs
- `CreateResourceRequest`: Include `capabilityIds: UUID[]`
- `AddServiceRequirementRequest`: Include `requiredCapabilityIds: UUID[]`

### Response DTOs
- `CapabilityDto`: Full capability with description
- `CapabilityRefDto`: Lightweight reference (id + name) used in nested DTOs
- `ResourceDto`: Now includes `capabilities: CapabilityRefDto[]`
- `ServiceRequirementDto`: Now includes `requiredCapabilities: CapabilityRefDto[]`

## Migration Notes

- **Backward Compatibility**: Old string-based capabilities are replaced with managed Capability references
- **Zero Downtime**: CapabilityService uses find-or-create pattern
- **Existing Capabilities**: Can be migrated from string tags to managed capabilities

## Benefits of This Architecture

1. ✅ **Clear Separation**: Capabilities ≠ Resources ≠ Services
2. ✅ **Reusability**: Define capabilities once, use in multiple services
3. ✅ **Consistency**: Single source of truth for capability naming
4. ✅ **Scalability**: Easier to add vertical-specific capabilities
5. ✅ **UI/UX**: Capability dropdowns in multiple places (no free-form strings)
6. ✅ **Analytics**: Track capability usage across resources and services
7. ✅ **Multi-tenancy**: Capabilities scoped per business

## Future Enhancements

- Capability hierarchies (e.g., "language" category with "speaks_english", "speaks_spanish")
- Capability metadata (icons, colors for UI)
- Capability templates per vertical (beauty, hospitality, healthcare)
- Soft constraints (prefer vs. require)

