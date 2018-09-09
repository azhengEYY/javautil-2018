package com.pacificdataservices.pdssr;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import org.javautil.io.FileUtil;
import org.javautil.text.CsvReader;
import org.javautil.util.ListOfNameValue;
import org.javautil.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.javautil.sql.Binds;

//class AddressGenerator:
//    city_state = []
//
//    def __init__(self):
//        self.read_city_state()
//        street_file = open("../testdata/streets.dat")
//        self.streets = street_file.readlines()
//
//    def read_city_state(self):
//        with open("../testdata/city_tx.csv", 'rb') as f:
//            reader = csv.reader(f, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
//            i = 0
//            for city, state, postal_cd in reader:
//                city = city.rstrip()
//                state = state.rstrip()
//                postal_cd = postal_cd.rstrip()
//                self.city_state.append((city, state, postal_cd))
//                i += 1
//
//    def get_city(self):
//        upper = len(self.city_state) - 1
//        ndx = random.randint(0, upper)

public class AddressGenerator {
    public Logger           logger    = LoggerFactory.getLogger(getClass());

    private List<String>    streets;
    private ListOfNameValue cities;
    private int             seed      = 314159;
    private Random          generator = new Random(seed);
    private static final String streetFileName = "src/main/resources/streets.text";

    public AddressGenerator() throws IOException {
        logger.info("starting address generator");
        final Charset charset = Charset.forName("ISO-8859-1");
        final Path streetPath = Paths.get(streetFileName);
        
       streets =  Files.readAllLines(streetPath, charset);
       // streets = FileUtil.readAllLines("src/main/resources/streets.text");
        logger.info("streets.size :" + streets.size());
       cities = new CsvReader("src/main/resources/city_tx.csv").setHasHeader(true)
                .readLinesAsListOfNameStringValue();

    }

    Binds getAddress() {
        Binds binds = new Binds();
        int streetNumber = generator.nextInt(10000);
        int streetIndex = generator.nextInt(streets.size());
        int cityIndex = generator.nextInt(cities.size());
        String streetName = streets.get(streetIndex);
        NameValue city = cities.get(cityIndex);
        String message = String.format("streetNumber %d street %s, city %s", streetNumber, streetName, city);
        System.out.println(message);
        binds.put("address_1", streetNumber + " " + streetName);
        binds.put("city", city.get("City"));
        binds.put("state", city.get("State"));
        binds.put("zip5", city.get("Zip5"));
        return binds;

    }

}
