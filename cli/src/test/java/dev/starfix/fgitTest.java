package dev.fgit;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;


import static dev.fgit.fgit.validate_url;
import static dev.fgit.fgit.runCommand;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;



@QuarkusTest
public class fgitTest {

    @Test
    public void testEcho()throws Exception {
        Path directory = Paths.get(System.getProperty( "user.home" ));
        String test_string="This is some random String that I want to Echo";
        assertEquals(test_string,runCommand(directory,"echo",test_string),"Echo Random String");
    }
  

}