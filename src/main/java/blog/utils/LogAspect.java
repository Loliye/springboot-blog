package blog.utils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.Arrays;

@Aspect
@Component
public class LogAspect
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSource.class);

    @Pointcut("execution(public * blog.controller..*.*(..))")
    public void webLog()
    {
    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable
    {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = requestAttributes.getRequest();
        LOGGER.info("URL:" + request.getRequestURI().toString() +
                ",IP:" + request.getRemoteAddr() +
                ",CLASS_METHOD:" + joinPoint.getSignature().getDeclaringTypeName() +
                "." + joinPoint.getSignature().getName() +
                ",ARGS:" + Arrays.toString(joinPoint.getArgs()));

    }

    @AfterReturning(returning = "object", pointcut = "webLog()")
    public void doAfterReturning(Object object) throws Throwable
    {
        LOGGER.info("RESPONSE:" + object);
    }

}
