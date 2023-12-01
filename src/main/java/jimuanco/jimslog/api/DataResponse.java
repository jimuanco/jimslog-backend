package jimuanco.jimslog.api;

import jimuanco.jimslog.api.service.post.response.PostResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DataResponse<T> {
    private T data;

    private DataResponse(T data) {
        this.data = data;
    }

    public static <T> DataResponse<T> ok(T data) {
        return new DataResponse<>(data);
    }
}
