package edu.usc.irds.sparkler.sparklerrest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GreetingController {

	@GetMapping("/greeting")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		model.addAttribute("name", name);
		return "greeting";
	}

	@PostMapping("/greeting2")
	public ResponseEntity<String> pinger(){
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(CustomMediaType.TURBO_STREAM);

		return ResponseEntity.ok().contentType(CustomMediaType.TURBO_STREAM).body("<turbo-stream action=\"append\" target=\"pings\">\n" +
				"  <template>\n" +
				"    <li>70</li>\n" +
				"  </template>\n" +
				"</turbo-stream>");
	}

}