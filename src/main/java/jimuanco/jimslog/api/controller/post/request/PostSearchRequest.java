package jimuanco.jimslog.api.controller.post.request;

import jimuanco.jimslog.api.service.post.request.PostSearchServiceRequest;
import lombok.Getter;

@Getter
public class PostSearchRequest {

    private int page;

    private int size;

    private void setPage(Integer page) { //todo 왜 ModelAttribute에 생성자 방식이 안먹히지?
        this.page = page == null ? 1 : page;
    }

    private void setSize(Integer size) {
        this.size = size == null ? 10 : size;
    }

    public PostSearchServiceRequest toServiceRequest() {
        return PostSearchServiceRequest.builder()
                .page(page)
                .size(size)
                .build();
    }

    public void changeToDefaultPage() {
        setPage(null);
    }

    public void changeToDefaultSize() {
        setSize(null);
    }

}
