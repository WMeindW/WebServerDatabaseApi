package cz.meind.user;

import cz.meind.interfaces.Api;
import cz.meind.interfaces.GetMapping;

@Api("/api")
public class ApiTest {

    @GetMapping(value = "/test-method")
    public String testMethod() {
        return "Test method called!";
    }
}
