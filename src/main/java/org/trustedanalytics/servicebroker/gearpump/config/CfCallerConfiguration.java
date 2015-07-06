/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustedanalytics.servicebroker.gearpump.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.CfCaller;

import javax.validation.constraints.NotNull;

@Configuration
public class CfCallerConfiguration {
    @Value("${gearpump.uiSpace}")
    @NotNull
    private String gearPumpUiSpace;

    @Value("${gearpump.uiOrg}")
    @NotNull
    private String gearPumpUiOrg;

    @Value("${gearpump.uiName}")
    @NotNull
    private String gearPumpUiName;

    public String getGearpumpUiName() {
        return gearPumpUiName;
    }

    public String getGearpumpUiOrg() {
        return gearPumpUiOrg;
    }

    public String getGearpumpUiSpace() {
        return gearPumpUiSpace;
    }

    @Bean
    public CfCaller cfCaller(OAuth2RestTemplate oAuth2RestTemplate) {
        return new CfCaller(oAuth2RestTemplate);
    }

    @Bean
    public OAuth2RestTemplate oAuth2RestTemplate(OAuth2ProtectedResourceDetails clientCredentials,
                                                 OAuth2ClientContext clientContext) {
        OAuth2RestTemplate template = new OAuth2RestTemplate(clientCredentials, clientContext);
        ClientCredentialsAccessTokenProvider tokenProvider = new ClientCredentialsAccessTokenProvider();
        template.setAccessTokenProvider(tokenProvider);

        return template;
    }

    @Bean
    @ConfigurationProperties("spring.oauth2.client")
    public OAuth2ProtectedResourceDetails clientCredentials() {
        return new ClientCredentialsResourceDetails();
    }

    @Bean
    public OAuth2ClientContext oauth2ClientContext() {
        return new DefaultOAuth2ClientContext(new DefaultAccessTokenRequest());
    }
}
