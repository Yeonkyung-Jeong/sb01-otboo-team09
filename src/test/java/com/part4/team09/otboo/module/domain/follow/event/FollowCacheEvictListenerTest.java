package com.part4.team09.otboo.module.domain.follow.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * FollowCacheEvictListener는 followSummary 캐시 키가 "조회 대상:조회자" 조합이라
 * 이벤트에 담긴 followeeId/followerId만으로는 어떤 조회자의 엔트리가 stale해졌는지 알 수 없다.
 * 그래서 특정 키를 골라 지우는 대신 캐시 전체를 clear()하여 정합성을 보장한다.
 */
@ExtendWith(MockitoExtension.class)
class FollowCacheEvictListenerTest {

  @Mock
  private CacheManager cacheManager;

  @Mock
  private Cache cache;

  @InjectMocks
  private FollowCacheEvictListener followCacheEvictListener;

  @Test
  @DisplayName("팔로우 생성: followSummary 캐시가 존재하면 전체 clear() 한다")
  void cacheEvictWhenFollowCreated_clearsCache() {
    // given
    when(cacheManager.getCache("followSummary")).thenReturn(cache);
    FollowCreatedEvent event = new FollowCreatedEvent(UUID.randomUUID(), UUID.randomUUID());

    // when
    followCacheEvictListener.cacheEvictWhenFollowCreated(event);

    // then
    verify(cache).clear();
  }

  @Test
  @DisplayName("팔로우 삭제: followSummary 캐시가 존재하면 전체 clear() 한다")
  void cacheEvictWhenFollowDeleted_clearsCache() {
    // given
    when(cacheManager.getCache("followSummary")).thenReturn(cache);
    FollowDeletedEvent event = new FollowDeletedEvent(UUID.randomUUID(), UUID.randomUUID());

    // when
    followCacheEvictListener.cacheEvictWhenFollowDeleted(event);

    // then
    verify(cache).clear();
  }

  @Test
  @DisplayName("팔로우 생성: followSummary 캐시가 없으면 아무 것도 하지 않고 NPE 없이 종료한다")
  void cacheEvictWhenFollowCreated_cacheMissing_doesNothing() {
    // given
    when(cacheManager.getCache("followSummary")).thenReturn(null);
    FollowCreatedEvent event = new FollowCreatedEvent(UUID.randomUUID(), UUID.randomUUID());

    // when
    followCacheEvictListener.cacheEvictWhenFollowCreated(event);

    // then
    verifyNoInteractions(cache);
  }
}
