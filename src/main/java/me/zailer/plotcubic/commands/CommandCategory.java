package me.zailer.plotcubic.commands;

public enum CommandCategory {
    GENERAL("General"),
    ADMIN("Admin"),
    MODERATION("Moderation"),
    COSMETIC("Cosmetic");

    private final String name;

    CommandCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
