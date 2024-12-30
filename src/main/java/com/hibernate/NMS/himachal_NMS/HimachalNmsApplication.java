package com.hibernate.NMS.himachal_NMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableScheduling  // Enable scheduling
@EnableWebSocket
public class HimachalNmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(HimachalNmsApplication.class, args);
	}

}
