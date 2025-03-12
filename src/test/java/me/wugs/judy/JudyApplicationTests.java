package me.wugs.judy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = JudyApplication.class)
@ActiveProfiles("test")
class JudyApplicationTests {

  @Test
  void contextLoads() {}
}
