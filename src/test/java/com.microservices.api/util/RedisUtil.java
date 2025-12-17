package com.microservices.api.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    private static JedisPool jedisPool;

    // Initialize Redis connection pool
    static {
        String redisHost;
        int redisPort;
        String redisPassword;

        // Use environment variables if set (local dev / port-forward)
        if (System.getProperty("REDIS_HOST") != null) {
            System.out.println("setting env varable ");
            redisHost = System.getProperty("REDIS_HOST", "redis");
            redisPort = Integer.parseInt(System.getProperty("REDIS_PORT", "6379"));
            redisPassword = System.getProperty("REDIS_PASSWORD", "redisPassword");
        } else {
            System.out.println(" in else ");
            // Default to Kubernetes service name
            redisHost = "localhost";           // matches api-gateway.yaml
            redisPort = 6379;
            redisPassword = "redisPassword";
        }

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            jedisPool = new JedisPool(poolConfig, redisHost, redisPort, 2000, redisPassword);
        } else {
            jedisPool = new JedisPool(poolConfig, redisHost, redisPort);
        }

        System.out.println("Initialized RedisUtil: host=" + redisHost + ", port=" + redisPort);
    }
    /**
     * Flush the entire Redis database
     */
    public static void flushAll() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushAll();
            System.out.println("Redis DB flushed successfully.");
        } catch (Exception e) {
            System.err.println("Failed to flush Redis: " + e.getMessage());
        }
    }

    /**
     * Flush a specific Redis database (optional, default DB=0)
     */
    public static void flushDB(int dbIndex) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            jedis.flushDB();
            System.out.println("Redis DB " + dbIndex + " flushed successfully.");
        } catch (Exception e) {
            System.err.println("Failed to flush Redis DB " + dbIndex + ": " + e.getMessage());
        }
    }

    /**
     * Safe executeCommand wrapper for common Redis commands
     * Supported commands: PING, GET, SET, DEL
     */
    public static Object executeCommand(String command, String... args) {
        try (Jedis jedis = jedisPool.getResource()) {
            switch (command.toUpperCase()) {
                case "PING":
                    return jedis.ping();
                case "GET":
                    if (args.length == 1) return jedis.get(args[0]);
                    break;
                case "SET":
                    if (args.length == 2) return jedis.set(args[0], args[1]);
                    break;
                case "DEL":
                    if (args.length >= 1) return String.valueOf(jedis.del(args));
                    break;
                case "KEYS":
                    if (args.length == 1) return jedis.keys(args[0]); // returns Set<String>
                    break;
                default:
                    System.err.println("Unsupported Redis command: " + command);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Redis command failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Close the pool (call on shutdown if needed)
     */
    public static void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
