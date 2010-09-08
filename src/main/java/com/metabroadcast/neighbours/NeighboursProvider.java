package com.metabroadcast.neighbours;

import java.util.List;

import com.metabroadcast.common.social.model.UserRef;

public interface NeighboursProvider {

    public List<Neighbour> neighbours(UserRef userRef);

}