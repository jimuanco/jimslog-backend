package jimuanco.jimslog.domain.post;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static jimuanco.jimslog.domain.post.QPost.*;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public List<Post> getPostList(PostSearchServiceRequest request) {
        return jpaQueryFactory.selectFrom(post)
                .limit(request.getSize())
                .offset(request.getOffset())
                .orderBy(post.id.desc())
                .fetch();
    }
}
