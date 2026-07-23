# FastAppoint Frontend Implementation Guide

## UI/UX Organization

The frontend should be organized into distinct, hierarchical sections to reflect the architecture:

## Section 1: Capability Management (Admin Setup)

### Location
Dashboard → Settings → Capabilities

### Components Needed
1. **CapabilityListComponent**
   - Display all capabilities for a business
   - Show: ID, Name, Description, Usage count
   - Actions: Edit, Delete

2. **CapabilityFormComponent**
   - Modal/form for creating and editing
   - Fields: Name (required), Description (optional)
   - Validation: Unique name per business

3. **CapabilityService** (Angular)
   - Wrap BackendService capability endpoints
   - Caching for performance

### UI Example
```
[Capabilities Management]
┌─────────────────────────────────────────┐
│ + Add Capability                         │
├─────────────────────────────────────────┤
│ Name                │ Description      │ │
├─────────────────────┼──────────────────┤ │
│ speaks_english      │ Can speak English │  Edit | Delete
│ wheelchair_access   │ Accessible entry │  Edit | Delete
│ senior_haircuts     │ Expert senior care
│ wifi                │ Has WiFi          │  Edit | Delete
└─────────────────────────────────────────┘
```

---

## Section 2: Resource Management

### Location
Dashboard → Resources

### Components Needed

#### A. Resource Type Overview (Readonly)
- Show all resource types in the business
- Auto-created from resources
- Display count of resources per type

#### B. Resource List
- Group resources by ResourceType
- For each resource show:
  - Name, Type
  - Capacity (if applicable)
  - Capabilities (as badges/tags)
  - Availability summary

#### C. Create Resource Modal/Form
- Fields:
  - **Name** (text): "Marius"
  - **Resource Type** (selector):
    - Existing types as dropdown
    - Option to type new type name
  - **Capacity** (optional number): "1"
  - **Capabilities** (multi-select from capability registry):
    - Shows all capabilities for the business
    - Can select multiple
    - Display as checklist or multi-select

#### D. Edit Resource
- Modify name, capacity
- Add/remove capabilities
- Cannot change type (maintain referential integrity)

### UI Example
```
[Resources]
┌──────────────────────────────────────────────┐
│ + Add Resource                               │
├──────────────────────────────────────────────┤
│
│ [Barber] (2 resources)
│ ├─ Marius
│ │  Capacity: 1
│ │  Capabilities: speaks_english, senior_haircuts
│ │  Edit | Delete
│ │
│ └─ Anna
│    Capacity: 1
│    Capabilities: speaks_english, speaks_spanish
│    Edit | Delete
│
│ [Table] (4 resources)
│ ├─ Table 12
│ │  Capacity: 4
│ │  Capabilities: outdoor, window_view
│ │  Edit | Delete
│
└──────────────────────────────────────────────┘

[Create/Edit Resource Modal]
┌────────────────────────────────────────┐
│ Resource Name: [Marius           ]     │
│                                        │
│ Resource Type: [Barber          ▼]    │
│                                        │
│ Capacity (optional): [1          ]    │
│                                        │
│ Capabilities:                          │
│ ☐ speaks_english                       │
│ ☐ speaks_spanish                       │
│ ☑ senior_haircuts                      │
│ ☐ wheelchair_accessible                │
│ ☐ wifi                                 │
│                                        │
│ [Cancel]  [Create]                     │
└────────────────────────────────────────┘
```

---

## Section 3: Service Management

### Location
Dashboard → Services

### Components Needed

#### A. Service List
- Show all services for business
- Display: Name, Duration, # of requirements
- Actions: Edit, Add Requirements, Delete

#### B. Service Details View
- Service info: Name, Duration
- List of requirements with:
  - Resource Type
  - Allocation Mode
  - Quantity / Demand Parameter
  - Required Capabilities (as badges)

#### C. Create Service Form
- Fields:
  - **Name** (text): "Haircut"
  - **Duration** (seconds): 3600
  - Submit → Create, then can add requirements

#### D. Add Service Requirement Modal
- **Step 1: Resource Type Selection**
  - Dropdown of all ResourceTypes
  - Can only select one at a time
  - (Note: MULTIPLE mode can actually use multiple types)

- **Step 2: Allocation Mode**
  - Radio buttons: SINGLE | MULTIPLE | MERGE
  - Help text explaining each mode

- **Step 3: Quantity Configuration**
  - If SINGLE/MULTIPLE: 
    - Spinner for quantity (min 1)
  - If MERGE:
    - Text field for demand parameter name (e.g., "partySize")
    - Option for fixed quantity (for fallback)

- **Step 4: Required Capabilities**
  - Multi-select from capability registry
  - Shows all capabilities
  - Can filter by typing
  - Display selected as removable tags

- **Step 5: Optional Occupation Duration**
  - Toggle for custom occupation duration
  - Time input (defaults to service duration)

