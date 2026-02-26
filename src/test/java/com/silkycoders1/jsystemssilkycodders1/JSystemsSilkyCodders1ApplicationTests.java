package com.silkycoders1.jsystemssilkycodders1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.r2dbc.url=r2dbc:h2:mem:///testdb-context;DB_CLOSE_DELAY=-1",
	"spring.r2dbc.username=sa",
	"spring.r2dbc.password=",
	"spring.sql.init.mode=always",
	"spring.sql.init.schema-locations=classpath:schema.sql"
})
class JSystemsSilkyCodders1ApplicationTests {

	@Test
	void contextLoads() {
	}

}
