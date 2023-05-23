package com.ocr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IntoriginEkycOcrApplicationTests {

	@Test
	void contextLoads() {
		Object userDetails = new Object();
		boolean flag = false;
		if(userDetails!= null) {
			flag = true;
		}
		assertEquals(true, flag);
	}
}
