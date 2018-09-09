package com.pacificdataservices.pdssr;

import java.io.IOException;

import org.javautil.sql.Binds;
import org.junit.Test;

public class AddressGeneratorTest {

    @Test
    public void test() throws IOException {
        AddressGenerator gen = new AddressGenerator();
        Binds b =gen.getAddress();
        System.out.println(b);
    }
    
    @Test
    public void test10000() throws IOException {
        AddressGenerator gen = new AddressGenerator();
        for (int i = 0;i < 10000; i++) {
     
        System.out.println(gen.getAddress());
        }
    }
}
