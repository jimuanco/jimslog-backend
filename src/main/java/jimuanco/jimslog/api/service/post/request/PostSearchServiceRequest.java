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

    @Builder
    public PostSearchServiceRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public long getOffset() {
        return (long) (max(1, page) - 1) * min(size, MAX_SIZE);
    }
}
