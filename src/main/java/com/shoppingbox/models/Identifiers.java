package com.shoppingbox.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikhilbansal on 26/05/14.
 */
public class Identifiers {
    private List<Identifier> identifiers;

    public Identifiers() {
        identifiers = new ArrayList<Identifier>();
    }

    public Identifiers(List<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public List<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<Identifier> identifiers) {
        this.identifiers = identifiers;
    }
}
