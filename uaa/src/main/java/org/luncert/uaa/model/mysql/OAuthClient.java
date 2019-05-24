package org.luncert.uaa.model.mysql;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class OAuthClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;

    private String clientSecret;

    private String redirectUrl;

    /**
     * authorization_code
     * client_credentials
     * password
     * implicit
     */
    private String grantType;

    private String scope;

}