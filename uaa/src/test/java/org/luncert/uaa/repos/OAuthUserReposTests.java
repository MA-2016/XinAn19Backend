package org.luncert.uaa.repos;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.luncert.uaa.model.mysql.OAuthUser;
import org.luncert.uaa.repos.mysql.OAuthUserRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class OAuthUserReposTests {

    @Autowired
    private OAuthUserRepos repos;

    @Test
    public void testSaveAndFind() {
        OAuthUser user = new OAuthUser();
        user.setUsername("test");
        user.setPassword("dacasrwa");
        user = repos.save(user);
        Assert.assertNotNull("save failed", user);

        Optional<OAuthUser> tmp = repos.findById(user.getId());
        Assert.assertTrue("find failed", tmp.isPresent());

        OAuthUser queryResult = tmp.get();
        Assert.assertTrue("query result doesn't match to initial data",
            user.getId() == queryResult.getId()
            && user.getUsername().equals(queryResult.getUsername())
            && user.getPassword().equals(queryResult.getPassword())
        );
    }

}