package indi.dkx.laocai;

import indi.dkx.laocai.annotation.EnableLaocaiBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableLaocaiBot
@SpringBootApplication
public class LaocaiApplication {

	static void main(String[] args) {
		SpringApplication.run(LaocaiApplication.class, args);
	}

}
