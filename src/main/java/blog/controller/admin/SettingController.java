package blog.controller.admin;

import blog.constant.WebConst;
import blog.controller.BaseController;
import blog.dto.LogAction;
import blog.exception.TipException;
import blog.model.Bo.BackResponseBo;
import blog.model.Bo.RestResponseBo;
import blog.model.Vo.OptionVo;
import blog.service.LogService;
import blog.service.OptionService;
import blog.service.SiteService;
import blog.utils.GsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/setting")
public class SettingController extends BaseController
{

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingController.class);

    @Resource
    private OptionService optionService;

    @Resource
    private LogService logService;

    @Resource
    private SiteService siteService;

    /**
     * 系统设置
     */
    @GetMapping(value = "")
    public String setting(HttpServletRequest request)
    {
        List<OptionVo> voList = optionService.getOptions();
        Map<String, String> options = new HashMap<>();
        voList.forEach((option) -> {
            options.put(option.getName(), option.getValue());
        });
        request.setAttribute("options", options);
        return "admin/setting";
    }

    /**
     * 保存系统设置
     */
    @PostMapping(value = "")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo saveSetting(@RequestParam(required = false) String site_theme, HttpServletRequest request)
    {
        try
        {
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, String> querys = new HashMap<>();
            parameterMap.forEach((key, value) -> {
                querys.put(key, join(value));
            });

            optionService.saveOptions(querys);

            WebConst.initConfig = querys;

            if (StringUtils.isNotBlank(site_theme))
            {
                BaseController.THEME = "themes/" + site_theme;
            }
            logService.insertLog(LogAction.SYS_SETTING.getAction(), GsonUtils.toJsonString(querys), request.getRemoteAddr(), this.getUid(request));
            return RestResponseBo.ok();
        } catch (Exception e)
        {
            String msg = "保存设置失败";
            if (e instanceof TipException)
            {
                msg = e.getMessage();
            } else
            {
                LOGGER.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }
    }


    /**
     * 系统备份
     *
     * @return
     */
    @PostMapping(value = "backup")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo backup(@RequestParam String bk_type, @RequestParam String bk_path,
                                 HttpServletRequest request)
    {
        if (StringUtils.isBlank(bk_type))
        {
            return RestResponseBo.fail("请确认信息输入完整");
        }
        try
        {
            BackResponseBo backResponse = siteService.backup(bk_type, bk_path, "yyyyMMddHHmm");
            logService.insertLog(LogAction.SYS_BACKUP.getAction(), null, request.getRemoteAddr(), this.getUid(request));
            return RestResponseBo.ok(backResponse);
        } catch (Exception e)
        {
            String msg = "备份失败";
            if (e instanceof TipException)
            {
                msg = e.getMessage();
            } else
            {
                LOGGER.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }
    }


    /**
     * 数组转字符串
     *
     * @param arr
     * @return
     */
    private String join(String[] arr)
    {
        StringBuilder ret = new StringBuilder();
        String[] var3 = arr;
        int var4 = arr.length;

        for (int var5 = 0; var5 < var4; ++var5)
        {
            String item = var3[var5];
            ret.append(',').append(item);
        }
        return ret.length() > 0 ? ret.substring(1) : ret.toString();
    }
}
