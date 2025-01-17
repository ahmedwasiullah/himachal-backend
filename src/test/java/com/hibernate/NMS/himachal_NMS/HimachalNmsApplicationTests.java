package com.hibernate.NMS.himachal_NMS;

import com.hibernate.NMS.himachal_NMS.config.WebSocketConfig;
import com.hibernate.NMS.himachal_NMS.controller.DistrictController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


@ImportAutoConfiguration(exclude = {WebSocketConfig.class, ServerEndpointExporter.class})
//@SpringBootTest
class HimachalNmsApplicationTests {

	@Test
	void contextLoads() {
	}

}
