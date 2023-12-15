package jimuanco.jimslog.api;

import lombok.Getter;

@Getter
public class DataResponse<T> {
    private T data;

    private DataResponse(T data) {
        this.data = data;
    }

    public static <T> DataResponse<T> of(T data) {
        return new DataResponse<>(data);
    }
}
