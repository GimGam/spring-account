package com.example.account.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

@Slf4j
@Configuration
public class LocalRedisConfig {
    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis(){
        redisServer = new RedisServer(redisPort);
        redisServer.start();

        //겪은 문제.
        //AccountApplication 을 Run 시키고 중지시키기 위해
        //인텔리제이 IDE 의 STOP 으로 정지를 시키니까
        //콘솔창에 build cancelled while executing task ':accountapplication.main() 라는 에러가 뜬다

        //이전까진 잘 돌아가서 문제 없었는데
        //제대로 종료가 되지 않았는지 @PreDestroy로 등록해놓은 REDIS서버 STOP 함수가 호출이 안되어서
        //계속해서 REDIS서버는 남아있고 포트 점유율 충돌 문제때문에 그 다음부터는 START함수에서 에러가 생겼다.


        //해결.
        //build cancelled while executing task ':accountapplication.main() 라는 에러 없애기 위해
        //SETTING-GRADLE-에서 빌드 관련을 IDE로 변경함..
        //수업 프로젝트는 제대로 따라간거같은데 버전 차이 문제인가?
    }

    @PreDestroy
    public void stopRedis(){
        if(redisServer != null){
            log.info("redis stop success!!!!");
            redisServer.stop();
        }
    }
}
