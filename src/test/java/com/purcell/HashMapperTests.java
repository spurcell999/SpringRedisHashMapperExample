package com.purcell;


import com.purcell.domain.Address;
import com.purcell.domain.Gender;
import com.purcell.domain.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HashMapperTests {

    @Autowired
    @Qualifier("cloud")
    RedisConnectionFactory connectionFactory;

    @Autowired
    HashMapper<Object, byte[], byte[]> mapper;

    RedisConnection connection;
    StringRedisConnection stringConnection;

    @Before
    public void setUp() {

        connection=connectionFactory.getConnection();

        connection.flushAll();

        stringConnection = new DefaultStringRedisConnection(connection);
    }

    @After
    public void after() {
        connection.close();
    }

    @Test
    public void mapToFromHash() {

        Address addr = new Address();
        addr.setCity("O'Fallon");
        addr.setCountry("MO");

        Person mongoJerry = new Person("Mongo", "Jerry", Gender.MALE);
        mongoJerry.setId("10001");
        mongoJerry.setAddress(addr);

        Map<String, String> rawHash = mapper.toHash(mongoJerry).entrySet().stream()
                .collect(Collectors.toMap(e -> new String(e.getKey()), e -> new String(e.getValue())));

        System.out.println(rawHash);

        stringConnection.hMSet("mj:777", rawHash);

        Person fromHash = (Person) mapper.fromHash(stringConnection.hGetAll("mj:777".getBytes()));

        assertThat(fromHash, is(equalTo(mongoJerry)));
    }

    @Test
    public void manipulateHashAndReadItBack() {

        Address addr = new Address();
        addr.setCity("O'Fallon");
        addr.setCountry("USA");

        Person trump = new Person("Donald", "TTrump", Gender.MALE);
        trump.setId("45");
        trump.setAddress(addr);

        stringConnection.hMSet("trump:123", mapper.toHash(trump).entrySet().stream()
                .collect(Collectors.toMap(e -> new String(e.getKey()), e -> new String(e.getValue()))));

        stringConnection.hSet("trump:123", "lastname", "Trump");

        Person fromHash = (Person) mapper.fromHash(connection.hGetAll("trump:123".getBytes()));

        System.out.println(fromHash);

        assertThat(fromHash, is(not(equalTo(trump))));
    }

}
