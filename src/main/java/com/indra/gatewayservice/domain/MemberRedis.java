package com.indra.gatewayservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@RedisHash(value = "member", timeToLive = 3600)
@Builder
public class MemberRedis {
    @Id
    private long id;
    private String accountId;
    private String name;
    private String role;
    private long companyId;
}
