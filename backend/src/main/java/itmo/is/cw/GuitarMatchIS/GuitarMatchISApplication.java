package itmo.is.cw.GuitarMatchIS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GuitarMatchISApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuitarMatchISApplication.class, args);
	}

}
