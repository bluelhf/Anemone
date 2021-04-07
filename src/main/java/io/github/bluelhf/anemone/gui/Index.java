package io.github.bluelhf.anemone.gui;

/**
 * An Index contains information about a slot inside an Anemone.
 * It namely contains the page number, the character at its slot in the Anemone template, the total slot number (including previous pages), and the total amount of times the character it represents has appeared (including previous pages).
 * @see Anemone#getTemplate()
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

    /**
     * Returns the character that exists at the index in an {@link Anemone}'s template
     * @return The character that exists at the index in an {@link Anemone}'s template
     * */
    public char getChar() {
        return c;
    }

    /**
     * Returns the page that the view context this index was generated for was on
     * @return The page that the view context this index was generated for was on
     * */
    public int getPage() {
        return page;
    }

    /**
     * Returns how many times this index' character has appeared in all prior indices, including previous pages
     * @return How many times this index' character has appeared in all prior indices, including previous pages
     * */
    public int getCharIndex() {
        return charIndex;
    }

    /**
     * Returns how many indices exist previous to this one, including previous pages
     * @return How many indices exist previous to this one, including previous pages
     * */
    public int getTotalIndex() {
        return totalIndex;
    }
}
