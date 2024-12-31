package com.github.certifiedtater.lifesteal.gamerules;

/**
 * @author Ampflower
 * @since ${version}
 **/
public enum DeathAction {
    REVIVE("revive"),
    SPECTATOR("spectator"),
    BAN("ban"),
    // The string is intentionally invalid for the command argument.
    UNSET("uninitialised [internal use only]"),
    ;

    public final String name;

    DeathAction(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
