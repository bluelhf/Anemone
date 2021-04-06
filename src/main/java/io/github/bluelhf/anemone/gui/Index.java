package io.github.bluelhf.anemone.gui;

/**
 * An Index contains information about a slot inside an Anemone.
 * */
public class Index {
    private final char c;
    private final int page, charIndex, totalIndex;

    public Index(char c, int page, int charIndex, int totalIndex) {
        this.c = c;
        this.page = page;
        this.charIndex = charIndex;
        this.totalIndex = totalIndex;
    }

    public char getChar() {
        return c;
    }

    public int getPage() {
        return page;
    }

    public int getCharIndex() {
        return charIndex;
    }

    public int getTotalIndex() {
        return totalIndex;
    }
}
