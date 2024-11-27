package stressTest.rest.controller;

import java.util.ArrayList;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiControllers {

    ArrayList<User> list = new ArrayList<User>();

    @GetMapping(value = "/")
    public String getPage() {

        return "Populate the array!";
    }

    @GetMapping(value = "/1")
    public String tryThis() {
        return "This is a number 1";
    }

    @PostMapping(value = "/createUser")
    public String createUser(@RequestBody String name) {
        list.add(new User(name));

        return name;
    }

    @GetMapping(value = "/getUser")
    public String getUser() {
        return list.get(0).getName();
    }

    // Stress Testing
    @PostMapping(value = "/post")
    public int postRoute() {
        return 1;
    }

    @GetMapping(value = "/get")
    public int getRoute() {
        return 2;
    }

    @PutMapping(value = "/put")
    public int putRoute() {
        return 3;
    }

    @DeleteMapping(value = "/delete")
    public int deleteRoute() {
        return 4;
    }

    @PostMapping(value = "/post1")
    public int postRoute1() {
        return 1;
    }

    @GetMapping(value = "/get1")
    public int getRoute1() {
        return 2;
    }

    @PutMapping(value = "/put1")
    public int putRoute1() {
        return 3;
    }

    @DeleteMapping(value = "/delete1")
    public int deleteRoute1() {
        return 4;
    }

    @PostMapping(value = "/post2")
    public int postRoute2() {
        return 1;
    }

    @GetMapping(value = "/get2")
    public int getRoute2() {
        return 2;
    }

    @PutMapping(value = "/put2")
    public int putRoute2() {
        return 3;
    }

    @DeleteMapping(value = "/delete2")
    public int deleteRoute2() {
        return 4;
    }

    @PostMapping(value = "/post3")
    public int postRoute3() {
        return 1;
    }

    @GetMapping(value = "/get3")
    public int getRoute3() {
        return 2;
    }

    @PutMapping(value = "/put3")
    public int putRoute3() {
        return 3;
    }

    @DeleteMapping(value = "/delete3")
    public int deleteRoute3() {
        return 4;
    }

    @PostMapping(value = "/post4")
    public int postRoute4() {
        return 1;
    }

    @GetMapping(value = "/get4")
    public int getRoute4() {
        return 2;
    }

    @PutMapping(value = "/put4")
    public int putRoute4() {
        return 3;
    }

    @DeleteMapping(value = "/delete4")
    public int deleteRoute4() {
        return 4;
    }

    @PostMapping(value = "/post5")
    public int postRoute5() {
        return 1;
    }

    @GetMapping(value = "/get5")
    public int getRoute5() {
        return 2;
    }

    @PutMapping(value = "/put5")
    public int putRoute5() {
        return 3;
    }

    @DeleteMapping(value = "/delete5")
    public int deleteRoute5() {
        return 4;
    }

    @PostMapping(value = "/post6")
    public int postRoute6() {
        return 1;
    }

    @GetMapping(value = "/get6")
    public int getRoute6() {
        return 2;
    }

    @PutMapping(value = "/put6")
    public int putRoute6() {
        return 3;
    }

    @DeleteMapping(value = "/delete6")
    public int deleteRoute6() {
        return 4;
    }

    @PostMapping(value = "/post7")
    public int postRoute7() {
        return 1;
    }

    @GetMapping(value = "/get7")
    public int getRoute7() {
        return 2;
    }

    @PutMapping(value = "/put7")
    public int putRoute7() {
        return 3;
    }

    @DeleteMapping(value = "/delete7")
    public int deleteRoute7() {
        return 4;
    }

    @PostMapping(value = "/post8")
    public int postRoute8() {
        return 1;
    }

    @GetMapping(value = "/get8")
    public int getRoute8() {
        return 2;
    }

    @PutMapping(value = "/put8")
    public int putRoute8() {
        return 3;
    }

    @DeleteMapping(value = "/delete8")
    public int deleteRoute8() {
        return 4;
    }

    @PostMapping(value = "/post9")
    public int postRoute9() {
        return 1;
    }

    @GetMapping(value = "/get9")
    public int getRoute9() {
        return 2;
    }

    @PutMapping(value = "/put9")
    public int putRoute9() {
        return 3;
    }

    @DeleteMapping(value = "/delete9")
    public int deleteRoute9() {
        return 4;
    }

    @PostMapping(value = "/post10")
    public int postRoute10() {
        return 1;
    }

    @GetMapping(value = "/get10")
    public int getRoute10() {
        return 2;
    }

    @PutMapping(value = "/put10")
    public int putRoute10() {
        return 3;
    }

    @DeleteMapping(value = "/delete10")
    public int deleteRoute10() {
        return 4;
    }

    @PostMapping(value = "/post11")
    public int postRoute11() {
        return 1;
    }

    @GetMapping(value = "/get11")
    public int getRoute11() {
        return 2;
    }

    @PutMapping(value = "/put11")
    public int putRoute11() {
        return 3;
    }

    @DeleteMapping(value = "/delete11")
    public int deleteRoute11() {
        return 4;
    }

    @PostMapping(value = "/post12")
    public int postRoute12() {
        return 1;
    }

    @GetMapping(value = "/get12")
    public int getRoute12() {
        return 2;
    }

    @PutMapping(value = "/put12")
    public int putRoute12() {
        return 3;
    }

    @DeleteMapping(value = "/delete12")
    public int deleteRoute12() {
        return 4;
    }

    @PostMapping(value = "/post13")
    public int postRoute13() {
        return 1;
    }

    @GetMapping(value = "/get13")
    public int getRoute13() {
        return 2;
    }

    @PutMapping(value = "/put13")
    public int putRoute13() {
        return 3;
    }

    @DeleteMapping(value = "/delete13")
    public int deleteRoute13() {
        return 4;
    }

    @PostMapping(value = "/post14")
    public int postRoute14() {
        return 1;
    }

    @GetMapping(value = "/get14")
    public int getRoute14() {
        return 2;
    }

    @PutMapping(value = "/put14")
    public int putRoute14() {
        return 3;
    }

    @DeleteMapping(value = "/delete14")
    public int deleteRoute14() {
        return 4;
    }

    @PostMapping(value = "/post15")
    public int postRoute15() {
        return 1;
    }

    @GetMapping(value = "/get15")
    public int getRoute15() {
        return 2;
    }

    @PutMapping(value = "/put15")
    public int putRoute15() {
        return 3;
    }

    @DeleteMapping(value = "/delete15")
    public int deleteRoute15() {
        return 4;
    }

    @PostMapping(value = "/post16")
    public int postRoute16() {
        return 1;
    }

    @GetMapping(value = "/get16")
    public int getRoute16() {
        return 2;
    }

    @PutMapping(value = "/put16")
    public int putRoute16() {
        return 3;
    }

    @DeleteMapping(value = "/delete16")
    public int deleteRoute16() {
        return 4;
    }

    @PostMapping(value = "/post17")
    public int postRoute17() {
        return 1;
    }

    @GetMapping(value = "/get17")
    public int getRoute17() {
        return 2;
    }

    @PutMapping(value = "/put17")
    public int putRoute17() {
        return 3;
    }

    @DeleteMapping(value = "/delete17")
    public int deleteRoute17() {
        return 4;
    }

    @PostMapping(value = "/post18")
    public int postRoute18() {
        return 1;
    }

    @GetMapping(value = "/get18")
    public int getRoute18() {
        return 2;
    }

    @PutMapping(value = "/put18")
    public int putRoute18() {
        return 3;
    }

    @DeleteMapping(value = "/delete18")
    public int deleteRoute18() {
        return 4;
    }

    @PostMapping(value = "/post19")
    public int postRoute19() {
        return 1;
    }

    @GetMapping(value = "/get19")
    public int getRoute19() {
        return 2;
    }

    @PutMapping(value = "/put19")
    public int putRoute19() {
        return 3;
    }

    @DeleteMapping(value = "/delete19")
    public int deleteRoute19() {
        return 4;
    }
}

class User {

    private String name;

    public User(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}