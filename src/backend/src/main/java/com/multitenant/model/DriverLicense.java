package com.multitenant.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DRIVER_LICENSE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DriverLicense extends IdentityDocument {
}
