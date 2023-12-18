package jimuanco.jimslog.exception;

public class UserNotFound extends MyBlogException {

    private static final String MESSAGE = "존재하지 않는 사용자입니다.";

    public UserNotFound() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 404;
    }

}
