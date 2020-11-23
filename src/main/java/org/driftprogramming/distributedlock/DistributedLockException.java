package org.driftprogramming.distributedlock;

public class DistributedLockException extends RuntimeException {
    public DistributedLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
