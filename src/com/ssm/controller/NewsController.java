package com.ssm.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ssm.pojo.Comment;
import com.ssm.pojo.CommentPlus;
import com.ssm.pojo.Like;
import com.ssm.pojo.News;
import com.ssm.pojo.NewsPlus;
import com.ssm.pojo.NewsType;
import com.ssm.service.NewsService;
import com.ssm.service.NewsTypeService;
import com.ssm.service.UserService;

@Controller
@RequestMapping("")
public class NewsController {
	@Autowired
	NewsService newsService;
	@Autowired
	NewsTypeService newsTypeService;
	@Autowired
	UserService userService;

	@RequestMapping("listNewsPage")
	public String listNewsPage(Model model) {
		// 用户列表的记录总数
		int amount = newsService.total();
		model.addAttribute("amount", amount);
		return "news-list";
	}

	@ResponseBody
	@RequestMapping("listNewsTable")
	public JSONObject listNewsTable(String draw, String start, String length) {
		int amount = newsService.total();
		List<News> newsList = newsService.list();
		List<NewsPlus> newsPlusList = new ArrayList<NewsPlus>();
		for (int i = 0; i < newsList.size(); i++) {
			NewsPlus newsPlus = new NewsPlus();
			News n = newsList.get(i);
			newsPlus.setId(n.getId());
			String news_type = newsTypeService.findNewsTypeById(n.getNewstype_id()).getName();
			newsPlus.setNews_type(news_type);
			newsPlus.setTitle(n.getTitle());
			newsPlus.setNewsabstract(n.getNewsabstract());
			String user_name = userService.get(n.getUser_id()).getName();
			newsPlus.setUser_name(user_name);
			newsPlus.setReading(n.getReading());
			String status = (n.getStatus() == 0) ? "<span class='label label-default radius'>待发布</span>"
					: "<span class='label label-success radius'>已发布</span>";
			newsPlus.setStatus(status);
			newsPlus.setCreatetime(n.getCreatetime());
			String stick = (n.getStick() == 0) ? "<span class='label label-default radius'>不置顶</span>"
					: "<span class='label label-success radius'>置顶</span>";
			newsPlus.setStick(stick);
			String audit = (n.getAudit() == 0) ? "<span class='label label-default radius'>待审核</span>"
					: "<span class='label label-success radius'>审核通过</span>";
			newsPlus.setAudit(audit);
			newsPlus.setContent(n.getContent());
			newsPlusList.add(newsPlus);
		}
		// 获取Action的上下文，就是action的一些运行信息，环境等等
		int mydraw = 0, mystart = 0, mylength = 0, mypage = 0;
		// 获取前端的分页参数
		mydraw = Integer.parseInt(draw);
		mystart = Integer.parseInt(start);
		mylength = Integer.parseInt(length);
		mypage = (mystart / mylength) + 1;
		// System.out.println("访问次数mydraw==" + mydraw);
		// System.out.println("起始下标mystart==" + mystart);
		// System.out.println("列表长度mylength==" + mylength);
		// System.out.println("当前页数mypage==" + mypage);

		List<NewsPlus> pageList = new ArrayList<NewsPlus>();
		int mylen = 0;
		if ((amount - (mypage - 1) * mylength) > 10) {
			mylen = mylength;
		} else {
			mylen = amount - (mypage - 1) * mylength;
		}
		for (int i = 0; i < mylen; i++) {
			pageList.add(newsPlusList.get(mystart + i));
		}
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("data", pageList);
		info.put("draw", mydraw);
		info.put("start", mystart);
		info.put("length", mylength);
		info.put("recordsTotal", amount);
		info.put("recordsFiltered", amount);
		String a = JSON.toJSONString(info);
		JSONObject news_table = JSONObject.parseObject(a);
		return news_table;
	}

	@RequestMapping("addNewsPage")
	public String addNewsPage(Model model) {
		List<NewsType> newsTypeList = newsTypeService.list();
		model.addAttribute("newsTypeList", newsTypeList);
		return "news-add";
	}

