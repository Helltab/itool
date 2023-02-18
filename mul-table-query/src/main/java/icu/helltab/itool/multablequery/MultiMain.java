package icu.helltab.itool.multablequery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class MultiMain {
	public static void main(String[] args)  {
		SpringApplication springApplication = new SpringApplication(MultiMain.class);
		springApplication.run(args);
	}

	void dod() {
		System.out.println(333);
	}
}
