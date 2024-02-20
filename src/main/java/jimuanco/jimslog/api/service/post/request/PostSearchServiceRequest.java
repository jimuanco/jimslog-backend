package jimuanco.jimslog.api.service.post.request;

import lombok.Builder;
import lombok.Getter;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Getter
public class PostSearchServiceRequest {

    private static final int MAX_SIZE = 2000;
    private int page;
    private int size;
    private int menuId;

    @Builder
    private PostSearchServiceRequest(int page, int size, int menuId) {
        this.page = page;
        this.size = size;
        this.menuId = menuId;
    }

    public long getOffset() {
        return (long) (max(1, page) - 1) * min(size, MAX_SIZE);
    }
}
