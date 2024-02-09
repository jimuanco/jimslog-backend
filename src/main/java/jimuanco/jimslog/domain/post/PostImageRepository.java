package jimuanco.jimslog.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    Optional<PostImage> findByFileName(String fileName);

    @Modifying()
    @Query("delete from PostImage p where p.fileName in :fileName")
    void deleteAllByFileNameInQuery(@Param("fileName") List<String> fileName);

    List<PostImage> findAllByPostId(Long postId);

    List<PostImage> findAllByPostIdIsNull();
}
