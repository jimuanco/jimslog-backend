package jimuanco.jimslog;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(AwsS3MockConfig.class)
@ActiveProfiles("test")
@SpringBootTest(properties = "schedules.post-images.limit-time = 0")
public class IntegrationTestSupport {
}
