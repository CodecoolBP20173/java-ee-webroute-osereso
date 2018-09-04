package com.codecool.annotation.server;

import com.codecool.annotation.annot.WebRoute;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MyHandler implements HttpHandler {

    public static List<String> names = new ArrayList<>();

    @WebRoute(path="/test")
    public String inTest(HttpExchange t) {
        return "Showing the test page!";
    }

    @WebRoute(path="/user")
    public String getUserTest(HttpExchange t) {
        names.add("Victor");
        names.add("Erika");
        return String.join(", ", names);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        String requestPath = t.getRequestURI().getPath();
        String response = "";
        Method[] methods = this.getClass().getMethods();

        for(Method m : methods) {
            Annotation[] annotations = m.getDeclaredAnnotations();

            for (Annotation a : annotations) {

                if (a instanceof WebRoute) {
                    WebRoute annotation = (WebRoute) a;

                    if (annotation.path().equals(requestPath) && annotation.method().equals(t.getRequestMethod())) {

                        try {
                            response = (String) m.invoke(this, t);
                            break;
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            response = "There was an error:\n" + e.getMessage();
                        }
                    }
                }
            }
        }

        if (response.equals("")) {
            response = "404 route not found! " + requestPath;
            t.sendResponseHeaders(404, response.getBytes().length);
        } else {
            t.sendResponseHeaders(200, response.getBytes().length);
        }
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();


    }

}
