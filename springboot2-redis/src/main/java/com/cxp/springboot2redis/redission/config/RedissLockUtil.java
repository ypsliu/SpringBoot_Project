package com.cxp.springboot2redis.redission.config;

import java.util.concurrent.TimeUnit;

/**
 * redis分布式锁工具类
 * @author 程
 * @date 2019/7/10 下午9:26
 */
public class RedissLockUtil {

    private static DistributedLocker redissLock;

    public static void setLocker(DistributedLocker locker) {
        redissLock = locker;
    }

    public static void lock(String lockKey) {
        redissLock.lock(lockKey);
    }

    public static void unlock(String lockKey) {
        redissLock.unlock(lockKey);
    }

    /**
     * 带超时的锁
     * @param lockKey
     * @param timeout 超时时间   单位：秒
     */
    public static void lock(String lockKey, int timeout) {
        redissLock.lock(lockKey, timeout);
    }

    /**
     * 带超时的锁
     * @param lockKey
     * @param unit 时间单位
     * @param timeout 超时时间
     */
    public static void lock(String lockKey, TimeUnit unit , int timeout) {
        redissLock.lock(lockKey, unit, timeout);
    }
}
