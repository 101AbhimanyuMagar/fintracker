package com.fintracker_backend.fintracker.service;

import java.util.List;
import com.fintracker_backend.fintracker.dto.InsightDTO;

public interface InsightService {
    List<InsightDTO> generateInsights(String email);
}