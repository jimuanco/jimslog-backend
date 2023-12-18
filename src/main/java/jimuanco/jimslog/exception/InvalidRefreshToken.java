package jimuanco.jimslog.exception;

public class InvalidRefreshToken extends MyBlogException {


    private static final String MESSAGE = "Refresh Token이 유효하지 않습니다.";

    public InvalidRefreshToken() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
