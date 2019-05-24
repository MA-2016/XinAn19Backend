package org.luncert.uaa.repos.mysql;

import org.luncert.uaa.model.mysql.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthClientRepos extends JpaRepository<OAuthClient, Long> {

    OAuthClient findByClientId(String clientId);

}