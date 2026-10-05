package com.payflow.authservice.security.oauth2;

import java.util.Map;

public interface OAuth2UserInfoExtractor {

    OAuth2UserInfoDto extract(Map<String, Object> attributes);
}
