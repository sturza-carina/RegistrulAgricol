# Lombok @Data in JPA Entities: Problem and Solution

This document explains **Problem #12** regarding the usage of Lombok's `@Data` annotation on JPA (Jakarta Persistence / Hibernate) entities and outlines the recommended standard pattern for implementing safe, high-performance entities.

---

## The Problem: Why `@Data` is Dangerous in JPA Entities

Lombok's `@Data` is a convenient bundle annotation that generates:
1. `@Getter` and `@Setter`
2. `@RequiredArgsConstructor`
3. `@ToString`
4. `@EqualsAndHashCode`

While useful for simple Data Transfer Objects (DTOs) or POJOs, using `@Data` on JPA entities introduces several critical issues:

### 1. Eager Loading of Lazy Relationships (N+1 Query Problem)
The auto-generated `@EqualsAndHashCode` and `@ToString` methods access **every non-transient field** in the class.
If your entity has lazy-loaded associations:
```java
@ManyToOne(fetch = FetchType.LAZY)
private Gospodarie gospodarie;
```
Any call to `equals()`, `hashCode()`, or `toString()` (e.g., in collection checks, logging, or serialization) will force Hibernate to initialize and load the associated `gospodarie` entity from the database. This triggers unexpected extra SQL queries, severely degrading application performance.

### 2. `LazyInitializationException`
If `equals()`, `hashCode()`, or `toString()` is called after the Hibernate Session has closed (for example, in the presentation layer during JSON serialization or in standard logging), Hibernate cannot retrieve the lazy relation. This results in a runtime `org.hibernate.LazyInitializationException`.

### 3. Infinite Recursion and `StackOverflowError`
If there are bidirectional relationships:
*   `Parent` has a `@OneToMany` list of `Child`.
*   `Child` has a `@ManyToOne` association back to `Parent`.

Lombok's generated `hashCode()`, `equals()`, and `toString()` on `Parent` will call the respective methods on all `Child` entities. These, in turn, will call the methods on `Parent`, creating an infinite loop that crashes the application with a `StackOverflowError`.

---

## The Solution: Explicit Getters, Setters, and Safe Identity Methods

To resolve these issues, the codebase has been refactored to use a clean, safe, and highly performant pattern:

### 1. Use `@Getter` and `@Setter`
Rather than using `@Data`, declare `@Getter` and `@Setter` at the class level. This generates the necessary accessor methods without touching any fields automatically during object comparison or string conversion.

### 2. Implement a Custom, ID-Based `equals()`
Two instances of the same JPA entity represent the same row in the database if they share the same non-null database primary key (`id`).
*   If the entity has been persisted (ID is not null), we compare their IDs.
*   If the entity is transient (newly created and not yet saved, ID is null), identity comparison falls back to reference equality (`this == o`).

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EntityClass other = (EntityClass) o;
    return id != null && id.equals(other.getId());
}
```

### 3. Implement a Consistent, Constant `hashCode()`
The Contract of `hashCode` has two crucial requirements:
1.  **Consistency across State Transitions:** If an object is added to a `HashSet` while transient (ID is null), and is then persisted (ID becomes non-null), its `hashCode()` **must not change**. If it does, the set will look in the wrong bucket, and the entity will be "lost" or duplicated in the collection.
2.  **Equality Alignment:** If `equals()` returns `true`, `hashCode()` must return the exact same value.

To satisfy both rules safely, we return a constant value (such as the class's hash code) for all instances. While this maps all instances of the same class to the same bucket in a hash table (making collection lookups act like a list traversal in memory), it is fully correct, safe, and avoids all database-access side-effects.

```java
@Override
public int hashCode() {
    return getClass().hashCode();
}
```

---

## Complete Refactored Entity Template

Applying this standard, the typical structure for any entity in our system is now:

```java
package com.multitenant.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "my_entity_table")
@Getter
@Setter
@NoArgsConstructor
public class MyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fields and lazy-loaded associations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_id")
    private RelatedEntity relatedEntity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyEntity other = (MyEntity) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```
