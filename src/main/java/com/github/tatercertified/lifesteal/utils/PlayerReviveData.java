package com.github.tatercertified.lifesteal.utils;

public interface PlayerReviveData {
    // This means that hearts shouldn't be given if killed
    boolean newlyRevived();
    void setNewlyRevived(boolean set);
}