	@ResponseBody
	@RequestMapping("addNews")
	public String addNews(String title, String newstype_id, String newsabstract, String content, HttpSession session) {
		long user_id = (long) session.getAttribute("userId");
		System.out.println("title==" + title);
		System.out.println("newstype_id==" + newstype_id);
		System.out.println("newsabstract==" + newsabstract);
		System.out.println("content==" + content);
		System.out.println("user_id==" + user_id);
		// 获取当前时间
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateNowStr = sdf.format(d);
		News news = new News();
		try {
			news.setTitle(title);
			news.setNewstype_id(Integer.parseInt(newstype_id));
			news.setTpic_url(null);
			news.setNewsabstract(newsabstract);
			news.setUser_id(user_id);
			news.setStatus(0);
			news.setReading(0);
			news.setCreatetime(dateNowStr);
			news.setCpic_url(null);
			news.setStick(0);
			news.setAudit(0);
			news.setContent(content);
			// 将拿到的数据插入数据库
			newsService.add(news);
			// 向前端返回操作成功的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "success");
			return json.toJSONString();
		} catch (Exception e) {
			e.printStackTrace();
			// 向前端返回操作失败的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "error");
			return json.toJSONString();
		}

	}

	@ResponseBody
	@RequestMapping("deleteNews")
	public String deleteNews(int id) {
		try {
			// 根据id删除数据
			newsService.delete(id);
			// 向前端返回操作成功的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "success");
			return json.toJSONString();
		} catch (Exception e) {
			// 向前端返回操作失败的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "error");
			return json.toJSONString();
		}
	}

	// 根据id批量删除数据
	@ResponseBody
	@RequestMapping("deleteNewss")
	public String deleteNewss(int[] idArray) {
		try {
			// 遍历数组
			for (int i = 0; i < idArray.length; i++) {
				// 根据id批量删除数据
				newsService.delete(idArray[i]);
			}
			// 向前端返回操作成功的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "success");
			return json.toJSONString();
		} catch (Exception e) {
			// 向前端返回操作失败的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "error");
			return json.toJSONString();
		}
	}

	@RequestMapping("editNewsPage")
	public String editNewsPage(Model model, int id) {
		List<NewsType> newsTypeList = newsTypeService.list();
		model.addAttribute("newsTypeList", newsTypeList);
		News news = newsService.get(id);
		model.addAttribute("news", news);
		return "news-edit";
	}

	@ResponseBody
	@RequestMapping("editNews")
	public String editNews(News news) {
		System.out.println("news.getContent()==" + news.getContent());
		try {
			// 提交编辑好的用户
			newsService.update(news);
			// 向前端返回操作成功的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "success");
			return json.toJSONString();
		} catch (Exception e) {
			// 向前端返回操作失败的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "error");
			return json.toJSONString();
		}
	}

	@RequestMapping("showNewsPage")
	public String showNewsPage(Model model, int id, HttpSession session) {
		long user_id = (long) session.getAttribute("userId");
		List<NewsType> newsTypeList = newsTypeService.list();
		model.addAttribute("newsTypeList", newsTypeList);
		News news = newsService.get(id);
		String user_name = userService.get(news.getUser_id()).getRealname();
		model.addAttribute("user_name", user_name);
		news.setReading(news.getReading() + 1);
		model.addAttribute("news", news);
		newsService.update(news);

		int like_total = newsService.likeTotal(id);
		model.addAttribute("like_total", like_total);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", user_id);
		map.put("news_id", id);
		Like like1 = newsService.getLike(map);
		if (like1 != null) {
			model.addAttribute("like", like1);
		} else {
			Like like2 = new Like();
			like2.setId(0);
			like2.setNews_id(0);
			like2.setUser_id((long) 0);
			like2.setStatus(0);
			model.addAttribute("like", like2);
		}

		List<Comment> commentList = newsService.getComment(id);
		List<CommentPlus> commentPlusList = new ArrayList<CommentPlus>();
		for (int i = 0; i < commentList.size(); i++) {
			CommentPlus cp = new CommentPlus();
			cp.setId(commentList.get(i).getId());
			cp.setContent(commentList.get(i).getContent());
			cp.setImg("static/h-ui.admin/images/touxiang.png");
			String realname = userService.get(commentList.get(i).getUser_id()).getRealname();
			cp.setReplyName(realname);
			cp.setTime(commentList.get(i).getCreatetime());
			commentPlusList.add(cp);
		}
		JSONObject json = new JSONObject();
		// json.put("commentPlusList", commentPlusList);
		model.addAttribute("commentPlusList", json.toJSONString(commentPlusList));

		return "news-show";
	}

