package groupmember.dao;

import groupmember.vo.GroupMember;
import common.DbUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupMemberDao {
    private DbUtils dbUtils;
    private static GroupMemberDao gmdao = new GroupMemberDao();
    private GroupMemberDao(){
        dbUtils = DbUtils.getInstance();
    }

    public static GroupMemberDao getInstance(){return gmdao;}

//* 그룹장의 기능 *//
    // 멤버 추가 (해당 그룹에 해당 멤버가 없어야함)
    public void insert(Long Member_id, String username, Long Group_id) { // 그룹원의idx, Member_id, Group_id, Cumulative_time

        String sql = """
                insert into GroupMember(Member_id, Cumulative_time, Group_id) 
                values(?,
                        (select sum(sh.Cumulative_time) from `Member` m
                        join `Subject` s on s.Member_id = m.Id
                        join StudyHour sh on s.Id = sh.Subject_id
                        where m.Username = ?),
                        ?)
                """;

        try (Connection connection = dbUtils.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, Member_id);
            pstmt.setString(2, username); //
            pstmt.setLong(3, Group_id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    // 그룹멤버 추방 (내 그룹에 있는)
    public void delete(Long Member_id, Long Group_id) {

        String sql = "delete from GroupMember where Member_id =? and Group_id =?";

        try (Connection connection = dbUtils.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, Member_id); //
            pstmt.setLong(2, Group_id);

            int cnt = pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


//* 공통기능 *//
    // 전체 검색 (내 그룹원들만) -> 누적시간까지 포함!
    public List<GroupMember> selectAll(Long Group_id) {
        List<GroupMember> list = new ArrayList<>();

        String sql = """
                select gm.Id, gm.Member_id, gm.Group_id, Cumulative_time
                from GroupMember gm
                join `Group` g on g.id = gm.Group_id
                where gm.Group_id =?
                """;

        try (Connection connection = dbUtils.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, Group_id);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new GroupMember(
                        rs.getLong(1), // Id
                        rs.getLong(2), // Member_id
                        rs.getLong(3), // Group_name
                        rs.getLong(4) // Cumulative_time
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
    // 나와 같은 Group_id를 가진 그룹원 中 Member_id를 입력해서 멤버의 정보확인
    public GroupMember selectGroupMember (Long Member_id, Long Group_id){

        String sql = """
                select gm.Id, gm.Member_id, gm.Group_id, Cumulative_time
                from GroupMember gm
                join `Group` g on g.id = gm.Group_id
                where gm.Member_id =? and gm.Group_id =?
                """;
        try (Connection connection = dbUtils.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, Member_id);
            pstmt.setLong(2, Group_id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new GroupMember(
                        rs.getLong(1),
                        rs.getLong(2),
                        rs.getLong(3),
                        rs.getLong(4)
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    // 누적시간 합계sum구하기(GroupMember 1명에 대하여)
    public GroupMember getSumCumulativeTime(Long Group_id){

        String sql = """
                select gm.Id, gm.Member_id, gm.Group_id, sum(sh.Cumulative_time) from `Member` m
                        join GroupMember gm on m.Id = gm.Member_id
                        join `Subject` s on s.Member_id = m.Id
                        join StudyHour sh on s.Id = sh.Subject_id
                        where gm.Group_id = ?
                """;

        try (Connection connection = dbUtils.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, Group_id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new GroupMember(
                        rs.getLong(1), // Id
                        rs.getLong(2), // Member_id
                        rs.getLong(3), // Group_name
                        rs.getLong(4) // Cumulative_time
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // 누적시간 update
    public void updateCumulativeTime(GroupMember gm){

        String sql = """
                    update GroupMember set Cumulative_time = ?  
                    where Member_id = ?
                    """;

        try (Connection connection = dbUtils.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)){
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, gm.getCumulative_time());
            ps.setLong(2, gm.getMember_id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 내 그룹원인지 확인( Member_id -> Group_id)
    public boolean checkMyGroup(Long Member_id, Long Group_id) {

        String sql = """
                select * from GroupMember where Member_id =? and Group_id =? 
                """;
        try (Connection connection = dbUtils.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, Member_id);
            pstmt.setLong(2, Group_id);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                if (rs.getInt(3) == Group_id) return true;
                else return false;

            } else return false;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}