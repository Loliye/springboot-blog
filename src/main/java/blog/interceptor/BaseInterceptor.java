package blog.interceptor;

import blog.constant.WebConst;
import blog.dto.Types;
import blog.model.Vo.UserVo;
import blog.service.UserService;
import blog.utils.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class BaseInterceptor implements HandlerInterceptor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseInterceptor.class);
    private static final String USER_AGENT = "user-agent";
    @Resource
    private UserService userService;

    private MapCache cache = MapCache.single();

    @Resource
    private Commons commons;

    @Resource
    private AdminCommons adminCommons;

    //    请求前拦截处理
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        String uri = request.getRequestURI();
        LOGGER.info("UserAgent:{}", request.getHeader(USER_AGENT));
        LOGGER.info("用户访问地址:{},来路地址:{}", uri, IPKit.getIpAddrByRequest(request));

        //        请求拦截处理
        UserVo userVo = TaleUtils.getLoginUser(request);
        if (userVo == null)
        {
            Integer uid = TaleUtils.getCoolieUid(request);
            if (uid != null)
            {
                userVo = userService.queryUserById(uid);
                request.getSession().setAttribute(WebConst.LOGIN_SESSION_KEY, userVo);
            }
        }
        if (uri.startsWith("/admin") && !uri.startsWith("/admin/login") && userVo == null)
        {
            request.getRequestDispatcher("/admin/login").forward(request, response);
            return false;
        }
        if (request.getMethod().equals("GET"))
        {
            String csrf_token = UUID.UU64();
            // 默认存储30分钟
            cache.hset(Types.CSRF_TOKEN.getType(), csrf_token, uri, 30 * 60);
            request.setAttribute("_csrf_token", csrf_token);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception
    {
        request.setAttribute("commons", commons);
        request.setAttribute("adminCommons", adminCommons);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception
    {
    }
}
