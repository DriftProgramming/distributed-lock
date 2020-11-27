package org.driftprogramming.distributedlock.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.driftprogramming.distributedlock.DistributedLockException;
import org.driftprogramming.distributedlock.LockType;
import org.driftprogramming.distributedlock.annotation.DistributedLock;
import org.driftprogramming.distributedlock.provider.LockKeyProvider;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aspect
@Component
@Order(1)
@Slf4j
public class DistributedLockAspect {
    public static final String MESSAGE_INVALID_INDEX = "Lock index out of range, please check the value.";
    public static final String MESSAGE_GET_LOCK_FAILED = "Thread %s get lock %s failed after %s %s.";
    public static final String MESSAGE_DISTRIBUTED_LOCK_FAILED = "Distributed lock failed.";
    public static final String KEY_SEPARATOR = ".";

    @Resource
    private Redisson redisson;

    @Autowired
    private LockKeyProvider keyProvider;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        return execute(joinPoint, distributedLock);
    }

    private Object execute(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String prefix = null;
        RLock lock = null;
        try {
            prefix = getLockPrefix(joinPoint, distributedLock);
            lock = getLock(joinPoint, distributedLock, prefix);
        } catch (Exception e) {
            throw new DistributedLockException(e.getMessage(), e);
        }

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
            throw new DistributedLockException(String.format(MESSAGE_GET_LOCK_FAILED,
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

    private RLock getLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock, String prefix) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        List<List<String>> keyCollections = getKeyCollections(method, distributedLock, joinPoint.getArgs());
        List<String> keys = keyCollections.stream()
                .flatMap(Collection::stream)
                .filter(o -> o != null && o != "")
                .map(o -> prefix + KEY_SEPARATOR + o)
                .collect(Collectors.toList());

        return this.getLockByKeys(new HashSet<String>(keys));
    }

    private List<List<String>> getKeyCollections(Method method, DistributedLock annotation, Object[] arguments) {
        if (annotation.key().length == 0) {
            return Collections.singletonList(keyProvider.get(null, method, arguments));
        } else {
            return Stream.of(annotation.key())
                    .map(keyDefinition -> keyProvider.get(keyDefinition, method, arguments))
                    .collect(Collectors.toList());
        }
    }

    private RLock getLockByKeys(HashSet<String> keys) {
        List<RLock> locks = new ArrayList<>();
        for (String key : keys) {

            locks.add(redisson.getLock(key));
        }

        return redisson.getMultiLock(locks.toArray(new RLock[locks.size()]));
    }
}