package com.metabroadcast.neighbours;

import java.util.List;

import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;

public interface NeighboursProvider {

    public List<Neighbour> neighbours(UserRef userRef, int limit);

    public List<Neighbour> neighbours(UserRef userRef, UserNamespace namespace, int limit);
}