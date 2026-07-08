package com.multitenant.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CentralizatorRecoltareDto {
      private String cultura;
      private String tipMediu;
      private Double cantitateTotalaKg;
      private Double suprafataTotalaMp;
      private Double randamentKgMp;
}
