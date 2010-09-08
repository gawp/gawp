package com.metabroadcast.neighbours;

import com.metabroadcast.common.social.model.UserRef;

public class Neighbour {

    private final UserRef neighbour;
    private final Number similarity;

    public Neighbour(UserRef neighbour, Number similarity) {
        this.neighbour = neighbour;
        this.similarity = similarity;
    }
    
    public UserRef neighbour() {
        return neighbour;
    }
    
    public Number similarity() {
        return similarity;
    }
    
    @Override
    public int hashCode() {
        return neighbour.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Neighbour) {
            Neighbour target = (Neighbour) obj;
            return neighbour.equals(target.neighbour) && similarity.equals(target.similarity);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "Neighbour: "+neighbour+" with similarity of "+similarity;
    }
}
