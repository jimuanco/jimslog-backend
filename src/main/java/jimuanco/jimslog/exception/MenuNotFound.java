package jimuanco.jimslog.exception;

public class MenuNotFound extends MyBlogException {

    private static final String MESSAGE = "존재하지 않는 메뉴입니다.";

    public MenuNotFound() {
        super(MESSAGE);
    }

    @Override
    public int getStatusCode() {
        return 404;
    }
}
