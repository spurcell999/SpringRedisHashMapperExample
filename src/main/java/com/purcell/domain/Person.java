package com.purcell.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Person {

    private String id;
    private String firstname;
    private String lastname;
    private Address address;
    private Gender gender;
    private Long ttl;

    public Person(String firstname, String lastname, Gender gender) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.gender = gender;

    }

}
