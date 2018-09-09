package org.javautil.salesdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.javautil.sql.Binds;
import org.javautil.util.ListOfNameValue;
import org.junit.Test;

public class ManufacturersTest {

    @Test
    public void test() throws IOException {
        Manufacturers mfrs = new Manufacturers();
        ListOfNameValue listOfBinds = mfrs.getManufacturers();
        assertNotNull(listOfBinds);
        assertEquals(16,listOfBinds.size());
//        Binds binds = listOfBinds.get(0);
//        System.out.println(binds);
//        assertEquals("0000000020",binds.get("org_id"));
//        assertEquals("F-L",binds.get("org_cd"));
//        assertEquals("Frito-Lay",binds.get("org_nm"));
    }
}
