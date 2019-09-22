package blog.controller.admin;

import blog.service.UserService;
import blog.constant.WebConst;
import blog.controller.BaseController;
import blog.dto.LogAction;
import blog.exception.TipException;
import blog.model.Bo.RestResponseBo;
import blog.model.Bo.StatisticsBo;
import blog.model.Vo.CommentVo;
import blog.model.Vo.ContentVo;
import blog.model.Vo.LogVo;
import blog.model.Vo.UserVo;
import blog.service.SiteService;
import blog.service.LogService;
import blog.utils.GsonUtils;
import blog.utils.TaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller("adminIndexController")
@RequestMapping("/admin")
@Transactional(rollbackFor = TipException.class)
public class IndexController extends BaseController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);
    @Resource
    private SiteService siteService;

    @Resource
    private LogService logService;

    @Resource
    private UserService userService;

    /**
     * 首页住准备
     *
     * @param request
     * @return
     */
    @GetMapping(value = {"", "/index"})
    public String index(HttpServletRequest request)
    {
        LOGGER.info("Enter admin index method");
        List<CommentVo> comments = siteService.recentComments(5);
        List<ContentVo> contents = siteService.recentContents(5);
        StatisticsBo statistics = siteService.getStatistics();
        List<LogVo> logs = logService.getLogs(1, 5);

        request.setAttribute("comments", comments);
        request.setAttribute("articles", contents);
        request.setAttribute("statistics", statistics);
        request.setAttribute("logs", logs);
        return "admin/index";
    }

    @GetMapping("/profile")
    public String profile()
    {
        return "admin/profile";
    }

    @GetMapping("/logout")
    public String logout()
    {
        System.out.println("index----------logout");
        return "admin/login";
    }

    /**
     * 保存个人信息
     *
     * @param screenName
     * @param email
     * @param request
     * @param session
     * @return
     */
    @PostMapping("/profile")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo saveProfile(@RequestParam String screenName, @RequestParam String email, HttpServletRequest request, HttpSession session)
    {
        UserVo user = this.user(request);
        if (StringUtils.isNotBlank(screenName) && StringUtils.isNotBlank(email))
        {
            UserVo temp = new UserVo();
            temp.setUid(user.getUid());
            temp.setEmail(email);
            temp.setScreenName(screenName);

            userService.updateByUid(temp);
            logService.insertLog(LogAction.UP_INFO.getAction(), GsonUtils.toJsonString(temp), request.getRemoteAddr(), this.getUid(request));

            UserVo original = (UserVo) session.getAttribute(WebConst.LOGIN_SESSION_KEY);
            original.setEmail(email);
            original.setScreenName(screenName);
            session.setAttribute(WebConst.LOGIN_SESSION_KEY, original);
        }
        return RestResponseBo.ok();
    }

    @PostMapping("/password")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo updatePwd(@RequestParam String oldPassword, @RequestParam String password, HttpServletRequest request, HttpSession session)
    {
        UserVo user = this.user(request);
        if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(password)) return RestResponseBo.fail("请确认信息输入完整");
        if (!user.getPassword().equals(TaleUtils.MD5encode(user.getUsername() + oldPassword)))
            return RestResponseBo.fail("旧密码错误");
        if (password.length() < 6 || password.length() > 14) return RestResponseBo.fail("请输入6-14位密码");

        try
        {
            UserVo temp = new UserVo();
            temp.setUid(user.getUid());
            String pwd = TaleUtils.MD5encode(user.getUsername() + password);
            temp.setPassword(pwd);
            userService.updateByUid(temp);
            logService.insertLog(LogAction.UP_PWD.getAction(), null, request.getRemoteAddr(), this.getUid(request));

            UserVo original = (UserVo) session.getAttribute(WebConst.LOGIN_SESSION_KEY);
            original.setPassword(pwd);
            session.setAttribute(WebConst.LOGIN_SESSION_KEY, original);
            return RestResponseBo.ok();
        } catch (Exception e)
        {
            String msg = "密码修改失败！";
            if (e instanceof TipException) msg = e.getMessage();
            else LOGGER.error(msg, e);
            return RestResponseBo.fail(msg);
        }

    }


}
