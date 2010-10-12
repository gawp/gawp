package com.metabroadcast.consumption.punchcard;

import com.metabroadcast.common.social.model.UserRef;

public interface ConsumptionPunchcardProvider {

    Punchcard punchCard(UserRef userRef);
    
}
