package com.trombipeti.simplecallblocker.model;


import java.util.ArrayList;

public class BlockProfilesSingleton {

    private BlockProfile defaultProfile;
    private ArrayList<BlockProfile> blockProfiles;

    private static BlockProfilesSingleton myInstance = null;

    private BlockProfilesSingleton() {
        defaultProfile = new BlockProfile("Dummy", false);
        blockProfiles = new ArrayList<>();
    }

    public synchronized static BlockProfilesSingleton Instance() {
        if(myInstance == null) {
            myInstance = new BlockProfilesSingleton();
        }
        return myInstance;
    }

    public synchronized boolean addBlockProfile(BlockProfile profile) {
        return blockProfiles.add(profile);
    }

    public synchronized BlockProfile getDefaultProfile() {
        return defaultProfile;
    }

    public synchronized void setDefaultProfile(BlockProfile defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    public synchronized boolean removeBlockProfile(BlockProfile profile) {
        if(profile.equals(defaultProfile)) {
            throw new RuntimeException("Cannot remove default block profile");
        }
        return blockProfiles.remove(profile);
    }

    public synchronized BlockProfile removeBlockProfile(int index) {
        if(index == 0) {
            throw new RuntimeException("Cannot remove default block profile");
        }
        return blockProfiles.remove(index - 1);
    }

    public synchronized void clear() {
        blockProfiles.clear();
    }

    public synchronized BlockProfile get(int index) {
        if(index == 0) {
            return defaultProfile;
        }
        return blockProfiles.get(index - 1);
    }

    public synchronized void modifyEnabled(int index, boolean isEnabled) {
        get(index).setEnabled(isEnabled);
    }

    public synchronized void modifyAllBlock(int index, boolean isAllBlock) {
        get(index).setAllBlock(isAllBlock);
    }

    public synchronized int size() {
        return blockProfiles.size() + 1;
    }
}