### UI Example
```
[Services]
┌──────────────────────────────────────────────┐
│ + Add Service                                │
├──────────────────────────────────────────────┤
│
│ Haircut (60 mins)
│ └─ Requirements: 1
│    Edit | Delete
│
│ Team Lunch (120 mins)
│ └─ Requirements: 2
│    Edit | Delete
│
└──────────────────────────────────────────────┘

[Service Details: Haircut]
┌──────────────────────────────────────────────┐
│ Name: Haircut                                │
│ Duration: 60 minutes                         │
│                                              │
│ Requirements                                 │
│ ┌────────────────────────────────────────┐  │
│ │ 1. Barber (SINGLE)                     │  │
│ │    Quantity: 1                         │  │
│ │    Required Capabilities:              │  │
│ │    [speaks_english]                    │  │
│ │    Edit | Remove                       │  │
│ └────────────────────────────────────────┘  │
│                                              │
│ + Add Requirement                            │
└──────────────────────────────────────────────┘

[Add Requirement Modal - Step 4: Capabilities]
┌────────────────────────────────────────┐
│ Select Required Capabilities for Barber│
│ (All selected capabilities must exist  │
│  on the resource)                      │
│                                        │
│ Filter: [________]                     │
│                                        │
│ ☐ speaks_english                       │
│ ☐ speaks_spanish                       │
│ ☑ senior_haircuts                      │
│ ☐ wheelchair_accessible                │
│ ☐ availability_evening                 │
│                                        │
│ Selected: senior_haircuts              │
│ (can remove by clicking tag)           │
│                                        │
│ [Back] [Next]                          │
└────────────────────────────────────────┘
```

---

## Implementation Steps

### Phase 1: Backend Integration
- [ ] Test capability endpoints
- [ ] Verify request/response formats
- [ ] Test capability CRUD

### Phase 2: Capability Management UI
- [ ] Create CapabilityService (Angular)
- [ ] Create CapabilityListComponent
- [ ] Create CapabilityFormComponent
- [ ] Add to settings/admin area

### Phase 3: Resource Management Updates
- [ ] Update CreateResourceRequest to use capabilityIds
- [ ] Create/update ResourceService (Angular)
- [ ] Update resource create/edit forms
- [ ] Add capability multi-select to resource form

### Phase 4: Service Management Updates
- [ ] Update AddServiceRequirementRequest to use capabilityIds
- [ ] Create/update ServiceService (Angular)
- [ ] Update requirement form with capability multi-select
- [ ] Display capabilities as badges in requirements list

### Phase 5: Polish & Testing
- [ ] Add loading states
- [ ] Add error handling
- [ ] Add success notifications
- [ ] Validate all flows

---

## Code Examples

### 1. Angular Service for Capabilities
```typescript
import { Injectable } from '@angular/core';
import { BackendService } from './backend.service';
import { CapabilityDto } from './backend.types';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class CapabilityService {
  private cache = new Map<string, BehaviorSubject<CapabilityDto[]>>();

  constructor(private backend: BackendService) {}

  getCapabilities(businessId: string): Observable<CapabilityDto[]> {
    if (!this.cache.has(businessId)) {
      this.cache.set(businessId, new BehaviorSubject<CapabilityDto[]>([]));
      this.backend.getCapabilities(businessId)
        .pipe(
          tap(capabilities => this.cache.get(businessId)!.next(capabilities))
        )
        .subscribe();
    }
    return this.cache.get(businessId)!.asObservable();
  }

  createCapability(businessId: string, name: string, description?: string): Observable<CapabilityDto> {
    return this.backend.createCapability(businessId, name, description)
      .pipe(
        tap(newCapability => {
          const current = this.cache.get(businessId);
          if (current) {
            current.next([...current.value, newCapability]);
          }
        })
      );
  }
}
```

### 2. Resource Create Form (Partial)
```typescript
export class ResourceCreateComponent {
  resourceForm: FormGroup;
  capabilities$: Observable<CapabilityDto[]>;

  constructor(
    private fb: FormBuilder,
    private capabilityService: CapabilityService,
    private backendService: BackendService,
    private businessService: BusinessService
  ) {
    this.resourceForm = this.fb.group({
      name: ['', Validators.required],
      typeName: ['', Validators.required],
      capacity: [null],
      capabilityIds: [[]] // Array of UUIDs
    });

    this.capabilities$ = this.businessService.currentBusinessId$.pipe(
      switchMap(businessId => 
        this.capabilityService.getCapabilities(businessId)
      )
    );
  }

  onSubmit() {
    if (this.resourceForm.valid) {
      const payload: CreateResourceRequest = {
        name: this.resourceForm.value.name,
        typeName: this.resourceForm.value.typeName,
        capacity: this.resourceForm.value.capacity,
        capabilityIds: this.resourceForm.value.capabilityIds
      };
      
      // Submit to backend...
    }
  }
}
```

### 3. Capability Multi-Select Component
```typescript
export class CapabilityMultiSelectComponent {
  @Input() capabilities: CapabilityDto[] = [];
  @Input() selectedIds: string[] = [];
  @Output() selectionChange = new EventEmitter<string[]>();

  toggleCapability(capabilityId: string) {
    const index = this.selectedIds.indexOf(capabilityId);
    if (index > -1) {
      this.selectedIds.splice(index, 1);
    } else {
      this.selectedIds.push(capabilityId);
    }
    this.selectionChange.emit([...this.selectedIds]);
  }

  isSelected(capabilityId: string): boolean {
    return this.selectedIds.includes(capabilityId);
  }
}
```

---

## Summary: Key Architectural Improvements

| Aspect | Before | After |
|--------|--------|-------|
| Capability Storage | Strings in Resource/Requirement | Managed Capability entity |
| Capability Definition | Scattered, free-form | Centralized registry |
| Capability Reuse | Manual duplication | Single reference |
| Type Safety | Weak (strings) | Strong (UUIDs) |
| Frontend UI | Free text input | Dropdowns/multi-select |
| Data Consistency | Weak | Strong (DB constraints) |
| Capability Tracking | Difficult | Easy (count usages) |

This architecture makes the system more maintainable, scalable, and user-friendly!

