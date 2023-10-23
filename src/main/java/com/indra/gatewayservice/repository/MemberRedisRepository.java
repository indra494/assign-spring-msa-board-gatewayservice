package com.indra.gatewayservice.repository;

import com.indra.gatewayservice.domain.MemberRedis;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MemberRedisRepository extends CrudRepository<MemberRedis, Long> {
    Optional<MemberRedis> findById(Long id);
}
