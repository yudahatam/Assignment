package Assignment;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class Main {
    public static void main(String[] args) throws IOException {
        Vertx vertx= Vertx.vertx();
        vertx.deployVerticle(new listener(),res1->{
            if (res1.succeeded())
                vertx.deployVerticle(new RestVerticle(),res2->{
                    if (res2.succeeded())
                        vertx.deployVerticle(new OrderVerticle());
                });
        });

        //        Code for reading login.json
//        String jsonStr = IOUtils.toString(new FileReader("src/main/java/Assignment/login.json"));
//        JsonObject jsonObj = new JsonObject(jsonStr);
//        System.out.println(jsonObj.getString("username"));

    }
}
