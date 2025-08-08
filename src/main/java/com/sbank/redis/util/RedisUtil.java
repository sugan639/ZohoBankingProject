package com.sbank.redis.util;

import redis.clients.jedis.Jedis;


public class RedisUtil {

	
	public void setCache(String key, String value) {
	
	try(Jedis jedis = new Jedis()){
		
		jedis.set(key, value);
	}
	
	

	}
	
	public String getSessionData(String key) {
		
		try (Jedis jedis = new Jedis()) {
            String sessionJsonString = jedis.get(key);
            
            if (sessionJsonString != null) {
            	return sessionJsonString;
            }
		}
		
		return null;
		
	}
	
	
	
    // 🔹 Delete session from Redis
	public void deleteSession(String sessionId) {
	    try (Jedis jedis = new Jedis()) {
	        jedis.del(sessionId);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	
}
















































//
//
//package com.sbank.redis.util;
//
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisPool;
//
//public class RedisUtil {
//
//    private static final JedisPool jedisPool = new JedisPool("localhost", 6379); // Customize if needed
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    private static final int SESSION_EXPIRY_SECONDS = 30 * 60; // 30 minutes
//
//    private String getKey(String sessionId) {
//        return "session:" + sessionId;
//    }
//
//    // 🔹 Store session data in Redis
//    public void saveSessionData(String sessionId, SessionData sessionData) {
//        try (Jedis jedis = jedisPool.getResource()) {
//            String key = getKey(sessionId);
//            String json = objectMapper.writeValueAsString(sessionData);
//            jedis.setex(key, SESSION_EXPIRY_SECONDS, json);
//        } catch (JsonProcessingException e) {
//            System.err.println("RedisUtil.saveSessionData: Failed to serialize session data");
//            e.printStackTrace();
//        }
//    }
//
//    // 🔹 Retrieve session data from Redis
//    public SessionData getSessionData(String sessionId) {
//        try (Jedis jedis = jedisPool.getResource()) {
//            String key = getKey(sessionId);
//            String json = jedis.get(key);
//            if (json != null) {
//                return objectMapper.readValue(json, SessionData.class);
//            }
//        } catch (Exception e) {
//            System.err.println("RedisUtil.getSessionData: Error while reading from Redis");
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    // 🔹 Delete session from Redis
//    public void deleteSession(String sessionId) {
//        try (Jedis jedis = jedisPool.getResource()) {
//            jedis.del(getKey(sessionId));
//        }
//    }
//
//}
