package com.wtplatform.backend.projection;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface MonthlyTrendProjection {
    String getMonth();
    BigDecimal getTotalAmount();
    BigInteger getCount();
    String getFromFund();
    String getToFund();
} 