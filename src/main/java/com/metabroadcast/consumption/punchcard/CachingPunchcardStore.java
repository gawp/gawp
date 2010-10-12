package com.metabroadcast.consumption.punchcard;

import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.PreDestroy;

import org.joda.time.Duration;

import com.google.common.collect.Maps;
import com.metabroadcast.common.caching.BackgroundComputingValue;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.user.Users;

public class CachingPunchcardStore implements ConsumptionPunchcardProvider {

    private final ConsumptionPunchcardProvider delegate;
    private final Users users;
    private final BackgroundComputingValue<Map<UserRef, Punchcard>> punchcards;

    public CachingPunchcardStore(ConsumptionPunchcardProvider delegate, Users users) {
        this.delegate = delegate;
        this.users = users;
        punchcards = new BackgroundComputingValue<Map<UserRef, Punchcard>>(Duration.standardMinutes(30), new UpdatePunchcards());
        punchcards.start();
    }
    
    @PreDestroy
    public void shutdown() {
        punchcards.shutdown();
    }

    @Override
    public Punchcard punchCard(UserRef userRef) {
        return punchcards.get().get(userRef);
    }
    
    class UpdatePunchcards implements Callable<Map<UserRef, Punchcard>> {

        @Override
        public Map<UserRef, Punchcard> call() throws Exception {
            Map<UserRef, Punchcard> punchcards = Maps.newHashMap();
            
            for (UserRef userRef: users.users()) {
                punchcards.put(userRef, delegate.punchCard(userRef));
            }
            
            return punchcards;
        }
    }
}
