package com.vbalan.rate_limiter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientConfiguration {
  private int requestsPerMinute;
  private int burstCapacity;
}
