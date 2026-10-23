package com.gulabi.rides;
import com.gulabi.rides.config.RateLimitFilter; import org.junit.jupiter.api.Test; import org.springframework.mock.web.*; import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {
  RateLimitFilter filter = new RateLimitFilter();
  RateLimitFilterTest() { ReflectionTestUtils.setField(filter, "limit", 3); }

  int post(String path, String ip) throws Exception {
    var req = new MockHttpServletRequest("POST", path); req.setRequestURI(path); req.setRemoteAddr(ip);
    var res = new MockHttpServletResponse(); filter.doFilter(req, res, new MockFilterChain()); return res.getStatus();
  }

  @Test void blocksTheFourthLoginAttemptInAMinute() throws Exception {
    for (int i = 0; i < 3; i++) assertThat(post("/api/auth/login", "1.1.1.1")).isEqualTo(200);
    assertThat(post("/api/auth/login", "1.1.1.1")).isEqualTo(429);
  }
  @Test void otherClientsAreUnaffected() throws Exception {
    for (int i = 0; i < 4; i++) post("/api/auth/login", "2.2.2.2");
    assertThat(post("/api/auth/login", "3.3.3.3")).isEqualTo(200);
  }
  @Test void onlyAuthPostsAreLimited() throws Exception {
    for (int i = 0; i < 10; i++) assertThat(post("/api/rides", "4.4.4.4")).isEqualTo(200);
  }
}
