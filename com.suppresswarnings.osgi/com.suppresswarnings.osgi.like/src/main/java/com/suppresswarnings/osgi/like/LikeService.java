package com.suppresswarnings.osgi.like;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Provider;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.like.impl.LikeHandlerImpl;
import com.suppresswarnings.osgi.like.model.Page;
import com.suppresswarnings.osgi.like.model.Result;
import com.suppresswarnings.osgi.like.model.Project;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class LikeService implements HTTPService, CommandProvider {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public Map<String, Provider<?>> providers = new HashMap<>();
	public Gson gson = new Gson();
	public LevelDB account, data, token;
	public LikeHandler handler;
	long start = System.currentTimeMillis();
	ScheduledExecutorService service = Executors.newScheduledThreadPool(3, new ThreadFactory() {
		AtomicInteger integer = new AtomicInteger(1);
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "LikeService-Thread-" + integer.getAndIncrement());
		}
	});
	
	@Override
	public String getHelp() {
		StringBuffer sb = new StringBuffer();
		sb.append("like list --show all like project");
		return sb.toString();
	}
	
	public void _like(CommandInterpreter ci) {
		String arg = ci.nextArgument();
		logger.info("command like " + arg);
		String ret = arg + " => " + new Date(start).toString() + " -> " + new Date().toString();
		ci.println(ret);
	}

	@Override
	public String getName() {
		return "like.http";
	}

	@Override
	public String start(Parameter parameter) throws Exception {
		logger.info("like request: " + parameter);
		String action = parameter.getParameter("action");
		if("project".equals(action)) {
			String projectid = parameter.getParameter("projectid");
			String openid = parameter.getParameter("openid");
			Page<Project> page = handler.get(projectid, openid);
			Result result = new Result(page);
			Map<String, String> extra = new HashMap<>();
			extra.put("uface", "http://thirdwx.qlogo.cn/mmopen/6XNMsXhEtvJdxWbKRtXG3RWZMaggh1BBYbNL6oZLKlKCZ1BOicq09TbCFg6Hqfia4MgYfiaEcHc67DlwZnibqVZIfEJOLJ6p6AHY/132");
			extra.put("name", "香克斯");
			result.setExtra(extra);
			return gson.toJson(result);
		}
		return "like";
	}
	
	String gitpull() throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder();
		List<String> commands = new ArrayList<String>();  
		commands.add("git");  
		commands.add("pull");  
		processBuilder.command(commands);  
		processBuilder.directory(new File("/usr/share/nginx/suppresswarnings/com.suppresswarnings.html/src/html"));  
		processBuilder.redirectErrorStream(true);  
		Process process = processBuilder.start();  
		StringBuilder result = new StringBuilder();  
		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));  
		try {  
			String cmd = processBuilder.command().toString();
		    String line;
		    while ((line = reader.readLine()) != null) {  
		           result.append(line);
		           if(line.contains("Already")) {
		        	   logger.debug(cmd + " --->: " + line);
		           } else {
		        	   logger.info(cmd + " --->: " + line);
		           }
		    }
		    
		} catch (Exception e) {  
			logger.warn("failed to read output from process", e);  
		} finally {  
		    reader.close();  
		} 
		process.waitFor();  
		int exit = process.exitValue();  
		if (exit != 0) { 
			logger.error("fail to execute command git pull");
		}
		
		return result.toString();  
	}

	public void activate() {
		logger.info("LikeService activate");
		handler = new LikeHandlerImpl(this);
		service.scheduleWithFixedDelay(()->{
			try {
				logger.info("start to execute git pull");
				gitpull();
			} catch (Exception e) {
				logger.error("Fail to git pull", e);
			}
		}, 3, 10, TimeUnit.SECONDS);
		data().list("001.", 10, (k,v)->{
			logger.info("test data: " + k + " = " + v);
		});
	}
	public void deactivate() {
		logger.info("[LikeService] deactivate.");
		service.shutdown();
	}
	public void modified() {
		logger.info("[LikeService] modified.");
	}
	
	public void provide(Provider<?> provider) {
		logger.info("provider: " + provider.description());
		String id = provider.identity();
		Object instance = provider.instance();
		Provider<?> old = providers.put(id, provider);
		logger.info("put new provider: " + instance + " replace if exists: " + old);
	}
	public void clearProvider(Provider<?> provider) {
		logger.info("clear provider: " + provider.description());
		String id = provider.identity();
		Object instance = provider.instance();
		boolean b = providers.remove(id, provider);
		logger.info("remove instance: " + instance + " if found: " + b);
	}
	public LevelDB account(){
		if(account != null) {
			return account;
		}
		account = getOrDefault("Account");
		return account;
	}
	public LevelDB data(){
		if(data != null) {
			return data;
		}
		data = getOrDefault("Data");
		return data;
	}
	public LevelDB token(){
		if(token != null) {
			return token;
		}
		token = getOrDefault("Token");
		return token;
	}
	public LevelDB getOrDefault(String key) {
		logger.info("[LikeService] get leveldb: " + key);
		Provider<?> provider = providers.get(key);
		if(provider == null) {
			logger.error("[LikeService] get null from providers by key: " + key);
			return null;
		}
		LevelDB instance = (LevelDB) provider.instance();
		return instance;
	}
}