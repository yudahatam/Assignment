package Assignment;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;

public class listener extends AbstractVerticle {
    private MessageConsumer<String> listen;

    @Override
    public void start() throws Exception {
        super.start();
        listen = vertx.eventBus().consumer("Start Success", res ->{
            System.out.println(res.body());
        });
    }

    @Override
    public void stop() throws Exception {
        listen.unregister(event -> {
            if (event.succeeded())
                System.out.println("Stopped");
            else
                System.out.println("Failed "+event.cause());
        });
        super.stop();
    }
}
