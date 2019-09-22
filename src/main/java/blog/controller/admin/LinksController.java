package blog.controller.admin;

import blog.controller.BaseController;
import blog.dto.Types;
import blog.exception.TipException;
import blog.model.Bo.RestResponseBo;
import blog.model.Vo.MetaVo;
import blog.service.MetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


//友情链接管理
@Controller
@RequestMapping("/admin/links")
public class LinksController extends BaseController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LinksController.class);
    @Resource
    private MetaService metaService;

    @GetMapping("")
    public String index(HttpServletRequest request)
    {
        List<MetaVo> metaVos = metaService.getMetas(Types.LINK.getType());
        request.setAttribute("links", metaVos);
        return "admin/links";
    }

    //    保存友情连接
    @PostMapping(value = "/save")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo saveLink(@RequestParam String title, @RequestParam String url,
                                   @RequestParam String logo, @RequestParam Integer mid,
                                   @RequestParam(value = "sort", defaultValue = "0") int sort)
    {
        try
        {
            MetaVo metas = new MetaVo();
            metas.setName(title);
            metas.setSlug(url);
            metas.setDescription(logo);
            metas.setSort(sort);
            metas.setType(Types.LINK.getType());
            if (null != mid)
            {
                metas.setMid(mid);
                metaService.update(metas);
            } else
            {
                metaService.saveMeta(metas);
            }
        } catch (Exception e)
        {
            String msg = "友链保存失败";
            if (e instanceof TipException)
            {
                msg = e.getMessage();
            } else
            {
                LOGGER.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

    //    删除友情连接
    @RequestMapping(value = "/delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam int mid)
    {
        try
        {
            metaService.delete(mid);
        } catch (Exception e)
        {
            String msg = "友链删除失败";
            if (e instanceof TipException)
            {
                msg = e.getMessage();
            } else
            {
                LOGGER.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }
}
