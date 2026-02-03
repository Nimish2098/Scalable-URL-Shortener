package com.project.urlshortener.service.impl;


import com.project.urlshortener.service.ShortCodeGenerator;
import org.springframework.stereotype.Service;

@Service
public class Base62ShortCodeGenerator implements ShortCodeGenerator {

    private static final String base62digits = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String generate(Long id){
        if(id<=0){
            throw new IllegalArgumentException("Id must be positive");
        }

        StringBuilder base62 = new StringBuilder();
        while(id>0){
            int rem = (int)(id%62);
            base62.append(base62digits.charAt(rem));
            id = id/62;

        }
        return base62.reverse().toString();
    }


}
