package com.metabroadcast.includes.www;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IncludesController {
    
    @RequestMapping("/includes/javascript")
    @ResponseBody
    public String javascript() {
        
        //if (true) {
            return "<script type=\"text/javascript\" src=\"/js/base.js\"></script>" +
                    "<script type=\"text/javascript\" src=\"/js/soyutils.js\"></script>" +
                    "<script type=\"text/javascript\" src=\"/js/jquery.js\"></script>" +
                    "<script type=\"text/javascript\" src=\"/js/jquery-ui.js\"></script>" +
                    "<script type=\"text/javascript\" src=\"/js/jquery.iphone.js\"></script>" +
                    "<script type=\"text/javascript\" src=\"/js/jquery.loading.1.6.4.js\"></script>" +
                    "<script type=\"text/javascript\" src=\"/js/ui.iTabs.js\"></script>" +
                    "<script type=\"text/javascript\" src=\"/js/js.js\"></script>";
        //}
        // Naughty
        //return "<script type=\"text/javascript\" src=\"/js/closure/generated/global.js\"></script>";
    }
}
