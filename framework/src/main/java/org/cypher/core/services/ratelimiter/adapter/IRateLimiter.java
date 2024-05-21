package org.cypher.core.services.ratelimiter.adapter;

import org.cypher.core.services.ratelimiter.RuntimeData;

public interface IRateLimiter {

  boolean acquire(RuntimeData data);

}
