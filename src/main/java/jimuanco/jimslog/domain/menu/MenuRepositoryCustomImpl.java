package jimuanco.jimslog.domain.menu;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MenuRepositoryCustomImpl implements MenuRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Menu> findAllMenus() {
        QMenu parent = new QMenu("parent");
        QMenu child = new QMenu("child");

        return jpaQueryFactory.selectFrom(parent)
                .leftJoin(parent.children, child).fetchJoin()
                .where(parent.parent.isNull())
                .orderBy(parent.listOrder.asc(), child.listOrder.asc())
                .fetch();
    }
}
