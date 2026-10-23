package com.gulabi.rides;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@org.springframework.scheduling.annotation.EnableAsync
@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class CabApplication { public static void main(String[] a){ SpringApplication.run(CabApplication.class,a); } }
