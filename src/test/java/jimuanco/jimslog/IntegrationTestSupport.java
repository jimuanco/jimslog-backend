package jimuanco.jimslog;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(AwsS3MockConfig.class)
@ActiveProfiles("test")
@SpringBootTest
public class IntegrationTestSupport {
}
