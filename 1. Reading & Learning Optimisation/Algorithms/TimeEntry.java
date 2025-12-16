package Algorithms;

public class TimeEntry {
    private final double timeSec;
    private final int wordCount;

    public TimeEntry(double timeSec, int wordCount) {
        this.timeSec = timeSec;
        this.wordCount = wordCount;
    }

    public double getTimeSec() {
        return timeSec;
    }

    public int getWordCount() {
        return wordCount;
    }
}