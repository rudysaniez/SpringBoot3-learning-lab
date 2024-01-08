package com.springboot.learning.sb3.properties;

import com.springboot.learning.sb3.config.PropertiesConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PropertiesTest {

    @Autowired
    PropertiesConfig.UserSecurityAccessConfiguration userSecurityAccess;

    @Test
    void userSecurityAccess() {

        Assertions.assertThat(userSecurityAccess.users()).hasSize(3);
        Assertions.assertThat(userSecurityAccess.users().getFirst().username()).isEqualTo("user");
        Assertions.assertThat(userSecurityAccess.users().getFirst().password()).isEqualTo("user");
        Assertions.assertThat(userSecurityAccess.users().getFirst().authorities()).hasSize(2);
    }
}
