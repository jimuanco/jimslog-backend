package jimuanco.jimslog.api.service.post;

import com.amazonaws.services.s3.AmazonS3;
import jimuanco.jimslog.IntegrationTestSupport;
import jimuanco.jimslog.domain.post.PostImage;
import jimuanco.jimslog.domain.post.PostImageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class S3UploaderTest extends IntegrationTestSupport {

    @Autowired
    private S3Uploader s3Uploader;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @BeforeEach
    public void setUp() {
        amazonS3.createBucket(bucket);
    }

    @AfterEach
    public void tearDown() {
        amazonS3.deleteBucket(bucket);
    }

    @DisplayName("S3에 이미지파일을 업로드 한다.")
    @Test
    void upload() throws IOException {
        // given
        String originalFileName = "image.png";
        String dirName = "images";

        MockMultipartFile image = new MockMultipartFile("postImage",
                originalFileName,
                "image/png",
                "<<image.png>>".getBytes());
        // when
        String uploadImageUrl = s3Uploader.upload(image, dirName);

        // then
        assertThat(uploadImageUrl).contains(originalFileName);
        assertThat(uploadImageUrl).contains(dirName);

        List<PostImage> postImages = postImageRepository.findAll();
        assertThat(postImages).hasSize(1);
        assertThat(postImages.get(0).getPostId()).isNull();
    }
}