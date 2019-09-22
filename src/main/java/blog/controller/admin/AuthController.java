package blog.controller.admin;

import blog.constant.WebConst;
import blog.controller.BaseController;
import blog.dto.LogAction;
import blog.exception.TipException;
import blog.model.Bo.RestResponseBo;
import blog.model.Vo.UserVo;
import blog.service.LogService;
import blog.service.UserService;
import blog.utils.Commons;
import blog.utils.TaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
@RequestMapping("/admin")
@Transactional(rollbackFor = TipException.class)
public class AuthController extends BaseController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    @Resource
    private UserService userService;
    @Resource
    private LogService logService;


    @GetMapping("/login")
    public String login()
    {
        return "admin/login";
    }


    @PostMapping(value = "login")
    @ResponseBody
    public RestResponseBo doLogin(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam(required = false) String remeber_me,
                                  HttpServletRequest request, HttpServletResponse response)
    {
        Integer error_count = cache.get("login_error_count");
        try
        {
            UserVo user = userService.login(username, password);
            request.getSession().setAttribute(WebConst.LOGIN_SESSION_KEY, user);
            if (StringUtils.isNotBlank(remeber_me)) TaleUtils.setCookie(response, user.getUid());
            logService.insertLog(LogAction.LOGIN.getAction(), null, request.getRemoteAddr(), user.getUid());
        } catch (Exception e)
        {
            error_count = error_count == null ? 1 : error_count + 1;
            if (error_count > 3) return RestResponseBo.fail("您输入密码已经错误超过3次，请1分钟后尝试");
            cache.set("login_error_count", error_count, 60 * 1);
            String msg = "登入失败";
            if (e instanceof TipException) msg = e.getMessage();
            else LOGGER.error(msg, e);
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

    @RequestMapping("logout")
    public void logout(HttpSession session, HttpServletRequest request, HttpServletResponse response)
    {
        session.removeAttribute(WebConst.LOGIN_SESSION_KEY);
        Cookie cookie = new Cookie(WebConst.LOGIN_SESSION_KEY, "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        try
        {
            response.sendRedirect(Commons.site_login());
        } catch (IOException e)
        {
            e.printStackTrace();
            LOGGER.error("注销失败", e);
        }
    }
}
