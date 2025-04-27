package org.example.algoplay.models;

import javafx.beans.property.*;

public class HanoiDataEntry {
    private final StringProperty username;
    private final StringProperty gameName;
    private final IntegerProperty diskCount;
    private final IntegerProperty moveCount;
    private final IntegerProperty optimalMoves;
    private final BooleanProperty isCorrect;
    private final LongProperty recursiveTime;
    private final LongProperty iterativeTime;
    private final LongProperty fourPegTime;
    private final StringProperty moveSequence;

    public HanoiDataEntry(String username, String gameName, Integer diskCount, Integer moveCount,
                          Integer optimalMoves, Boolean isCorrect, Long recursiveTime,
                          Long iterativeTime, Long fourPegTime, String moveSequence) {
        this.username = new SimpleStringProperty(username);
        this.gameName = new SimpleStringProperty(gameName);
        this.diskCount = new SimpleIntegerProperty(diskCount != null ? diskCount : 0);
        this.moveCount = new SimpleIntegerProperty(moveCount != null ? moveCount : 0);
        this.optimalMoves = new SimpleIntegerProperty(optimalMoves != null ? optimalMoves : 0);
        this.isCorrect = new SimpleBooleanProperty(isCorrect != null ? isCorrect : false);
        this.recursiveTime = new SimpleLongProperty(recursiveTime != null ? recursiveTime : 0);
        this.iterativeTime = new SimpleLongProperty(iterativeTime != null ? iterativeTime : 0);
        this.fourPegTime = new SimpleLongProperty(fourPegTime != null ? fourPegTime : 0);
        this.moveSequence = new SimpleStringProperty(moveSequence);
    }

    // Getters
    public String getUsername() { return username.get(); }
    public String getGameName() { return gameName.get(); }
    public Integer getDiskCount() { return diskCount.get(); }
    public Integer getMoveCount() { return moveCount.get(); }
    public Integer getOptimalMoves() { return optimalMoves.get(); }
    public Boolean getIsCorrect() { return isCorrect.get(); }
    public Long getRecursiveTime() { return recursiveTime.get(); }
    public Long getIterativeTime() { return iterativeTime.get(); }
    public Long getFourPegTime() { return fourPegTime.get(); }
    public String getMoveSequence() { return moveSequence.get(); }

    // Property getters
    public StringProperty usernameProperty() { return username; }
    public StringProperty gameNameProperty() { return gameName; }
    public IntegerProperty diskCountProperty() { return diskCount; }
    public IntegerProperty moveCountProperty() { return moveCount; }
    public IntegerProperty optimalMovesProperty() { return optimalMoves; }
    public BooleanProperty isCorrectProperty() { return isCorrect; }
    public LongProperty recursiveTimeProperty() { return recursiveTime; }
    public LongProperty iterativeTimeProperty() { return iterativeTime; }
    public LongProperty fourPegTimeProperty() { return fourPegTime; }
    public StringProperty moveSequenceProperty() { return moveSequence; }
}