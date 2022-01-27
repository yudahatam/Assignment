package Assignment;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RestVerticle extends AbstractVerticle {
    @Override
    public void start()   {
        vertx.eventBus().publish("Start Success","RestVerticle Started Successfully");
        Router router=Router.router(vertx);
        router.route().handler(BodyHandler.create());
        SessionStore store = SessionStore.create(vertx);
//        router.route().handler(LoggerHandler.create());       //Logger
//        SessionStore store = LocalSessionStore.create(vertx); // Local store
        router.route().handler(SessionHandler.create(store));
        router.get("/hello").handler(ctx->{
           ctx.response().setChunked(true).end("First\n");
        });
        /**
         * POST request of login with parameters in body as JSON
         * */
        router.post("/login").handler(ctx->{
            if (!ctx.session().isEmpty())
                ctx.response().end("Already logged in");
            else {
                JsonObject jsonObject = ctx.getBodyAsJson();
                String username = jsonObject.getString("username");
                String password = jsonObject.getString("password");
                boolean result = false;
                try {
                    result = login(username, password);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (result) {
                    Session session = ctx.session();
                    session.put("Id", session.id());
                }
                ctx.response().end(String.format("Username\t : %s\nPassword\t : %s\nCould log in : %s\nSession id\t : %s", username, password, String.valueOf(result), ctx.session().id()));
            }
        });

        /**
         * POST request of logout
         */
        router.post("/logout").handler(ctx->{
            if (ctx.session().isEmpty())
                ctx.response().end("Can't logout if you are not logged in");
            else {
                String session = ctx.session().get("Id");
                ctx.session().destroy();
                ctx.response().end(String.format("Session %s has been logged out", session));
            }
        });

        /**
         * POST request of AddOrder which receive parameters in body as JSON
         */
        router.post("/addOrder").handler(ctx->{
            if (ctx.session().isEmpty())
                ctx.response().end("Error in adding order please log in first");
            else {
                JsonObject jsonObject = ctx.getBodyAsJson();
                jsonObject.put("sessionId", ctx.session().id());
                vertx.eventBus().publish("addOrder",jsonObject);
                String orderId = jsonObject.getString("orderId");
                String orderName = jsonObject.getString("orderName");
                String date = jsonObject.getString("date");
                ctx.response().end(String.format("Order ID\t: %s\nOrder name\t: %s\nDate\t\t: %s\nSession Id \t: %s", orderId, orderName, date, ctx.session().id()));
            }
        });

        /**
         * GET request of getOrders
         */
        router.get("/getOrders").handler(ctx->{
            ctx.response().setChunked(true);
            if (ctx.session().isEmpty())
                ctx.response().end("Error in getting orders please log in first");
            else {
                String sessionId=ctx.session().id();
                vertx.eventBus().request("getOrders", sessionId,reply->{
                    if (reply.succeeded()){
                        JsonArray result = (JsonArray) reply.result().body();
                        ArrayList<JsonObject> list = (ArrayList) result.getList();
                        for (int i=0;i<list.size();i++) {
                            JsonObject obj = list.get(i);
                            String orderId = obj.getString("orderId");
                            String orderName = obj.getString("orderName");
                            String date = obj.getString("date");
                            String replySessionId = obj.getString("sessionId");
                            ctx.response().write(String.format("%d.\nOrder ID\t: %s\nOrder name\t: %s\nDate\t\t: %s\nSession Id \t: %s\n\n",i+1, orderId, orderName, date, replySessionId));
                        }
                        ctx.response().end();
                    }
                    else
                        ctx.response().end("Reply from event bus failed");
                });
            }
        });
        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    private boolean login(String username,String passowrd) throws IOException {
        boolean result=false;
        String jsonStr = IOUtils.toString(new FileReader("src/main/java/Assignment/login.json"));
        Map jsonObj = new JsonObject(jsonStr).getMap();
        ArrayList<HashMap> listOfUsers = (ArrayList) jsonObj.get("users");
        int existFlag=0;
        for (HashMap user : listOfUsers) {
            String name = (String) user.get("username");
            String pass = (String) user.get("password");
            if ((name.equals(username)) && (pass.equals(passowrd)) && existFlag==0) {
                existFlag=1;
                result = true;
            }
        }
        if(existFlag==0) {
            System.out.println("User doesn't exist");
        }
        return result;
    }
    @Override
    public void stop() throws Exception {

    }
}
