package org.driftprogramming.distributedlock;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@Order(1)
@Slf4j
public class DistributedLockAspect {
    public static final String MESSAGE_INVALID_INDEX = "Lock index out of range, please check the value.";
    public static final String MESSAGE_GET_LOCK_FAILED = "Thread %s get lock %s failed after %s %s.";

    @Resource
    private Redisson redisson;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String prefix = getLockPrefix(joinPoint, distributedLock);
        RLock lock = getLock(joinPoint.getArgs(), distributedLock.lockIndex(), prefix);
        boolean isSuccessLocked = lock.tryLock(
                distributedLock.waitTime(),
                distributedLock.expireTime(),
                distributedLock.timeUnit());
        if (isSuccessLocked) {
            try {
                return joinPoint.proceed();
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.error("Thread " + Thread.currentThread().getId() + " " + e.getMessage());
                }
            }
        } else {
            throw new RuntimeException(String.format(MESSAGE_GET_LOCK_FAILED,
                    Thread.currentThread().getId(),
                    distributedLock.lockType(),
                    distributedLock.waitTime(),
                    distributedLock.timeUnit()
            ));
        }
    }

    private String getLockPrefix(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        return distributedLock.lockType() == LockType.Default
                ? joinPoint.getSignature().getDeclaringTypeName()
                + "." + joinPoint.getSignature().getName()
                : distributedLock.lockType().toString();
    }

    private RLock getLock(Object[] params, int[] lockIndex, String prefix) {
        if (lockIndex.length > 0) {
            return getMultipleLockByParameters(params, lockIndex, prefix);
        } else {
            return redisson.getLock(prefix);
        }
    }

    private RLock getMultipleLockByParameters(Object[] params, int[] lockIndex, String prefix) {
        List<RLock> locks = new ArrayList<>();
        for (int index : lockIndex) {
            if (index >= params.length) {
                throw new RuntimeException(MESSAGE_INVALID_INDEX);
            }

            Object paramValue = params[index];
            locks.addAll(getLocksByParameterValue(paramValue, prefix));
        }

        return redisson.getMultiLock(locks.toArray(new RLock[locks.size()]));
    }

    private List<RLock> getLocksByParameterValue(Object paramValue, String prefix) {
        if (paramValue instanceof Collection<?>) {
            return ((Collection<?>) paramValue)
                    .stream()
                    .map(v -> redisson.getLock(prefix + "_" + v.toString()))
                    .collect(Collectors.toList());
        } else {
            return Collections.singletonList(redisson.getLock(prefix + "_" + paramValue));
        }
    }
}