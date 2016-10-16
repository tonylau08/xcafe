package com.igeeksky.xcafe.example.demo1.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.igeeksky.xcafe.core.pojo.Message;

@RequestMapping("/test")
@org.springframework.stereotype.Controller
public class TestController {
	
	private static final Logger logger = LoggerFactory.getLogger(TestController.class);
	
	/*@Autowired
	private SimpMessagingTemplate messagingTemplate;*/
	
/*	@MessageMapping("/welcome")
	@SendTo("/topic/getResponse")
	public void welcome(String msg){
		//messagingTemplate.convertAndSendToUser("wisely", "/queue/notifications", "-send:" + msg);
	}*/
	
	@RequestMapping("/xx.do")
	public ModelAndView test(HttpServletResponse response) throws IOException{
		/*ServletOutputStream sos = response.getOutputStream();
		sos.print("asdfffff");*/
		ModelAndView mav = new ModelAndView("public/3");
		logger.info("mav.getViewName()"+mav.getViewName());
		return mav;
	}
	
	@RequestMapping("/index.do")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response, Message msg){
		HttpSession session = request.getSession();
		session.setAttribute("sid", "sdfasf"+122);
		String sessionId = session.getId();
		logger.info("session.getCreationTime()" + session.getCreationTime());
		logger.info("sessionId" + sessionId);
		
		ModelAndView mav = new ModelAndView("public/index");
		logger.info("msg" + msg.getKey() + "  " + msg.getValue());
		
		response.addHeader(HttpHeaders.SET_COOKIE, "JSESSIONID="+sessionId);
		return mav;
	}
	
	@ResponseBody
	@RequestMapping("/postv.mo")
	public String postv(@RequestParam MultipartFile upload, HttpServletRequest request, String publicKey, Integer latitude, Integer longitude, Integer age) throws IllegalStateException, IOException{
		
		System.out.println(age);
		System.out.println(publicKey);
		System.out.println(latitude);
		System.out.println(longitude);

		upload.transferTo(new File("d:/newsss" + age +".txt"));
		
		return "code:1, msg:'ok'";
	}
	
	@RequestMapping("/privatePath.do")
	public ModelAndView privatePath(Message msg){
		ModelAndView mav = new ModelAndView("private/admin");
		System.out.println(msg.getKey() + msg.getValue());
		return mav;
	}
	
	

}
