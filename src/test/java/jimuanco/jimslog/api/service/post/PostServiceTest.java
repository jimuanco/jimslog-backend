package jimuanco.jimslog.api.service.post;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import jakarta.persistence.EntityManager;
import jimuanco.jimslog.IntegrationTestSupport;
import jimuanco.jimslog.api.service.post.request.PostCreateServiceRequest;
import jimuanco.jimslog.api.service.post.request.PostEditServiceRequest;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import jimuanco.jimslog.api.service.post.response.PostResponse;
import jimuanco.jimslog.domain.menu.Menu;
import jimuanco.jimslog.domain.menu.MenuRepository;
import jimuanco.jimslog.domain.post.Post;
import jimuanco.jimslog.domain.post.PostImage;
import jimuanco.jimslog.domain.post.PostImageRepository;
import jimuanco.jimslog.domain.post.PostRepository;
import jimuanco.jimslog.exception.MenuNotFound;
import jimuanco.jimslog.exception.PostNotFound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
class PostServiceTest extends IntegrationTestSupport {

    @Autowired
    private EntityManager em;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${jimslog.s3.url}")
    private String s3Url;

    @Value("${jimslog.s3.local}")
    private String localS3;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private S3Uploader s3Uploader;

    @Autowired
    private PostImageRepository postImageRepository;

//    @AfterAll
//    static void tearDown(@Autowired S3Mock s3Mock) {
//        s3Mock.stop();
//    }

    @DisplayName("새로운 글을 등록할때 munuId를 넣지 않으면 메뉴가 지정되지 않는다.")
    @Test
    void createPostWithoutMenuId() {
        // given
        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .uploadImageUrls(new ArrayList<>())
                .deleteImageUrls(new ArrayList<>())
                .build();

        // when
        postService.createPost(request);
        
        // then
        List<Post> posts = postRepository.findAll();
        assertThat(posts).hasSize(1)
                .extracting("title", "content", "menu")
                .contains(
                        tuple("글제목 입니다.", "글내용 입니다.", null)
                );
    }

    @DisplayName("새로운 글을 등록할때 munuId를 넣으면 메뉴가 지정된다.")
    @Test
    void createPostWithMenuId() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        menuRepository.save(mainMenu1);

        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .menuId(Math.toIntExact(mainMenu1.getId()))
                .uploadImageUrls(new ArrayList<>())
                .deleteImageUrls(new ArrayList<>())
                .build();

        // when
        postService.createPost(request);

