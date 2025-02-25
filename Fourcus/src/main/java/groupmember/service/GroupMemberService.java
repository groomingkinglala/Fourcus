package groupmember.service;

import group.service.GroupService;
import group.vo.Group;
import groupmember.dao.GroupMemberDao;
import groupmember.vo.GroupMember;
import member.dao.MemberDao;
import member.vo.Member;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupMemberService {
    private GroupMemberDao dao;
    private MemberDao mdao;

    public GroupMemberService() {
        dao = GroupMemberDao.getInstance();
        mdao = MemberDao.getInstance();
    }
    // 기본적으로 Group_id를 갖고있는 상태
    /* 그룹장 기능 */

    // 1. 그룹멤버 추가(입 :username)
    public void addGroupMember(BufferedReader br) throws IOException {
        System.out.println("==== 그룹 멤버 추가 ====");

        while (true) {
            System.out.print("추가할 username: ");
            String username = br.readLine();
            Member m = mdao.select(1, username);
            if (m == null) {
                System.out.println("존재하지 않는 회원");
            } else {
                if (dao.checkMyGroup(m.getId(), GroupService.groupId)) {
                    System.out.printf("이미 %s에 소속된 멤버입니다", GroupService.groupId);
                } else {

                    dao.insert(m.getId(),username, GroupService.groupId);
                    System.out.println(username +"님 추가완료");
                }
                break;
            }
        }
    }

    // 2. 그룹멤버 추방 (username을 받아서)
    public void delGroupMember(BufferedReader br) throws IOException{
        System.out.println("==== 그룹 멤버 추방 ====");

        while (true) {
            System.out.print("추방할 username: ");
            String username = br.readLine();
            Member m = mdao.select(1, username);
            if (m == null) {
                System.out.println("존재하지 않는 회원");
            } else {
                if (dao.checkMyGroup(m.getId(), GroupService.groupId)) {
                    dao.delete(m.getId(), GroupService.groupId);
                    System.out.println(username+"님 추방완료");
                } else {
                    System.out.println("소속된 멤버가 아님");
                }
                break;
            }
        }
    }

    /* 공통 */
    // 내 그룹원 확인(전체)
    public void printMyGroupMember() {
        System.out.println("==== 내 그룹원 전체 확인 ====");
        GroupMember gm = dao.getSumCumulativeTime(GroupService.groupId); // 업데이트 된 누적시간(sum)을 가진 객체
        dao.updateCumulativeTime(gm); // GroupMember 테이블에 업데이트
        List<GroupMember> list = dao.selectAll(GroupService.groupId);
        printAll(list);
    }

    // 내 그룹원 확인(username 입력해서) -> 프로필 확인
    public void printMyGroupMemberProfile(BufferedReader br) throws IOException{
        System.out.println("==== 내 그룹원 프로필 보기 ====");
        System.out.print("확인할 username: ");
        String username = br.readLine();
        Member m = mdao.select(1, username);
        // 존재하지 않는 멤버
        if (m == null){
            System.out.println("존재하지 않는 회원");
        } else{
            GroupMember gm = dao.selectGroupMember(m.getId(), GroupService.groupId);
            if(dao.checkMyGroup(m.getId(), GroupService.groupId)){ // 내 그룹인지 확인
                GroupMember gm2 = dao.getSumCumulativeTime(GroupService.groupId); // 업데이트 된 누적시간(sum)을 가진 객체
                dao.updateCumulativeTime(gm2); // GroupMember 테이블에 업데이트
                System.out.println(gm2);
            } else{
                System.out.println("소속멤버가 아님");
            }
//            if(dao.checkMyGroup(m.getId(), GroupService.groupId) && gm != null){ // 내 그룹인지 확인
//                System.out.println(gm);
//            }
//

        }

    }
    public void printAll(List<GroupMember> list){
        for (GroupMember gm: list){
            System.out.println(gm);
        }
    }
}

