package com.payflow.notificationservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class TemplateEngineService {

    public String render(String template, Map<String , Object> variable){
        if (template == null)return "";
        if (variable == null || variable.isEmpty())return template;

        String rendered = template;
        for (Map.Entry<String , Object>entry : variable.entrySet()){
            String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            // Supports both [[${key}]] and {{key}} formats
            rendered = rendered.replace("[[${" + entry.getKey() + "}]]", value);
            rendered = rendered.replace("{{" + entry.getKey() + "}}", value);
        }
        return rendered;
    }


}