        // then
        List<Post> posts = postRepository.findAll();
        assertThat(posts).hasSize(1)
                .extracting("title", "content", "menu")
                .contains(
                        tuple("글제목 입니다.", "글내용 입니다.", mainMenu1)
                );
    }

    @DisplayName("존재하지 않는 munuId로 새로운 글을 등록하면 예외가 발생한다.")
    @Test
    void createPostWithNonExistingMenuId() {
        // given
        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .menuId(1)
                .build();

        // when // then
        List<Post> posts = postRepository.findAll();
        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(MenuNotFound.class)
                .hasMessage("존재하지 않는 메뉴입니다.");
    }

    @DisplayName("새로운 글을 등록할때 S3와 DB에는 최종 등록하는 글의 이미지들만 남아있다.")
    @Test
    void createPostWithImages() throws IOException {
        // given
        MockMultipartFile image1 = new MockMultipartFile("postImage",
                "image1.png",
                "image/png",
                "<<image1.png>>".getBytes());

        MockMultipartFile image2 = new MockMultipartFile("postImage",
                "image2.png",
                "image/png",
                "<<image2.png>>".getBytes());

        MockMultipartFile image3 = new MockMultipartFile("postImage",
                "image3.png",
                "image/png",
                "<<image3.png>>".getBytes());

        MockMultipartFile image4 = new MockMultipartFile("postImage",
                "image4.png",
                "image/png",
                "<<image4.png>>".getBytes());

        MockMultipartFile image5 = new MockMultipartFile("postImage",
                "image5.png",
                "image/png",
                "<<image5.png>>".getBytes());

        String uploadImageUrl1 = s3Uploader.upload(image1, "images").replace(localS3, s3Url);
        String uploadImageUrl2 = s3Uploader.upload(image2, "images").replace(localS3, s3Url);
        String uploadImageUrl3 = s3Uploader.upload(image3, "images").replace(localS3, s3Url);
        String uploadImageUrl4 = s3Uploader.upload(image4, "images").replace(localS3, s3Url);
        String uploadImageUrl5 = s3Uploader.upload(image5, "images").replace(localS3, s3Url);

        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .uploadImageUrls(List.of(uploadImageUrl1, uploadImageUrl3, uploadImageUrl5))
                .deleteImageUrls(List.of(uploadImageUrl2, uploadImageUrl4))
                .build();

        // when
        postService.createPost(request);

        // then
        List<PostImage> postImages = postImageRepository.findAll();
        Post post = postRepository.findAll().get(0);

        String fileName1 = uploadImageUrl1.substring(s3Url.length() + 1);
        String fileName2 = uploadImageUrl2.substring(s3Url.length() + 1);
        String fileName3 = uploadImageUrl3.substring(s3Url.length() + 1);
        String fileName4 = uploadImageUrl4.substring(s3Url.length() + 1);
        String fileName5 = uploadImageUrl5.substring(s3Url.length() + 1);

        assertThat(amazonS3.getObject(bucket, fileName1).getKey()).isEqualTo(fileName1);
        assertThat(amazonS3.getObject(bucket, fileName3).getKey()).isEqualTo(fileName3);
        assertThat(amazonS3.getObject(bucket, fileName5).getKey()).isEqualTo(fileName5);

        assertThatThrownBy(() -> amazonS3.getObject(bucket, fileName2))
                .isInstanceOf(AmazonS3Exception.class);
        assertThatThrownBy(() -> amazonS3.getObject(bucket, fileName4))
                .isInstanceOf(AmazonS3Exception.class);

        assertThat(postImages).hasSize(3)
                .extracting("postId", "fileName")
                .contains(
                        tuple(post.getId(), fileName1),
                        tuple(post.getId(), fileName3),
                        tuple(post.getId(), fileName5)
                );
    }

    @DisplayName("새로운 글을 등록할때 uploadImageUrl이 정확하면 Post Image에 PostId를 업데이트 한다.")
    @Test
    void createPostWithExactUploadImageUrl() {
        // given
        String dirName = "images";

        PostImage image1 = PostImage.builder()
                .fileName(dirName + "/image1")
                .build();
        postImageRepository.save(image1);

        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다. ![image](" + s3Url +  "/" + dirName + "/image1)")
                .uploadImageUrls(List.of(s3Url + "/" + dirName + "/image1"))
                .deleteImageUrls(new ArrayList<>())
                .build();

        // when
        postService.createPost(request);

        // then
        PostImage postImage = postImageRepository.findAll().get(0);
        Post post = postRepository.findAll().get(0);

        assertThat(postImage.getPostId()).isEqualTo(post.getId());
    }

    @DisplayName("새로운 글을 등록할때 " +
            "uploadImageUrl에서 S3 Url과 Directory Name이 정확하지 않으면 Post Image에 PostId를 업데이트하지 않는다.")
    @Test
    void createPostWithInexactUploadImageUrl() {
        // given
        String dirName = "images";

        PostImage image1 = PostImage.builder()
                .fileName(dirName + "/image1")
                .build();
        postImageRepository.save(image1);

        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다. ![image](" + s3Url +  "a/" + dirName + "/image1)")
                .uploadImageUrls(List.of(s3Url + "a/" + dirName + "/image1"))
                .deleteImageUrls(new ArrayList<>())
                .build();

        // when
        postService.createPost(request);

        // then
        PostImage postImage = postImageRepository.findAll().get(0);

        assertThat(postImage.getPostId()).isNull();
    }

    @DisplayName("새로운 글을 등록할때 " +
            "uploadImageUrl에서 S3 Url만 입력하면 Post Image에 PostId를 업데이트하지 않는다.")
    @Test
    void createPostWithOnlyS3UrlInUploadImageUrl() {
        // given
        String dirName = "images";

        PostImage image1 = PostImage.builder()
                .fileName(dirName + "/image1")
                .build();
        postImageRepository.save(image1);

        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다. ![image](" + s3Url + ")")
                .uploadImageUrls(List.of(s3Url))
                .deleteImageUrls(new ArrayList<>())
                .build();

        // when
        postService.createPost(request);

        // then
        PostImage postImage = postImageRepository.findAll().get(0);

        assertThat(postImage.getPostId()).isNull();
    }

    @DisplayName("새로운 글을 등록할때 " +
            "uploadImageUrl에서 Image 파일 이름이 정확하지 않으면 Post Image에 PostId를 업데이트하지 않는다.")
    @Test
    void createPostWithInexactUploadImageName() {
        // given
        String dirName = "images";

        PostImage image1 = PostImage.builder()
                .fileName(dirName + "/image1")
                .build();
        postImageRepository.save(image1);

        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다. ![image](" + s3Url +  "/" + dirName + "/image2)")
                .uploadImageUrls(List.of(s3Url + "/" + dirName + "/image2"))
                .deleteImageUrls(new ArrayList<>())
                .build();

        // when
        postService.createPost(request);

        // then
        PostImage postImage = postImageRepository.findAll().get(0);

        assertThat(postImage.getPostId()).isNull();
    }

    @DisplayName("새로운 글을 등록할때 deleteImageUrl이 정확하면 Post Image를 S3와 Db에서 삭제한다.")
    @Test
    void createPostWithExactDeleteImageUrl() throws IOException {
        // given
        MockMultipartFile image1 = new MockMultipartFile("postImage",
                "image1.png",
                "image/png",
                "<<image1.png>>".getBytes());

        String dirName = "images";
        String uploadImageUrl1 = s3Uploader.upload(image1, dirName).replace(localS3, s3Url);
        String fileName1 = uploadImageUrl1.substring(s3Url.length() + 1);

        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .uploadImageUrls(new ArrayList<>())
                .deleteImageUrls(List.of(uploadImageUrl1))
                .build();

        // when
        postService.createPost(request);

        // then
        List<PostImage> postImages = postImageRepository.findAll();

        assertThatThrownBy(() -> amazonS3.getObject(bucket, fileName1))
                .isInstanceOf(AmazonS3Exception.class);
        assertThat(postImages).hasSize(0);
    }

    @DisplayName("새로운 글을 등록할때 " +
            "deleteImageUrl에서 S3 Url과 Directory Name이 정확하지 않으면 Post Image를 S3와 Db에서 삭제하지 않는다.")
    @Test
    void createPostWithInexactDeleteImageUrl() throws IOException {
        // given
        MockMultipartFile image1 = new MockMultipartFile("postImage",
                "image1.png",
                "image/png",
                "<<image1.png>>".getBytes());

        String dirName = "images";
        String uploadImageUrl1 = s3Uploader.upload(image1, dirName).replace(localS3, s3Url);
        String fileName1 = uploadImageUrl1.substring(s3Url.length() + 1);

        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .uploadImageUrls(new ArrayList<>())
                .deleteImageUrls(List.of("a" + uploadImageUrl1))
                .build();

        // when
        postService.createPost(request);

        // then
        List<PostImage> postImages = postImageRepository.findAll();

        assertThat(amazonS3.getObject(bucket, fileName1).getKey()).isEqualTo(fileName1);
        assertThat(postImages).hasSize(1);
    }

    @DisplayName("새로운 글을 등록할때 " +
            "deleteImageUrl에서 S3 Url만 입력하면 Post Image를 S3와 Db에서 삭제하지 않는다.")
    @Test
    void createPostWithOnlyS3UrlInDeleteImageUrl() throws IOException {
        // given
        MockMultipartFile image1 = new MockMultipartFile("postImage",
                "image1.png",
                "image/png",
                "<<image1.png>>".getBytes());

        String dirName = "images";
        String uploadImageUrl1 = s3Uploader.upload(image1, dirName).replace(localS3, s3Url);
        String fileName1 = uploadImageUrl1.substring(s3Url.length() + 1);

        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .uploadImageUrls(new ArrayList<>())
                .deleteImageUrls(List.of(s3Url))
                .build();

        // when
        postService.createPost(request);

        // then
        List<PostImage> postImages = postImageRepository.findAll();

        assertThat(amazonS3.getObject(bucket, fileName1).getKey()).isEqualTo(fileName1);
        assertThat(postImages).hasSize(1);
    }

    @DisplayName("새로운 글을 등록할때 " +
            "deleteImageUrl에서 Image 파일 이름이 정확하지 않으면 Post Image를 S3와 Db에서 삭제하지 않는다.")
    @Test
    void createPostWithInexactDeleteImageName() throws IOException {
        // given
        MockMultipartFile image1 = new MockMultipartFile("postImage",
                "image1.png",
                "image/png",
                "<<image1.png>>".getBytes());

        String dirName = "images";
        String uploadImageUrl1 = s3Uploader.upload(image1, dirName).replace(localS3, s3Url);
        String fileName1 = uploadImageUrl1.substring(s3Url.length() + 1);

        PostCreateServiceRequest request = PostCreateServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .uploadImageUrls(new ArrayList<>())
                .deleteImageUrls(List.of(s3Url + "/" + dirName + "/a"))
                .build();

        // when
        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(AmazonS3Exception.class);

        // then
        List<PostImage> postImages = postImageRepository.findAll();

        assertThat(amazonS3.getObject(bucket, fileName1).getKey()).isEqualTo(fileName1);
        assertThat(postImages).hasSize(1);
    }

    @DisplayName("글 1개 조회한다.")
    @Test
    void getPost() {
        // given
        Post post = Post.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .build();
        postRepository.save(post);

        // when
        PostResponse response = postService.getPost(post.getId());

        // then
        assertThat(response)
                .extracting("title", "content", "createdDateTime")
                .contains("글제목 입니다.", "글내용 입니다.", post.getCreatedDateTime());
    }

    @DisplayName("존재하지 않는 글ID로 글을 조회하면 예외가 발생한다.")
    @Test
    void getPostByNonExistingId() {
        // given
        Post post = Post.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .build();
        postRepository.save(post);

        // when // then
        assertThatThrownBy(() -> postService.getPost(post.getId() + 1))
                .isInstanceOf(PostNotFound.class)
                .hasMessage("존재하지 않는 글입니다.");
    }

    @DisplayName("서브 메뉴에 속한 글들을 Id 내림차순으로 조회한다.")
    @Test
    void getSubMenuPostList() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        Post post1 = Post.builder()
                .title("글제목1")
                .content("글내용1")
                .menu(subMenu1_1)
                .build();
        Post post2 = Post.builder()
                .title("글제목2")
                .content("글내용2")
                .menu(subMenu1_1)
                .build();
        Post post3 = Post.builder()
                .title("글제목3")
                .content("글내용3")
                .menu(subMenu1_1)
                .build();

        menuRepository.save(mainMenu1);
        postRepository.saveAll(List.of(post1, post2, post3));

        PostSearchServiceRequest request = PostSearchServiceRequest.builder()
                .page(1)
                .size(3)
                .menuId(Math.toIntExact(subMenu1_1.getId()))
                .build();

        // when
        List<PostResponse> postList = postService.getPostList(request);

        // then
        assertThat(postList).hasSize(3)
                .extracting("title", "content", "createdDateTime")
                .containsExactly(
                        tuple("글제목3", "글내용3", post3.getCreatedDateTime()),
                        tuple("글제목2", "글내용2", post2.getCreatedDateTime()),
                        tuple("글제목1", "글내용1", post1.getCreatedDateTime())
                );
    }

    @DisplayName("메인 메뉴에 속한 글들을 Id 내림차순으로 조회한다.")
    @Test
    void getMAinMenuPostList() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu subMenu1_2 = Menu.builder()
                .name("1-2. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1, subMenu1_2))
                .build();

        Post post1 = Post.builder()
                .title("글제목1")
                .content("글내용1")
                .menu(subMenu1_1)
                .build();
        Post post2 = Post.builder()
                .title("글제목2")
                .content("글내용2")
                .menu(subMenu1_1)
                .build();
        Post post3 = Post.builder()
                .title("글제목3")
                .content("글내용3")
                .menu(subMenu1_2)
                .build();

        menuRepository.save(mainMenu1);
        postRepository.saveAll(List.of(post1, post2, post3));

        PostSearchServiceRequest request = PostSearchServiceRequest.builder()
                .page(1)
                .size(3)
                .menuId(Math.toIntExact(mainMenu1.getId()))
                .build();

        // when
        List<PostResponse> postList = postService.getPostList(request);

        // then
        assertThat(postList).hasSize(3)
                .extracting("title", "content", "createdDateTime")
                .containsExactly(
                        tuple("글제목3", "글내용3", post3.getCreatedDateTime()),
                        tuple("글제목2", "글내용2", post2.getCreatedDateTime()),
                        tuple("글제목1", "글내용1", post1.getCreatedDateTime())
                );
    }

    @DisplayName("글 제목을 수정한다.")
    @Test
    void editPostTitle() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        menuRepository.save(mainMenu1);

        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .menu(subMenu1_1)
                .build();
        postRepository.save(post);

        em.flush(); // todo @Transactional 대신 tearDown() 쓸지 고민
        em.clear();

        PostEditServiceRequest request = PostEditServiceRequest.builder()
                .title("글제목 수정")
                .content("글내용")
                .menuId(Math.toIntExact(subMenu1_1.getId()))
                .uploadImageUrls(new ArrayList<>())
                .deleteImageUrls(new ArrayList<>())
                .build();

        // when
        postService.editPost(post.getId(), request);

        em.flush();
        em.clear();

        // then
        Post editedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));
        assertThat(editedPost.getTitle()).isEqualTo("글제목 수정");
        assertThat(editedPost.getContent()).isEqualTo("글내용");
        assertThat((long) editedPost.getMenu().getId()).isEqualTo(subMenu1_1.getId());
    }

    @DisplayName("글 내용을 수정한다.")
    @Test
    void editPostContent() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        menuRepository.save(mainMenu1);

        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .menu(subMenu1_1)
                .build();
        postRepository.save(post);

        em.flush(); // todo @Transactional 대신 tearDown() 쓸지 고민
        em.clear();

        PostEditServiceRequest request = PostEditServiceRequest.builder()
                .title("글제목")
                .content("글내용 수정")
                .menuId(Math.toIntExact(subMenu1_1.getId()))
                .uploadImageUrls(new ArrayList<>())
                .deleteImageUrls(new ArrayList<>())
                .build();

        // when
        postService.editPost(post.getId(), request);

        em.flush();
        em.clear();

        // then
        Post editedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));
        assertThat(editedPost.getTitle()).isEqualTo("글제목");
        assertThat(editedPost.getContent()).isEqualTo("글내용 수정");
        assertThat((long) editedPost.getMenu().getId()).isEqualTo(subMenu1_1.getId());
    }

    @DisplayName("글 메뉴를 수정한다.")
    @Test
    void editPostMenu() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu subMenu1_2 = Menu.builder()
                .name("1-2. 메뉴")
                .listOrder(2)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1, subMenu1_2))
                .build();

        menuRepository.save(mainMenu1);

        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .menu(subMenu1_1)
                .build();
        postRepository.save(post);

        em.flush(); // todo @Transactional 대신 tearDown() 쓸지 고민
        em.clear();

        PostEditServiceRequest request = PostEditServiceRequest.builder()
                .title("글제목")
                .content("글내용")
                .menuId(Math.toIntExact(subMenu1_2.getId()))
                .uploadImageUrls(new ArrayList<>())
                .deleteImageUrls(new ArrayList<>())
                .build();

        // when
        postService.editPost(post.getId(), request);

        em.flush();
        em.clear();

        // then
        Post editedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));
        assertThat(editedPost.getTitle()).isEqualTo("글제목");
        assertThat(editedPost.getContent()).isEqualTo("글내용");
        assertThat((long) editedPost.getMenu().getId()).isEqualTo(subMenu1_2.getId());
    }

    @DisplayName("존재하지 않는 글ID의 글을 수정할 시 예외가 발생한다.")
    @Test
    void editPostByNonExistingMenuId() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        menuRepository.save(mainMenu1);

        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .menu(subMenu1_1)
                .build();
        postRepository.save(post);

        em.flush(); // todo @Transactional 대신 tearDown() 쓸지 고민
        em.clear();

        PostEditServiceRequest request = PostEditServiceRequest.builder()
                .title("글제목 수정")
                .content("글내용 수정")
                .menuId(Math.toIntExact(subMenu1_1.getId()))
                .build();

        // when // then
        assertThatThrownBy(() -> postService.editPost(post.getId() + 1, request))
                .isInstanceOf(PostNotFound.class)
                .hasMessage("존재하지 않는 글입니다.");

        em.flush();
        em.clear();

        Post editedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));
        assertThat(editedPost.getTitle()).isEqualTo("글제목");
        assertThat(editedPost.getContent()).isEqualTo("글내용");
        assertThat((long) editedPost.getMenu().getId()).isEqualTo(subMenu1_1.getId());
    }

    @DisplayName("존재하지 않는 메뉴ID로 글을 수정할 시 예외가 발생한다.")
    @Test
    void editPostByNonExistingId() {
        // given
        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        menuRepository.save(mainMenu1);

        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .menu(subMenu1_1)
                .build();
        postRepository.save(post);

        em.flush(); // todo @Transactional 대신 tearDown() 쓸지 고민
        em.clear();

        PostEditServiceRequest request = PostEditServiceRequest.builder()
                .title("글제목 수정")
                .content("글내용 수정")
                .menuId(Math.toIntExact(subMenu1_1.getId() + 1))
                .build();

        // when // then
        assertThatThrownBy(() -> postService.editPost(post.getId(), request))
                .isInstanceOf(MenuNotFound.class)
                .hasMessage("존재하지 않는 메뉴입니다.");

        em.flush();
        em.clear();

        Post editedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));
        assertThat(editedPost.getTitle()).isEqualTo("글제목");
        assertThat(editedPost.getContent()).isEqualTo("글내용");
        assertThat((long) editedPost.getMenu().getId()).isEqualTo(subMenu1_1.getId());
    }

    @DisplayName("글을 수정할때 S3와 DB에는 최종 등록하는 글의 이미지들만 남아있다.")
    @Test
    void editPostWithImages() throws IOException {
        // given
        MockMultipartFile image1 = new MockMultipartFile("postImage",
                "image1.png",
                "image/png",
                "<<image1.png>>".getBytes());

        MockMultipartFile image2 = new MockMultipartFile("postImage",
                "image2.png",
                "image/png",
                "<<image2.png>>".getBytes());

        MockMultipartFile image3 = new MockMultipartFile("postImage",
                "image3.png",
                "image/png",
                "<<image3.png>>".getBytes());

        String uploadImageUrl1 = s3Uploader.upload(image1, "images").replace(localS3, s3Url);
        String uploadImageUrl2 = s3Uploader.upload(image2, "images").replace(localS3, s3Url);
        String uploadImageUrl3 = s3Uploader.upload(image3, "images").replace(localS3, s3Url);

        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        menuRepository.save(mainMenu1);

        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .menu(subMenu1_1)
                .build();
        postRepository.save(post);

        MockMultipartFile image4 = new MockMultipartFile("postImage",
                "image4.png",
                "image/png",
                "<<image4.png>>".getBytes());

        MockMultipartFile image5 = new MockMultipartFile("postImage",
                "image5.png",
                "image/png",
                "<<image5.png>>".getBytes());

        String uploadImageUrl4 = s3Uploader.upload(image4, "images").replace(localS3, s3Url);
        String uploadImageUrl5 = s3Uploader.upload(image5, "images").replace(localS3, s3Url);

        PostEditServiceRequest request = PostEditServiceRequest.builder()
                .title("글제목 입니다.")
                .content("글내용 입니다.")
                .menuId(Math.toIntExact(subMenu1_1.getId()))
                .uploadImageUrls(List.of(uploadImageUrl5))
                .deleteImageUrls(List.of(uploadImageUrl2, uploadImageUrl4))
                .build();

        // when
        postService.editPost(post.getId(), request);

        // then
        List<PostImage> postImages = postImageRepository.findAll();
        Post EditedPost = postRepository.findAll().get(0);

        String fileName1 = uploadImageUrl1.substring(s3Url.length() + 1);
        String fileName2 = uploadImageUrl2.substring(s3Url.length() + 1);
        String fileName3 = uploadImageUrl3.substring(s3Url.length() + 1);
        String fileName4 = uploadImageUrl4.substring(s3Url.length() + 1);
        String fileName5 = uploadImageUrl5.substring(s3Url.length() + 1);

        assertThat(amazonS3.getObject(bucket, fileName1).getKey()).isEqualTo(fileName1);
        assertThat(amazonS3.getObject(bucket, fileName3).getKey()).isEqualTo(fileName3);
        assertThat(amazonS3.getObject(bucket, fileName5).getKey()).isEqualTo(fileName5);

        assertThatThrownBy(() -> amazonS3.getObject(bucket, fileName2))
                .isInstanceOf(AmazonS3Exception.class);
        assertThatThrownBy(() -> amazonS3.getObject(bucket, fileName4))
                .isInstanceOf(AmazonS3Exception.class);

        assertThat(postImages).hasSize(3)
                .extracting("postId", "fileName")
                .contains(
                        tuple(null, fileName1),
                        tuple(null, fileName3),
                        tuple(EditedPost.getId(), fileName5)
                );
    }

    @DisplayName("글을 삭제한다.")
    @Test
    void deletePost() {
        // given
        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .build();
        postRepository.save(post);

        // when
        postService.deletePost(post.getId());

        // then
        assertThat(postRepository.count()).isEqualTo(0);
    }

    @DisplayName("글을 삭제할때 글에 포함된 이미지는 S3와 DB에서 삭제된다.")
    @Test
    void deletePostWithImages() throws IOException {
        // given
        MockMultipartFile image1 = new MockMultipartFile("postImage",
                "image1.png",
                "image/png",
                "<<image1.png>>".getBytes());

        MockMultipartFile image2 = new MockMultipartFile("postImage",
                "image2.png",
                "image/png",
                "<<image2.png>>".getBytes());

        String uploadImageUrl1 = s3Uploader.upload(image1, "images").replace(localS3, s3Url);
        String uploadImageUrl2 = s3Uploader.upload(image2, "images").replace(localS3, s3Url);

        Menu subMenu1_1 = Menu.builder()
                .name("1-1. 메뉴")
                .listOrder(1)
                .children(new ArrayList<>())
                .build();

        Menu mainMenu1 = Menu.builder()
                .name("1. 메뉴")
                .listOrder(1)
                .children(List.of(subMenu1_1))
                .build();

        menuRepository.save(mainMenu1);

        Post post = Post.builder()
                .title("글제목")
                .content("글내용")
                .menu(subMenu1_1)
                .build();
        postRepository.save(post);

        postImageRepository.findAll().stream()
                .forEach(postImage -> postImage.updatePostId(post.getId()));

        // when
        postService.deletePost(post.getId());

        // then
        assertThat(postImageRepository.findAll()).hasSize(0);

        String fileName1 = uploadImageUrl1.substring(s3Url.length() + 1);
        String fileName2 = uploadImageUrl2.substring(s3Url.length() + 1);

        assertThatThrownBy(() -> amazonS3.getObject(bucket, fileName1))
                .isInstanceOf(AmazonS3Exception.class);
        assertThatThrownBy(() -> amazonS3.getObject(bucket, fileName2))
                .isInstanceOf(AmazonS3Exception.class);
    }
}