package jimuanco.jimslog.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jimuanco.jimslog.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;

@Slf4j
@Profile("!prod")
@Configuration
public class EmbeddedRedisConfig {
    @Value("${spring.data.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() throws IOException {
        int port = ProcessUtils.isRunningPort(redisPort) ? ProcessUtils.findAvailablePort() : redisPort;
        redisServer = RedisServer.builder()
                .port(port)
                .setting("maxmemory 128M")
                .build();
        redisServer.start();
        log.info("인메모리 Redis 서버가 시작됩니다. port: {}", port);
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
            log.info("인메모리 Redis 서버가 종료됩니다. port: {}", redisPort);
        }
    }
}