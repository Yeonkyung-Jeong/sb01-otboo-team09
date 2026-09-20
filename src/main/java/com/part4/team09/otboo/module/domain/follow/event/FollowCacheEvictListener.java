package com.part4.team09.otboo.module.domain.follow.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowCacheEvictListener {

  private final CacheManager cacheManager;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void cacheEvictWhenFollowCreated(FollowCreatedEvent event) {
    log.info("팔로우 등록시 캐시 무효화 이벤트 처리 시작: followeeId = {}, followerId = {}", event.followeeId(),
      event.followerId());

    evictFollowSummaryCache();

    log.info("팔로우 등록시 캐시 무효화 이벤트 처리 완료: followeeId = {}, followerId = {}", event.followeeId(),
      event.followerId());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void cacheEvictWhenFollowDeleted(FollowDeletedEvent event) {
    log.info("팔로우 삭제시 캐시 무효화 이벤트 처리 시작: followeeId = {}, followerId = {}", event.followeeId(),
      event.followerId());

    evictFollowSummaryCache();

    log.info("팔로우 삭제시 캐시 무효화 이벤트 처리 완료: followeeId = {}, followerId = {}", event.followeeId(),
      event.followerId());
  }

  // followSummary 캐시 키는 "조회 대상:조회자" 조합이라, 팔로우/언팔로우 1건이
  // 그 대상을 본 적 있는 모든 조회자의 엔트리를 stale하게 만든다. 이벤트에는 조회자 정보가
  // 없어 특정 키만 골라 지울 수 없으므로, 전체 엔트리를 지워 정합성을 보장한다.
  private void evictFollowSummaryCache() {
    Cache cache = cacheManager.getCache("followSummary");

    if (cache != null) {
      cache.clear();
      log.debug("followSummary 캐시 전체 무효화 완료");
    } else {
      log.debug("followSummary 캐시를 찾을 수 없습니다.");
    }
  }
}
