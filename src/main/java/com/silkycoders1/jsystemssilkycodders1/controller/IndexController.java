package com.silkycoders1.jsystemssilkycodders1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to serve the React frontend.
 * All routes except /api/** are forwarded to index.html for client-side routing.
 * This enables React Router to handle client-side navigation.
 * 
 * Note: API routes (/api/**) are handled by @RestController classes
 * and will take precedence over this controller.
 */
@Controller
public class IndexController {

    /**
     * Serves index.html for all non-API routes.
     * This catch-all pattern ensures React Router can handle client-side navigation.
     */
    @GetMapping(value = {"/", "/{path:[^\\.]*}", "/{path1:[^\\.]*}/{path2:[^\\.]*}"})
    public String index() {
        return "forward:/index.html";
    }
}
