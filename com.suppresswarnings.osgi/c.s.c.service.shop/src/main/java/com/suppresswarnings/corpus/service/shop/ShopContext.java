/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.shop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.work.Quiz;

public class ShopContext extends WXContext {
	public static final String CMD = "我的商铺";
	public static final String Wait = "Wait";
	public static final String None = "None";
	String qrcode = null;
	List<KeyValue> quiz = new ArrayList<>();
	State<Context<CorpusService>> shop = new State<Context<CorpusService>>() {
		boolean finish = false;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1572458819181499561L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("管理你的商铺，你可以输入一下命令：");
			//check binded?
			u.output("    " + bind.name());
			u.output("    " + ad.name());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			//check exist shop assistant
			logger.info("input: " + t);
			if(t.startsWith("SCAN_")) return scan;
			if(CMD.equals(t)) return shop;
			if(bind.name().equals(t)) return bind;
			if(ad.name().equals(t)) return ad;
			return shop;
		}

		@Override
		public String name() {
			return "我的商铺";
		}

		@Override
		public boolean finish() {
			return finish;
		}
		
	};
	State<Context<CorpusService>> bind = new State<Context<CorpusService>>() {
		boolean finish = false;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String keyBind = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Bind");
			String keyBindTime = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "BindTime");
			u.content().account().put(keyBind, Wait);
			u.content().account().put(keyBindTime, time());
			u.output("请扫描商铺二维码（请联系[素朴网联]工作人员索取商铺二维码）");
			finish = true;
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(name().equals(t)) return bind;
			return init;
		}

		@Override
		public String name() {
			return "绑定二维码";
		}

		@Override
		public boolean finish() {
			return finish;
		}
		
		
		
	};
	
	State<Context<CorpusService>> ad = new State<Context<CorpusService>>() {
		boolean finish = false;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			//1.check if I am Boss
			//2.Y check binded, N go to 10 quiz
			u.output("发广告" + t);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(!finish) {
				return ad;
			}
			//get out
			finish = false;
			return init;
		}

		@Override
		public String name() {
			return "发广告";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> scan = new State<Context<CorpusService>>() {
		boolean finish = false;
		boolean isCustomer = false;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			finish = true;
			String qrScene = t.substring("SCAN_".length());
			qrcode = qrScene;
			//1.check if I am Boss
			String keyBind = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "Bind");
			String binded = u.content().account().get(keyBind);
			
			String keyOwnerid = String.join(Const.delimiter, Const.Version.V1, "Shop", "Ownerid", qrScene);
			String ownerid = u.content().account().get(keyOwnerid);
			
			logger.info("[Shop scan] bind: " + binded);
			//not binded, customer
			if(binded == null || None.equals(binded)) {
				logger.info("[Shop scan] ownerid: " + ownerid);
				if(ownerid == null) {
					u.output("该商铺二维码还未绑定！");
				} else {
					String keyCustomer = String.join(Const.delimiter, Const.Version.V1, ownerid, "Shop", "Customer", time(), random());
					u.content().account().put(keyCustomer, openid());
					isCustomer = true;
					u.output("请留下您的手机号，中奖后需要凭手机号领奖！");
				}
				return;
			}
			
			if(qrScene.equals(binded)) {
				if(ownerid == null) {
					u.content().account().put(keyOwnerid, openid());
				}
				u.output("您已成为该商铺的主人");
			} else if(Wait.equals(binded) && ownerid == null){
				//onetime flag, reset to None
				u.content().account().put(keyBind, None);
				//1.check binded by others
				String keyBindTime = String.join(Const.delimiter, Const.Version.V1, openid(), "Shop", "BindTime");
				String bindTime = u.content().account().get(keyBindTime);
				logger.info("[Shop scan] bindTime: " + bindTime);
				try {
					long time = Long.parseLong(bindTime);
					if(System.currentTimeMillis() - time > TimeUnit.DAYS.toMillis(1)) {
						u.output("超过24小时，绑定二维码命令失效！请重新进入我的商铺->绑定二维码");
					} else {
						//2.bind
						u.content().account().put(keyBind, qrScene);
						u.content().account().put(keyBindTime, time());
						u.content().account().put(keyOwnerid, openid());
						u.output("绑定成功！");
					}
				} catch (Exception e) {
					u.output("绑定异常，请稍后重试！");
				}
			} else {
				//reset onetime flag
				u.content().account().put(keyBind, None);
				if(openid().equals(ownerid)){
					u.output("你已经绑定该商铺");
				} else {
					u.output("该二维码已经被其他商铺老板绑定了！");
				}
			}
			
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(isCustomer) return customer;
			if(!finish) {
				return scan;
			}
			//get out
			finish = false;
			return init;
		}

		@Override
		public String name() {
			return "扫描商铺二维码";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> customer = new State<Context<CorpusService>>() {
		boolean finish = false;
		boolean first = true;
		Iterator<Quiz> quiz = null;
		Quiz next = null;
		String lastQuizId = null;
		int i=0;
		/**
		 * 
		 */
		private static final long serialVersionUID = 3937145686042893179L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			
			i ++;
			if(first) {
				String keyPhone = String.join(Const.delimiter, Const.Version.V1, openid(), "Contact", "Phone");
				String phone = u.content().account().get(keyPhone);
				if(phone != null) {
					String keyHistory = String.join(Const.delimiter, Const.Version.V1, openid(), "Contact", "PhoneHistory", time());
					u.content().account().put(keyHistory, phone);
				}
				u.content().account().put(keyPhone, t);
				logger.info("[Shop customer] save phone: " + openid() + " => " + t);
				u.output("请回答10个问题，大奖等你来拿！");
				first = false;
				quiz = getQuiz(u.content(), 10);
			} else {
				String keyReply = String.join(Const.delimiter, lastQuizId, "Reply", openid(), time(), random());
				u.content().data().put(keyReply, t);
			}
			if(quiz != null && quiz.hasNext()) {
				next = quiz.next();
				u.output(i + ". " + next.getQuiz().value());
				lastQuizId = next.getQuiz().key();
			} else {
				finish = true;
				u.output("感谢您回复这些问题，请稍后留意我们的中奖通知！");
			}
		}
		
		public Iterator<Quiz> getQuiz(CorpusService service, int n) {
			List<Quiz> all = new ArrayList<>();
			all.addAll(service.assimilatedQuiz);
			Collections.shuffle(all);
			if(all.size() <= n) return all.iterator();
			else {
				List<Quiz> little = new ArrayList<>();
				for(int i=0;i<n;i++) {
					little.add(all.get(i));
				}
				return little.iterator();
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(finish) return init;
			return customer;
		}

		@Override
		public String name() {
			return "顾客回复问题";
		}

		@Override
		public boolean finish() {
			return finish;
		}
	};
	public ShopContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = shop;
		quiz.add(new KeyValue(String.join(Const.delimiter, Const.Version.V1, "Shop","Name", time()), "你的商铺名称？"));
		quiz.add(new KeyValue(String.join(Const.delimiter, Const.Version.V1, "Shop","Goods", time()), "你的商铺主要经营哪些商品或服务？"));
	}

}
