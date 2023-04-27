package com.szu.platform.controller;

import com.szu.platform.service.ILoxService;
import com.szu.platform.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/code/lox")
public class LoxController {

    @Autowired
    private ILoxService loxService;

    @GetMapping("/test")
    public Result<String> getTest() {
        return Result.success("测试成功");
    }

    @PostMapping("/run")
    public Result<String> run(@RequestBody Map<String, Object> requestBody) {
        if(!requestBody.containsKey("source")) return Result.fail("没有source键");
        String result = loxService.run((String) requestBody.get("source"));
        return Result.success(result,"success");
    }
}
