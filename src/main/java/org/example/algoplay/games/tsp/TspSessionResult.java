package org.example.algoplay.games.tsp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Model class representing a combined result from the TSP sessions query
 */
public class TspSessionResult {
    private Long sessionId;
    private String sessionUuid;
    private Integer cityCount;
    private LocalDateTime createdAt;
    private String source; // "User" or algorithm name
    private String username;
    private Long userId;
    private Long runId;
    private Double pathLength;
    private Long executionTimeMs;
    private List<Integer> cityOrder; // Storing city order as a list of city indices

    public TspSessionResult() {
        cityOrder = new ArrayList<>();
    }

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionUuid() {
        return sessionUuid;
    }

    public void setSessionUuid(String sessionUuid) {
        this.sessionUuid = sessionUuid;
    }

    public Integer getCityCount() {
        return cityCount;
    }

    public void setCityCount(Integer cityCount) {
        this.cityCount = cityCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public Double getPathLength() {
        return pathLength;
    }

    public void setPathLength(Double pathLength) {
        this.pathLength = pathLength;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public List<Integer> getCityOrder() {
        return cityOrder;
    }

    public void setCityOrder(List<Integer> cityOrder) {
        this.cityOrder = cityOrder;
    }

    public void setCityOrderFromString(String cityOrderStr) {
        // Parse the city order string from the database format
        // Expected format: "[1, 2, 3, 4, 5]" or similar formats
        if (cityOrderStr != null && !cityOrderStr.isEmpty()) {
            // Remove brackets and split by comma
            String cleaned = cityOrderStr.replaceAll("[\\[\\]]", "").trim();
            if (!cleaned.isEmpty()) {
                String[] cities = cleaned.split(",");
                cityOrder.clear();
                for (String city : cities) {
                    try {
                        cityOrder.add(Integer.parseInt(city.trim()));
                    } catch (NumberFormatException e) {
                        // Skip invalid numbers
                    }
                }
            }
        }
    }

    public String getCityOrderAsString() {
        if (cityOrder == null || cityOrder.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cityOrder.size(); i++) {
            sb.append(cityOrder.get(i));
            if (i < cityOrder.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "TspSessionResult{" +
                "sessionId=" + sessionId +
                ", sessionUuid='" + sessionUuid + '\'' +
                ", cityCount=" + cityCount +
                ", source='" + source + '\'' +
                ", runId=" + runId +
                ", pathLength=" + pathLength +
                ", executionTimeMs=" + executionTimeMs +
                ", cityOrder=" + getCityOrderAsString() +
                '}';
    }
}