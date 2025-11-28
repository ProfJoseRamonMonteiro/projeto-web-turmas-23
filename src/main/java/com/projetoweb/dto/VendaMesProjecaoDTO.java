package com.projetoweb.dto;

import java.math.BigDecimal;

public record VendaMesProjecaoDTO(
        Integer mes,
        BigDecimal total
) {}