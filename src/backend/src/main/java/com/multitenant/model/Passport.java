package com.multitenant.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("PASSPORT")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Passport extends IdentityDocument {
}
