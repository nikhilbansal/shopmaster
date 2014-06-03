package com.shoppingbox.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikhilbansal on 26/05/14.
 */
public class Identifier {
    private List<Pair> identifierPairs;

    public Identifier() {
        identifierPairs = new ArrayList<Pair>();
    }

    public Identifier(List<Pair> identifierPairs) {
        this.identifierPairs = identifierPairs;
    }

    public List<Pair> getIdentifierPairs() {
        return identifierPairs;
    }

    public void setIdentifierPairs(List<Pair> identifierPairs) {
        this.identifierPairs = identifierPairs;
    }
}
