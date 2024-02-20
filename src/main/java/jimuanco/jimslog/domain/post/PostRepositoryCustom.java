package jimuanco.jimslog.domain.post;

import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;

import java.util.List;

public interface PostRepositoryCustom {

    List<Post> getPostList(PostSearchServiceRequest postSearchServiceRequest, List<Long> menuIdList);
}
