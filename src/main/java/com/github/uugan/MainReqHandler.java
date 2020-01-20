package com.github.uugan;

import org.json.JSONObject;
import org.rapidoid.http.*;
import org.rapidoid.lambda.*;


import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainReqHandler implements ReqHandler {
    private static Logger logger =Logger.getLogger(MainReqHandler.class.getName());

    @Override
    public Object execute(Req req) throws Exception {
        String retstr = "";
        try {

            logger.info(req.clientIpAddress() + ":" + req.realIpAddress());
            String body = new String(req.body(), StandardCharsets.UTF_8);
            /*checking headers*/
 /*           for (String hname : req.headers().keySet()) {
                String hval = req.header(hname);
                logger.debug("checking header:" + hname + ":" + hval);
                if (hname.equalsIgnoreCase("TOKEN") && hval.equals("D637FA3B16817B01751C1FF7A654414BCF4085FA")) {
                    retstr = Main.INSTANCE.onAdmin(req.uri(), body);
                    logger.debug("on admin result:" + retstr);
                    Resp resp = req.response();
                    resp.contentType(MediaType.TEXT_PLAIN_UTF8);
                    resp.result(retstr);
                    return resp;
                }
            }
  */
            JSONObject json = new JSONObject(body);
            logger.info("body:" + body);
            retstr = Main.INSTANCE.onData(req.uri(), json);
        } catch (Exception ex) {
            logger.log(Level.SEVERE,"Exception occured", ex);
            JSONObject j = new JSONObject();
            j.put("msg", ex.getMessage());
            j.put("code", "999");
            retstr = j.toString();
        }
        Resp resp = req.response();
        resp.code(200);
        resp.contentType(MediaType.TEXT_PLAIN_UTF8);
        resp.result(retstr);
        return resp;
    }
}
