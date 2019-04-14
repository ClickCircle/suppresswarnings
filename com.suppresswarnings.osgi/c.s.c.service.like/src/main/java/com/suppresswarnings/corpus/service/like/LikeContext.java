package com.suppresswarnings.corpus.service.like;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;
import com.suppresswarnings.osgi.like.model.Project;

public class LikeContext extends WXContext {
	public static final String CMD = "我要发起点赞";
	Project project = new Project();
	Gson gson = new Gson();
	State<Context<CorpusService>> like = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8864185195064611468L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			project.setOpenid(openid());
			project.setProjectid(String.join(Const.delimiter, "Project", time(), openid()));
			project.setBonusCent("1000");
			project.setTime(time());
			u.output("请输入点赞宣传语：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(t.startsWith("SCAN_")) {
				return like;
			}
			if(CMD.equals(t)) {
				return like;
			}
			return title;
		}

		@Override
		public String name() {
			return "发起点赞初始状态";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> title = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7462979902564338018L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			project.setTitle(t);
			u.output("请上传图片（最多4张）：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return picture;
		}

		@Override
		public String name() {
			return "点赞宣传语";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> picture = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7086519805654327293L;
		int count = 4;
		@Override
		public void accept(String t, Context<CorpusService> u) {
			count --;
			String image = t;
			if(t.startsWith("IMAGE_")) {
				image = t.substring("IMAGE_".length());
			}
			project.addPicture(image);
			u.output("请继续上传图片（最多"+count+"张）(或输入“完成”即可结束上传)：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(count <= 0) {
				return finish;
			}
			if("完成".equals(t)) {
				return finish;
			}
			return picture;
		}

		@Override
		public String name() {
			return "点赞图片";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> finish = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8693241831167651212L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			logger.info(project.toString());
			u.content().atUser(openid(), "恭喜你完成了创建点赞，发起点赞之后，分享到朋友圈开始集赞，达到集赞目标之后，即可获得最高100元现金奖励，所有参与点赞者均有份！");
			WXnews news = new WXnews();
			news.setTitle("先赞一个亿，分享到朋友圈，开始集赞吧！");
			news.setDescription("进去点赞哦！完成集赞目标，即可获得最高100元现金奖励，所有参与点赞者均有份！");
			news.setUrl("http://SuppressWarnings.com/like.html?state="+project.getProjectid());
			String json = gson.toJson(news);
			u.output("news://" + json);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "点赞项目完成";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	
	
	public void save() {
		content().account().put(String.join(Const.delimiter, Const.Version.V1, "Projectid", project.getProjectid()), project.getProjectid());
		content().account().put(String.join(Const.delimiter, Const.Version.V1, "Project", "Openid", project.getProjectid()), project.getOpenid());
		content().account().put(String.join(Const.delimiter, Const.Version.V1, "Project", "Title", project.getProjectid()), project.getTitle());
		content().account().put(String.join(Const.delimiter, Const.Version.V1, "Project", "Pictures", project.getProjectid()), project.getPictures());
		content().account().put(String.join(Const.delimiter, Const.Version.V1, "Project", "BonusCent", project.getProjectid()), project.getBonusCent());
		
		content().account().put(String.join(Const.delimiter, Const.Version.V1, openid(), "Projectid", project.getProjectid()), project.getProjectid());
	}
	
	public LikeContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = like;
	}

}
