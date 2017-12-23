package util;

import org.apache.http.client.fluent.Executor;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class CloudfareHelper {

    public static Executor skipAntiBot(Executor executor, String url) {
        // https://github.com/Anorov/cloudflare-scrape/blob/master/cfscrape/__init__.py

        ScriptEngineManager factory = new ScriptEngineManager();

        // create JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        // evaluate JavaScript code from given file
        try {
            Object test = engine.eval("print('hi')");
            System.out.println("test = " + test);
            System.out.println("test = " + test);
        } catch (ScriptException e) {
            e.printStackTrace();
        }


        return executor;
    }

    public static void main(String[] args) {
        skipAntiBot(null, null);
    }
}
