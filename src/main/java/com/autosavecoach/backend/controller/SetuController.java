package com.autosavecoach.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setu")
public class SetuController {

    @GetMapping("/callback")
    public String callback(
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String errorcode,
            @RequestParam(required = false) String errormsg) {

        return "Setu callback received: success=" + success
                + ", id=" + id
                + ", errorcode=" + errorcode
                + ", errormsg=" + errormsg;
    }
}