	@ResponseBody
	@RequestMapping("publishNews")
	public String publishNews(int id) {
		try {
			News news = newsService.get(id);
			// 设置为发布状态
			news.setStatus(1);
			// 保存数据
			newsService.update(news);
			// 向前端返回操作成功的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "success");
			return json.toJSONString();
		} catch (Exception e) {
			// 向前端返回操作失败的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "error");
			return json.toJSONString();
		}
	}

	@RequestMapping("searchNewsByKeyPage")
	public String searchNewsByKeyPage() {
		return "news-search";
	}

	@ResponseBody
	@RequestMapping("searchNewsByKey")
	public JSONObject searchNewsByKey(String key, String datemin, String datemax) {
		System.out.println("查找datemin：" + datemin);
		System.out.println("查找datemax：" + datemax);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", key);
		map.put("endTime", datemax);
		map.put("startTime", datemin);
		List<News> newsList = newsService.findNewsByTime(map);
		for (News n : newsList) {
			System.out.println("查找到的对象：" + n.getTitle());
		}
		List<NewsPlus> newsPlusList = new ArrayList<NewsPlus>();
		for (int i = 0; i < newsList.size(); i++) {
			NewsPlus newsPlus = new NewsPlus();
			News n = newsList.get(i);
			newsPlus.setId(n.getId());
			String news_type = newsTypeService.findNewsTypeById(n.getNewstype_id()).getName();
			newsPlus.setNews_type(news_type);
			newsPlus.setTitle(n.getTitle());
			newsPlus.setNewsabstract(n.getNewsabstract());
			String user_name = userService.get(n.getUser_id()).getName();
			newsPlus.setUser_name(user_name);
			newsPlus.setReading(n.getReading());
			String status = (n.getStatus() == 0) ? "<span class='label label-default radius'>待发布</span>"
					: "<span class='label label-success radius'>已发布</span>";
			newsPlus.setStatus(status);
			newsPlus.setCreatetime(n.getCreatetime());
			String stick = (n.getStick() == 0) ? "<span class='label label-default radius'>不置顶</span>"
					: "<span class='label label-success radius'>置顶</span>";
			newsPlus.setStick(stick);
			String audit = (n.getAudit() == 0) ? "<span class='label label-default radius'>待审核</span>"
					: "<span class='label label-success radius'>审核通过</span>";
			newsPlus.setAudit(audit);
			newsPlus.setContent(n.getContent());
			newsPlusList.add(newsPlus);
		}
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("data", newsPlusList);
		String a = JSON.toJSONString(info);
		JSONObject news_table = JSONObject.parseObject(a);
		return news_table;
	}

	@ResponseBody
	@RequestMapping("pickLike")
	public String pickLike(int id, int news_id, int status, HttpSession session) {
		long user_id = (long) session.getAttribute("userId");
		try {
			// 如果数据库中没有数据
			if ((status == 0) && (id == 0)) {
				Like like = new Like();
				like.setNews_id(news_id);
				like.setUser_id(user_id);
				like.setStatus(1);
				// 提交编辑好的数据
				newsService.addLike(like);
			} else if ((id != 0) && (status == 1)) {
				Like like = new Like();
				like.setId(id);
				like.setNews_id(news_id);
				like.setUser_id(user_id);
				like.setStatus(0);
				newsService.updateLikeById(like);
			} else {
				Like like = new Like();
				like.setId(id);
				like.setNews_id(news_id);
				like.setUser_id(user_id);
				like.setStatus(1);
				newsService.updateLikeById(like);
			}
			// 向前端返回操作成功的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "success");
			return json.toJSONString();
		} catch (Exception e) {
			// 向前端返回操作失败的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "error");
			return json.toJSONString();
		}
	}

	@ResponseBody
	@RequestMapping("pickComment")
	public String pickComment(int news_id, String content, HttpSession session) {
		// 获取当前时间
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String createtime = sdf.format(d);

		long user_id = (long) session.getAttribute("userId");
		Comment comment = new Comment();
		try {
			comment.setNews_id(news_id);
			comment.setUser_id(user_id);
			comment.setContent(content);
			comment.setCreatetime(createtime);
			newsService.addComment(comment);
			// 向前端返回操作成功的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "success");
			return json.toJSONString();
		} catch (Exception e) {
			// 向前端返回操作失败的json信息
			JSONObject json = new JSONObject();
			json.put("msg", "error");
			return json.toJSONString();
		}
	}

}
