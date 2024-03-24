package jimuanco.jimslog.domain.menu;

import jimuanco.jimslog.api.service.menu.request.MenuServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class MenuBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void createMenus(List<Menu> menus) {
        String mainMenuSql = "INSERT INTO menu(name, list_order) VALUES(?, ?)";

        jdbcTemplate.batchUpdate(
                mainMenuSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Menu menu = menus.get(i);
                        ps.setString(1, menu.getName());
                        ps.setInt(2, menu.getListOrder());
                    }

                    @Override
                    public int getBatchSize() {
                        return menus.size();
                    }
                }
        );

        Long firstPK = jdbcTemplate.queryForObject("SELECT last_insert_id()", Long.class);
        List<SubMenuDto> subMenus = menus.stream()
                .flatMap(menu -> menu.getChildren().stream()
                        .map(child -> SubMenuDto.builder()
                                .name(child.getName())
                                .listOrder(child.getListOrder())
                                .parentId(firstPK + menus.indexOf(menu))
                                .build()
                        )
                )
                .collect(Collectors.toList());

        String subMenuSql = "INSERT INTO menu(name, list_order, parent_id) VALUES(?, ?, ?)";

        jdbcTemplate.batchUpdate(
                subMenuSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        SubMenuDto subMenuDto = subMenus.get(i);
                        ps.setString(1, subMenuDto.getName());
                        ps.setInt(2, subMenuDto.getListOrder());
                        ps.setLong(3, subMenuDto.getParentId());
                    }

                    @Override
                    public int getBatchSize() {
                        return subMenus.size();
                    }
                }
        );
    }

    @Transactional
    public void updateMenus(List<MenuServiceRequest> menusToUpdate) {
        String mainMenuSql = "UPDATE menu SET name = ?, list_order = ? WHERE id = ?";

        jdbcTemplate.batchUpdate(
                mainMenuSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MenuServiceRequest menuToUpdate = menusToUpdate.get(i);
                        ps.setString(1, menuToUpdate.getName());
                        ps.setInt(2, menuToUpdate.getListOrder());
                        ps.setLong(3, menuToUpdate.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return menusToUpdate.size();
                    }
                }
        );
    }

    @Getter
    static class SubMenuDto {
        private String name;
        private int listOrder;
        private Long parentId;

        @Builder
        public SubMenuDto(String name, int listOrder, Long parentId) {
            this.name = name;
            this.listOrder = listOrder;
            this.parentId = parentId;
        }
    }
}
