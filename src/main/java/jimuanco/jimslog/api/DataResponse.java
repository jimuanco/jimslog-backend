package jimuanco.jimslog.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Getter
public class DataResponse<T> {
    private T data;

    @JsonInclude(NON_DEFAULT)
    private int count;

    private DataResponse(T data) {
        this.data = data;
    }

    private DataResponse(T data, int count) {
        this.data = data;
        this.count = count;
    }

    public static <T> DataResponse<T> of(T data) {
        return new DataResponse<>(data);
    }

    public static <T> DataResponse<T> of(T data, int count) {
        return new DataResponse<>(data, count);
    }
}
