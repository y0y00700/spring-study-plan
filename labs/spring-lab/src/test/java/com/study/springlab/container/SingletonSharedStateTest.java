package com.study.springlab.container;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SingletonSharedStateTest {

    class UserService{
        private void addName(List<String> names, String name) {
            // 전달받은 리스트에 name 추가
            names.add(name);
        }
    }


    @Test
    void 메서드_인자라도_같은_변경가능_객체를_공유하면_호출이_간섭한다() {
        // 공유할 ArrayList 생성
        ArrayList<String> sharedNames = new ArrayList<String>();
        UserService userService = new UserService() ;

        // 같은 UserService로 "A" 추가
        userService.addName(sharedNames,"A");
        // 같은 UserService로 "B" 추가
        userService.addName(sharedNames,"B");
        // List.of("A")를 기대하는 assertion 작성
        assertEquals(List.of("A","B"),sharedNames);
    }
}