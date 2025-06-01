package com.wtplatform.backend.projection;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface MonthlyTrendProjection {
    String getMonth();
    BigInteger getCount();
    BigDecimal getTotalAmount();
} 