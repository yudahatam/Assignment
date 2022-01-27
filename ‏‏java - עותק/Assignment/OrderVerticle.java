package Assignment;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Session;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderVerticle extends AbstractVerticle {
    HashMap<String, JsonArray> orderMap=new HashMap<>();
    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().publish("Start Success","OrderVerticle Started Successfully");
        vertx.eventBus().consumer("addOrder",handler->{
            JsonObject order = (JsonObject) handler.body();
            addOrder(order);
        });
        vertx.eventBus().consumer("getOrders",handler->{
            String sessionId= (String) handler.body();
            JsonArray reply=orderMap.get(sessionId);
            handler.reply(reply);
        });
    }

    private void addOrder(JsonObject jsonObject){
        JsonArray list = orderMap.get(jsonObject.getString("sessionId"));
        if (list==null) {
            JsonArray arr=new JsonArray();
            arr.add(jsonObject);
            orderMap.put(jsonObject.getString("sessionId"),arr);
        }
        else {
            list.add(jsonObject);
        }
        //Write to file using  Session id as file name
        String session = jsonObject.getString("sessionId");
        try {
            list=orderMap.get(session);
            FileWriter file = new FileWriter(String.format("src\\main\\java\\Jsons\\%s.json",session));
            file.write(list.toString()+"\n");
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
