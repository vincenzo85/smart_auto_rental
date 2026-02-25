package com.smartautorental.platform.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UiController {

    @GetMapping({"/", "/ui"})
    public String index() {
        return "index";
    }
}
