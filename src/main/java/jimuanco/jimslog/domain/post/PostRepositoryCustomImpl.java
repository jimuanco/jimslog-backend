package jimuanco.jimslog.domain.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static jimuanco.jimslog.domain.post.QPost.post;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public List<Post> getPostList(PostSearchServiceRequest request, List<Long> menuIdList) {
        return jpaQueryFactory.selectFrom(post)
                .where(getMenuId(request, menuIdList))
                .limit(request.getSize())
                .offset(request.getOffset())
                .orderBy(post.id.desc())
                .fetch();
    }

    private BooleanExpression getMenuId(PostSearchServiceRequest request, List<Long> menuIdList) {
        return (request.getMenuId() != 0) ? post.menu.id.in(menuIdList) : null;
    }
}
