package jimuanco.jimslog;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(properties = "schedules.post-images.limit-time = 0")
public class IntegrationTestSupport {
}
