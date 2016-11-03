package com.trombipeti.simplecallblocker.model;

import java.util.LinkedHashSet;

public class BlockProfile {

    private LinkedHashSet<Contact> contacts;

    private String name;

    private boolean allBlock;
    private boolean enabled;

    public BlockProfile(String name, boolean block, boolean isEnabled) {
        this.name = name;
        allBlock = block;
        contacts = new LinkedHashSet<>();
        enabled = isEnabled;
    }

    public BlockProfile(String name, boolean block) {
        this.name = name;
        allBlock = block;
        contacts = new LinkedHashSet<>();
        enabled = true;
    }

    public int getContactsNum() {
        return contacts.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String theName) {
        name = theName;
    }

    public boolean isAllBlock() {
        return allBlock;
    }

    public void setAllBlock(boolean allBlock) {
        this.allBlock = allBlock;
    }

    public boolean contains(Contact contact) {
        return (contacts.contains(contact));
    }

    public Contact get(int index) {
        int i = 0;
        for(Contact c : contacts) {
            if(i == index) {
                return c;
            }
            i++;
        }
        return null;
    }

    public boolean remove(int index) {
        int i = 0;
        for(Contact c : contacts) {
            if(i == index) {
                return contacts.remove(c);
            }
            i++;
        }
        return false;
    }

    public void clear() {
        contacts.clear();
    }

    public boolean addContact(Contact contact) {
        return contacts.add(contact);
    }

    public boolean removeContact(Contact contact) {
        return contacts.remove(contact);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
