package com.payflow.commonclient.interceptor;

import com.payflow.common.constant.Headers;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;


@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();

        if (attribs instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();

            // forward identity and security claims
            forwardHeader(request, template, Headers.USER_ID);
            forwardHeader(request, template, Headers.USER_EMAIL);
            forwardHeader(request, template, Headers.USER_ROLES);
            forwardHeader(request, template, Headers.AUTHORIZATION);


            // Forward or generate correlation ID for distributed tracing
            String correlationId = request.getHeader(Headers.CORRELATION_ID);
            if (correlationId != null && !correlationId.isEmpty()) {
                  template.header(Headers.CORRELATION_ID , correlationId);
            } else {
                template.header(Headers.CORRELATION_ID, UUID.randomUUID().toString());
            }
        }else{
            //back ground async call
            template.header(Headers.CORRELATION_ID, UUID.randomUUID().toString());
        }
    }

    private void forwardHeader(HttpServletRequest request , RequestTemplate template ,String headerName){
        String value = request.getHeader(headerName);
        if(value != null && !value.isEmpty()){
            template.header(headerName, value);
        }
    }
}
