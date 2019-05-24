package org.luncert.uaa.repos.mysql;

import org.luncert.uaa.model.mysql.OAuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthUserRepos extends JpaRepository<OAuthUser, Long> {
    
    OAuthUser findByUsername(String username);

}