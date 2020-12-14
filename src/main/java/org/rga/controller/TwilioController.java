package org.rga.controller;

import io.smallrye.mutiny.Uni;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.rga.domain.request.UserDomain;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/api/twilio")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class TwilioController {

    @Inject
    Vertx vertx;

    private WebClient client;

    @PostConstruct
    void initialize() {
        VertxOptions vxOptions = new VertxOptions().setBlockedThreadCheckInterval(200000000);
        vertx = io.vertx.mutiny.core.Vertx.vertx(vxOptions);

        this.client = WebClient.create(vertx,
                new WebClientOptions().setDefaultHost("run.mocky.io").setDefaultPort(443).
                        setSsl(true).setTrustAll(true));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/send")
    public Uni<JsonObject> postTwilio(UserDomain userDomain) {
        return client.get("/v3/a8d9cf86-5910-4511-ab17-68f33f436b3e").send().onItem().transform(resp -> {
            if (resp.statusCode() == 200) {
                // Comprobamos si la respuesta recibida el usuario existe o no existe
                JsonObject jsonObject = resp.bodyAsJsonObject();

                if ( jsonObject != null && jsonObject.getBoolean("response")){
                    // Existe usuario y llamamos a Twilio
                    log.info("SE ENVIO EL SMS, EMAIL CORRECTAMENTE EN TWILIO");
                } else {
                    log.info("NO SE ENVIO EL SMS, EMAIL EN TWILIO");
                }
                return resp.bodyAsJsonObject();
            } else {
                return new JsonObject()
                        .put("code", resp.statusCode())
                        .put("message", resp.bodyAsString());
            }
        });
    }
}