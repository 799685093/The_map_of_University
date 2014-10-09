package com.wpy.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wpy.dao.ActivityDao;
import com.wpy.service.ActivityService;
import com.wpy.util.JsonUtil;

public class ActivityServleat extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActivityService activityService;

	/**
	 * Constructor of the object.
	 */
	public ActivityServleat() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		String msg = null;
		String action_flag = request.getParameter("action_flag");
		if (action_flag.equals("findAll")) {
			msg = JsonUtil.createJsonString("activities",
					activityService.getActivity());
			System.out.println(msg);
		} else if (action_flag.equals("findSimple")) {
			String name = request.getParameter("name");
			System.out.println(name);
			Map<String, Object> map = activityService.findByName(name);
			if (map.isEmpty() == true) {
				msg = "Nothing";
			} else {
				msg = JsonUtil.createJsonString("activities", map);
			}
			System.out.println(msg);
		}
		out.print(msg);
		out.flush();
		out.close();
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException {
		activityService = new ActivityDao();
	}

}
