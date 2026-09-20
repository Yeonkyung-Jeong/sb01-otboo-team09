package com.part4.team09.otboo.module.domain.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.part4.team09.otboo.module.domain.follow.dto.FollowSummaryDto;
import com.part4.team09.otboo.module.domain.follow.repository.FollowRepository;
import com.part4.team09.otboo.module.domain.user.entity.User;
import com.part4.team09.otboo.module.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * 7da79e1 커밋에서 주석 처리됐던 followSummary 캐싱을 다시 켠 뒤,
 * 실제로 캐시 히트/미스, 그리고 팔로우 생성/삭제 시 캐시 무효화가 정상 동작하는지 확인하는 통합 테스트.
 *
 * 테스트 메서드 자체는 @Transactional을 붙이지 않는다.
 * FollowService.create()/deleteFollow()의 @Transactional이 실제로 커밋되어야
 * @TransactionalEventListener(phase = AFTER_COMMIT) 리스너가 실행되기 때문이다.
 * (테스트 클래스 전체에 @Transactional을 걸면 테스트 종료 시 롤백되어 커밋 자체가 발생하지 않아
 *  AFTER_COMMIT 리스너가 절대 트리거되지 않는다.)
 *
 * spring.cache.type=simple 로 오버라이드해서 실제 캐시 프록시(@Cacheable)가 동작하는
 * ConcurrentMapCacheManager를 사용하도록 한다. (test 프로파일 기본값은 cache.type=none 이라
 * @Cacheable이 사실상 no-op이 되어 검증이 불가능하다.)
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.cache.type=simple")
class FollowCachingIntegrationTest {

  @Autowired
  private FollowService followService;

  @Autowired
  private FollowRepository followRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CacheManager cacheManager;

  private User userA;
  private User userB;

  @BeforeEach
  void setUp() {
    userA = userRepository.save(User.createUser("cacheA@email.com", "캐시에이", "password"));
    userB = userRepository.save(User.createUser("cacheB@email.com", "캐시비", "password"));
  }

  @AfterEach
  void tearDown() {
    Cache followSummaryCache = cacheManager.getCache("followSummary");
    if (followSummaryCache != null) {
      followSummaryCache.clear();
    }
    followRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("getFollowSummary: 첫 호출은 캐시 미스(DB 조회), 두 번째 호출은 캐시 히트")
  void getFollowSummary_cacheHitOnSecondCall() {
    // given
    Cache followSummaryCache = cacheManager.getCache("followSummary");
    assertThat(followSummaryCache).as("followSummary 캐시 빈이 등록되어 있어야 함").isNotNull();

    String key = userA.getId().toString() + ":" + userB.getId().toString();
    assertThat(followSummaryCache.get(key)).as("첫 조회 전에는 캐시에 값이 없어야 함").isNull();

    // when: 첫 번째 호출 (캐시 미스 -> DB 조회 -> 캐시 저장)
    FollowSummaryDto firstCall = followService.getFollowSummary(userA.getId(), userB.getId());

    // then
    assertThat(firstCall).isNotNull();
    assertThat(followSummaryCache.get(key))
        .as("첫 호출 이후에는 @Cacheable에 의해 캐시에 값이 채워져 있어야 함")
        .isNotNull();

    // when: 캐시에 저장된 값을 미리 꺼내둔 뒤, 두 번째 호출 결과와 비교해서 캐시 히트를 검증
    Object cachedWrapper = followSummaryCache.get(key).get();

    FollowSummaryDto secondCall = followService.getFollowSummary(userA.getId(), userB.getId());

    // then: 캐시 히트라면 두 번째 호출 결과도 캐시에 저장된 값과 동일해야 하고,
    // 무엇보다 DB 재조회 없이 그대로 반환되므로 첫 호출과 필드값이 완전히 동일해야 한다.
    assertThat(secondCall).isEqualTo(firstCall);
    assertThat(cachedWrapper).isEqualTo(secondCall);
  }

  /**
   * FollowCacheEvictListener는 더 이상 존재하지 않는 currentUser 필드에 의존하지 않고,
   * 팔로우 생성/삭제 이벤트 발생 시 followSummary 캐시 전체를 clear()한다.
   * (키가 "조회 대상:조회자" 조합이라 이벤트만으로는 어떤 조회자의 엔트리가 stale해졌는지
   *  특정할 수 없기 때문에, 부분 evict 대신 전체 무효화로 정합성을 보장한다.)
   */
  @Test
  @DisplayName("팔로우 생성: create() 이후 캐시가 무효화되어 다음 조회는 최신 followerCount를 반영한다")
  void create_evictsCache_soNextCallReflectsFreshFollowerCount() {
    // given: 팔로우가 생기기 전 상태로 캐시를 미리 채워둔다 (followerCount = 0)
    FollowSummaryDto beforeFollow = followService.getFollowSummary(userA.getId(), userB.getId());
    assertThat(beforeFollow.followerCount()).isZero();

    String key = userA.getId().toString() + ":" + userB.getId().toString();
    Cache followSummaryCache = cacheManager.getCache("followSummary");
    assertThat(followSummaryCache.get(key)).isNotNull();

    // when: 팔로우 생성 — AFTER_COMMIT 리스너가 followSummary 캐시를 전체 무효화한다
    assertThatCode(() -> followService.create(userA.getId(), userB.getId()))
        .doesNotThrowAnyException();

    // then: DB에는 팔로우가 정상적으로 커밋되어 있다
    assertThat(followRepository.count()).isEqualTo(1);

    // then: 캐시 무효화 리스너가 정상 동작해서, 팔로우 생성 이전 값이 캐시에 남아있지 않다.
    assertThat(followSummaryCache.get(key))
        .as("캐시가 evict되어 이전 엔트리가 남아있지 않아야 함")
        .isNull();

    // getFollowSummary()를 다시 호출하면 캐시 미스로 DB를 재조회해서 최신 followerCount(1)를 반환한다.
    FollowSummaryDto afterFollow = followService.getFollowSummary(userA.getId(), userB.getId());
    assertThat(afterFollow.followerCount())
        .as("팔로우 생성 후에는 캐시가 무효화되어 최신 팔로워 수(1)를 반영해야 함")
        .isEqualTo(1);
  }
}
