package com.fintracker_backend.fintracker.service;

import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;



@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final Client geminiClient;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.model.fallback}")
private String fallbackModel;

   public String getAIResponse(String prompt) {

    log.info("AI request started | model={} | promptLength={}", model, prompt.length());

    try {
        GenerateContentResponse response = geminiClient.models
                .generateContent(model, prompt, null);

        log.info("AI response success | model={}", model);

        return response.text();

    } catch (ClientException e) {

        log.error("AI primary model failed | model={} | error={}", model, e.getMessage());

        if (e.getMessage() != null && e.getMessage().contains("429")) {

            log.warn("Rate limit hit | switching to fallbackModel={}", fallbackModel);

            try {
                GenerateContentResponse fallback = geminiClient.models
                        .generateContent(fallbackModel, prompt, null);

                log.info("Fallback model success | model={}", fallbackModel);

                return fallback.text();

            } catch (Exception ex) {

                log.error("Fallback model failed | model={}", fallbackModel, ex);

                return getStaticFallback();
            }
        }

        return "AI error: " + e.getMessage();
    }
}

private String getStaticFallback() {
    return """
        INSIGHTS:
        1. Try reducing unnecessary expenses.
        2. Track spending weekly.
        3. Increase savings gradually.

        PREDICTION:
        PREDICTION: ₹--
        REASON: AI unavailable.

        ALERTS:
        ⚠️ Unable to analyze due to API limits.

        BUDGETS:
        Essentials: ₹--
        Savings: ₹--
        """;
}
@Recover
public String recover(Exception e, String prompt) {

    log.error("AI retry exhausted | error={}", e.getMessage(), e);

    // Smart fallback
    if (prompt.contains("INSIGHTS:")) {
        return """
            INSIGHTS:
            1. Monitor your spending habits regularly.
            2. Reduce unnecessary expenses.
            3. Keep at least 20% savings.

            PREDICTION:
            PREDICTION: ₹--
            REASON: AI unavailable.

            ALERTS:
            ⚠️ AI limit reached. Try again later.

            BUDGETS:
            Essentials: ₹--
            Savings: ₹--
            """;
    }

    return "AI is temporarily unavailable. Please try again shortly.";
}
    // 💬 Finance Chat
public String chatWithAI(String userMessage, String financialContext) {
    String prompt = """
            You are a helpful personal finance assistant. The user has the following financial context:
            %s
            
            Answer this question concisely and helpfully: %s
            """.formatted(financialContext, userMessage);
    return getAIResponse(prompt);
}

// 🔮 Next Month Prediction
public String predictNextMonth(String spendingHistory) {
    String prompt = """
            You are a financial analyst. Based on this monthly spending history:
            %s
            
            Predict next month's likely expense as a single number in ₹ only.
            Then in one sentence explain why. Format:
            PREDICTION: ₹<amount>
            REASON: <one sentence>
            """.formatted(spendingHistory);
    return getAIResponse(prompt);
}

// ⚠️ Overspending Alert
public String analyzeOverspending(String categoryData, BigDecimal totalExpense, BigDecimal totalIncome) {
    String prompt = """
            You are a financial advisor. Analyze if the user is overspending.
            
            Income : ₹%s
            Expense: ₹%s
            Category Breakdown: %s
            
            Give 2-3 specific overspending warnings if any exist.
            Format each as: ⚠️ <warning>
            If spending is healthy, say: ✅ Your spending looks healthy this month.
            """.formatted(totalIncome, totalExpense, categoryData);
    return getAIResponse(prompt);
}

// 🎯 Budget Suggestions
public String suggestBudgets(String categoryData, BigDecimal totalIncome) {
    String prompt = """
            You are a budgeting expert. Based on the user's income of ₹%s and their spending:
            %s
            
            Suggest a recommended monthly budget for each category.
            Format each as: <Category>: ₹<amount>
            Use the 50/30/20 rule as a guideline.
            """.formatted(totalIncome, categoryData);
    return getAIResponse(prompt);
}
}